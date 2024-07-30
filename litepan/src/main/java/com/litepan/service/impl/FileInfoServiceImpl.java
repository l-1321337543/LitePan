package com.litepan.service.impl;

import com.litepan.entity.config.AppConfig;
import com.litepan.entity.constants.Constants;
import com.litepan.entity.dto.SessionWebUserDTO;
import com.litepan.entity.dto.UploadResultDTO;
import com.litepan.entity.dto.UserSpaceDTO;
import com.litepan.entity.po.UserInfo;
import com.litepan.entity.query.UserInfoQuery;
import com.litepan.enums.*;
import com.litepan.exception.BusinessException;
import com.litepan.mappers.UserInfoMapper;
import com.litepan.service.FileInfoService;
import com.litepan.entity.po.FileInfo;
import com.litepan.entity.query.FileInfoQuery;
import com.litepan.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.litepan.entity.vo.PaginationResultVO;
import com.litepan.mappers.FileInfoMapper;
import com.litepan.entity.query.SimplePage;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;


/**
 * @Description: 文件信息表ServiceImpl
 * @date: 2024/07/26
 */
@Service("fileInfoService")
@Slf4j
public class FileInfoServiceImpl implements FileInfoService {

    @Resource
    private FileInfoMapper<FileInfo, FileInfoQuery> fileInfoMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AppConfig appConfig;

    @Resource
    @Lazy//懒加载，防止循环依赖
    private FileInfoServiceImpl fileInfoService;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<FileInfo> findListByParam(FileInfoQuery query) {
        return fileInfoMapper.selectListByQuery(query);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(FileInfoQuery query) {
        return fileInfoMapper.selectCountByQuery(query);
    }

    /**
     * 分页查询
     */
    @Override
    public PaginationResultVO<FileInfo> findListByPage(FileInfoQuery query) {
        Integer count = this.findCountByParam(query);
        int pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), this.findListByParam(query));
    }

    /**
     * 新增
     */
    @Override
    public Integer add(FileInfo bean) {
        return fileInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<FileInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return fileInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/修改
     */
    @Override
    public Integer addOrUpdateBatch(List<FileInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return fileInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据fileIdAndUserId查询
     */
    @Override
    public FileInfo getFileInfoByFileIdAndUserId(String fileId, String userId) {
        return fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
    }

    /**
     * 根据fileIdAndUserId更新
     */
    @Override
    public Integer updateFileInfoByFileIdAndUserId(FileInfo bean, String fileId, String userId) {
        return fileInfoMapper.updateByFileIdAndUserId(bean, fileId, userId);
    }

    /**
     * 根据fileIdAndUserId删除
     */
    @Override
    public Integer deleteFileInfoByFileIdAndUserId(String fileId, String userId) {
        return fileInfoMapper.deleteByFileIdAndUserId(fileId, userId);
    }

    /**
     * 上传文件
     *
     * @param webUserDTO 封装的用户DTO
     * @param fileId     非必传，文件的第一块没有fileId
     * @param file       上传的文件本身
     * @param fileName   文件名
     * @param filePid    父级Id
     * @param fileMD5    文件MD5值
     * @param chunkIndex 分块索引
     * @param chunks     分块总数
     * @return 文件上传状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResultDTO uploadFile(SessionWebUserDTO webUserDTO, String fileId, MultipartFile file,
                                      String fileName, String filePid, String fileMD5, Integer chunkIndex, Integer chunks) {
        boolean uploadSuccess = true;
        if (StringTools.isEmpty(fileId)) {
            fileId = StringTools.getRandomString(Constants.LENGTH_10);
        }
        //返给前端的DTO
        UploadResultDTO uploadResultDTO = new UploadResultDTO();
        File tempFileFolder = null;
        try {
            uploadResultDTO.setFileId(fileId);

            Date curDate = new Date();

            //获取用户空间使用情况
            UserSpaceDTO userSpaceUse = redisComponent.getUserSpaceUse(webUserDTO.getUserId());

            //根据第一快的MD5值判断是否可以秒传
            if (chunkIndex == 0) {
                //在数据库中查找是否有已经转码成功的相同MD5值的文件
                FileInfoQuery infoQuery = new FileInfoQuery();
                infoQuery.setFileMd5(fileMD5);
                infoQuery.setStatus(FileStatusEnums.USING.getStatus());
                infoQuery.setSimplePage(new SimplePage(0, 1));
                List<FileInfo> dbFileInfos = fileInfoMapper.selectListByQuery(infoQuery);

                //如果找到，则进行秒传
                if (!dbFileInfos.isEmpty()) {
                    FileInfo dbFileInfo = dbFileInfos.get(0);

                    //判断文件大小
                    if (dbFileInfo.getFileSize() + userSpaceUse.getUseSpace() > userSpaceUse.getTotalSpace()) {
                        throw new BusinessException(ResponseCodeEnum.CODE_904);
                    }
                    // 给文件重命名，防止文件名冲突
                    fileName = autoRename(filePid, webUserDTO.getUserId(), fileName);

                    // 将数据库查到的记录中部分字段信息改为本次上传文件的
                    dbFileInfo.setFileId(fileId)
                            .setFilePid(filePid)
                            .setUserId(webUserDTO.getUserId())
                            .setCreateTime(curDate)
                            .setLastUpdateTime(curDate)
                            .setDelFlag(FileDelFlagEnums.NORMAL.getFlag())
                            .setFileName(fileName)
                            .setStatus(FileStatusEnums.USING.getStatus());

                    // 更新数据库
                    fileInfoMapper.insert(dbFileInfo);

                    uploadResultDTO.setStatus(UploadStatusEnums.UPLOAD_SECONDS.getCode());

                    // 更新用户空间使用情况
                    updateUseSpace(webUserDTO.getUserId(), dbFileInfo.getFileSize());
                    return uploadResultDTO;
                }
            }

            //分片上传
            //先判断该分片上传后空间是否足够
            Long fileTempSize = redisComponent.getFileTempSize(webUserDTO.getUserId(), fileId);//该文件已上传分片的总大小
            if (fileTempSize + file.getSize() + userSpaceUse.getUseSpace() > userSpaceUse.getTotalSpace()) {
                throw new BusinessException(ResponseCodeEnum.CODE_904);
            }

            //建立存储分片文件的目录
            String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;// d:/LitePan/temp/
            String curUserFolderName = webUserDTO.getUserId() + fileId;
            tempFileFolder = new File(tempFolderName + curUserFolderName);// d:/LitePan/temp/userId+fileId
            if (!tempFileFolder.exists()) {
                tempFileFolder.mkdirs();
            }

            //建立存储该分片的文件，并将传进来的分片写入到该文件中
            File newFile = new File(tempFileFolder.getPath() + "/" + chunkIndex);
            file.transferTo(newFile);

            if (chunkIndex < chunks - 1) {//如果不是最后一片，继续上传
                //保存本次分片大小到缓存中
                redisComponent.saveFileTempSize(webUserDTO.getUserId(), fileId, file.getSize());

                //设置上传状态为上传中，并返回给前端，以便其继续上传
                uploadResultDTO.setStatus(UploadStatusEnums.UPLOADING.getCode());
                return uploadResultDTO;
            }

            //最后一个分片上传完成，写入缓存，记录数据库
            redisComponent.saveFileTempSize(webUserDTO.getUserId(), fileId, file.getSize());
            String fileSuffix = StringTools.getFileSuffix(fileName);

            String mouth = DateUtil.format(new Date(), DateTimePattenEnum.YYYYMM.getPatten());//和realFileName一起拼成保存在数据库的文件路径
            String realFileName = curUserFolderName + fileSuffix;//userId+fileId+fileSuffix

            FileTypeEnums fileTypeEnums = FileTypeEnums.getFileTypeBySuffix(fileSuffix);

            //更新用户空间使用情况
            Long tempSize = redisComponent.getFileTempSize(webUserDTO.getUserId(), fileId);
            updateUseSpace(webUserDTO.getUserId(), tempSize);

            //自动重命名文件名
            String dbFileName = autoRename(filePid, webUserDTO.getUserId(), fileName);

            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilePid(filePid)
                    .setFileId(fileId)
                    .setFileMd5(fileMD5)
                    .setUserId(webUserDTO.getUserId())
                    .setFileName(dbFileName)
                    .setCreateTime(curDate)
                    .setLastUpdateTime(curDate)
                    .setStatus(FileStatusEnums.TRANSFER.getStatus())
                    .setDelFlag(FileDelFlagEnums.NORMAL.getFlag())
                    .setFilePath(mouth + "/" + realFileName)
                    .setFileCategory(fileTypeEnums.getCategory().getCategory())
                    .setFileType(fileTypeEnums.getType())
                    .setFolderType(FileFolderTypeEnums.FILE.getType())
//                    .setRecoverTime()
//                    .setFileCover()
                    .setFileSize(tempSize);

            //写入数据库
            fileInfoMapper.insert(fileInfo);

            //设置上传状态为上传完成，并返回给前端
            uploadResultDTO.setStatus(UploadStatusEnums.UPLOAD_FINISH.getCode());

            //文件转码
            //此处转码应该在上面事务提交之后在执行
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    fileInfoService.transfer(fileInfo.getFileId(), webUserDTO);
                }
            });
            return uploadResultDTO;
        } catch (BusinessException e) {
            uploadSuccess = false;
            log.error("文件上传出错", e);
            throw e;
        } catch (Exception e) {
            uploadSuccess = false;
            log.error("文件上传出错", e);
        } finally {
            //如果上传过程出错，删除temp文件夹下面相应的临时文件夹
            if (!uploadSuccess && tempFileFolder != null) {
                try {
                    FileUtils.deleteDirectory(tempFileFolder);
                } catch (IOException e) {
                    log.error("删除临时目录失败", e);
                }
            }
        }
        return uploadResultDTO;
    }

    /**
     * 新建文件夹
     *
     * @param userId     用户Id
     * @param folderName 文件夹名
     * @param filePid    父级目录
     * @return 创建好的FileInfo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileInfo newFolder(String userId, String folderName, String filePid) {
        //校验是否有重名文件夹
        checkFileName(filePid, userId, folderName, FileFolderTypeEnums.FOLDER.getType());

        FileInfo fileInfo = new FileInfo();
        Date curDate = new Date();
        String mouth = DateUtil.format(curDate, DateTimePattenEnum.YYYYMM.getPatten());
        fileInfo.setFileId(StringTools.getRandomString(Constants.LENGTH_10))
                .setFileName(folderName)
                .setFilePid(filePid)
                .setUserId(userId)
                .setFolderType(FileFolderTypeEnums.FOLDER.getType())
                .setCreateTime(curDate)
                .setLastUpdateTime(curDate)
                .setFilePath(mouth + "/" + folderName)
                .setDelFlag(FileDelFlagEnums.NORMAL.getFlag())
                .setStatus(FileStatusEnums.USING.getStatus());
        fileInfoMapper.insert(fileInfo);
        return fileInfo;
    }

    /**
     * 重命名文件/文件夹
     *
     * @param fileId   文件Id
     * @param fileName 文件名
     * @param userId   用户Id
     * @return 重命名后的FileInfo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileInfo rename(String userId, String fileId, String fileName) {
        FileInfo dbFileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
        if (dbFileInfo == null) {
            throw new BusinessException("文件/文件夹不存在");
        }
        String filePid = dbFileInfo.getFilePid();
        if (dbFileInfo.getFolderType().equals(FileFolderTypeEnums.FILE.getType())) {
            //如果是文件，前端传过来的filename是不带拓展名的，需要手动加上
            fileName = fileName + StringTools.getFileSuffix(dbFileInfo.getFileName());
        }
        //校验是否重名
        checkFileName(filePid, userId, fileName, dbFileInfo.getFolderType());
        FileInfo fileInfo = new FileInfo();
        Date curDate = new Date();
        fileInfo.setFileName(fileName);
        fileInfo.setLastUpdateTime(curDate);
        //更新数据
        fileInfoMapper.updateByFileIdAndUserId(fileInfo, fileId, userId);

        //二次查询数据库
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setFilePid(filePid);
//        fileInfoQuery.setFileId(fileId);
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setFileName(fileName);
        Integer count = fileInfoMapper.selectCountByQuery(fileInfoQuery);
        if (count > 1) {
            throw new BusinessException("文件名" + fileName + "已存在");
        }
        //返回数据
        dbFileInfo.setFileName(fileName);
        dbFileInfo.setLastUpdateTime(curDate);
        return dbFileInfo;
    }

    /**
     * 移动文件/文件夹到指定文件夹
     *
     * @param userId  用户Id
     * @param fileIds 前端选中的要移动的文件/文件夹的fileId，以","分隔
     * @param filePid 目标目录的fileId，即将前端选中的要移动的文件/文件夹的filePid改为此值
     */
    @Override
    public void changeFileFolder(String userId, String fileIds, String filePid) {
        //禁止自己移入自己
        if (fileIds.equals(filePid)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //禁止移入不存在的、放在回收站的、被删除的目录
        if (!filePid.equals(Constants.ZERO_STR)) {
            FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(filePid, userId);
            if (fileInfo == null || !fileInfo.getDelFlag().equals(FileDelFlagEnums.NORMAL.getFlag())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }

        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setDelFlag(FileDelFlagEnums.NORMAL.getFlag());
        fileInfoQuery.setFilePid(filePid);
        //查询要移入的目标目录下的文件
        List<FileInfo> dbFileInfos = fileInfoMapper.selectListByQuery(fileInfoQuery);

        //将其转换为fileName作为key，FileInfo作为Value的Map
        Map<String, FileInfo> fileNameMap = dbFileInfos.stream()
                .collect(Collectors.toMap(FileInfo::getFileName, Function.identity(), (date1, date2) -> date2));

        fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setFileIdArray(fileIds.split(","));
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setDelFlag(FileDelFlagEnums.NORMAL.getFlag());

        //前端被选中要移动的文件
        List<FileInfo> selectedFileInfoList = fileInfoMapper.selectListByQuery(fileInfoQuery);
        //遍历前端选中的文件，判断是否和要移入的文件夹中的文件命名冲突
        selectedFileInfoList.forEach(selectedFileInfo -> {
            FileInfo mapFileInfo = fileNameMap.get(selectedFileInfo.getFileName());
            FileInfo fileInfo = new FileInfo();
            if (mapFileInfo != null) {//文件命名冲突，需要重命名
                fileInfo.setFileName(StringTools.rename(selectedFileInfo.getFileName()));
            }
            fileInfo.setFilePid(filePid);
            fileInfoMapper.updateByFileIdAndUserId(fileInfo, selectedFileInfo.getFileId(), userId);
        });
    }


    /**
     * 校验文件/文件夹是否重名
     *
     * @param filePid    父级目录
     * @param userId     用户Id
     * @param filename   文件/文件夹名称
     * @param folderType 文件类型
     */
    private void checkFileName(String filePid, String userId, String filename, Integer folderType) {
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setFileName(filename);
        fileInfoQuery.setFilePid(filePid);
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setFolderType(folderType);
        Integer count = fileInfoMapper.selectCountByQuery(fileInfoQuery);
        if (count > 0) {
            if (folderType.equals(FileFolderTypeEnums.FOLDER.getType())) {
                throw new BusinessException("此目录下已存在同命文件夹，请修改名称");
            }
            if (folderType.equals(FileFolderTypeEnums.FILE.getType())) {
                throw new BusinessException("此目录下已存在同命文件，请修改名称");
            }
        }
    }

    /**
     * 从文件信息表中根据下面三个参数查询是否存在文件正常的记录，若存在，则重命名，不存在则无需重命名
     *
     * @param filePid  父级ID
     * @param userId   用户ID
     * @param fileName 文件名
     * @return 重命名后的文件名
     */
    private String autoRename(String filePid, String userId, String fileName) {
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setFilePid(filePid);
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setDelFlag(FileDelFlagEnums.NORMAL.getFlag());
        fileInfoQuery.setFileName(fileName);

        // 从数据库查询有没有符合以上条件的数据
        Integer count = fileInfoMapper.selectCountByQuery(fileInfoQuery);

        // 如果查到，则需要重命名
        if (count > 0) {
            fileName = StringTools.rename(fileName);
        }
        // 没查到直接返回原文件名
        return fileName;
    }

    /**
     * 更新用户空间使用情况
     *
     * @param userId   更新的用户
     * @param useSpace 变更的空间大小，可正可负
     */
    private void updateUseSpace(String userId, Long useSpace) {
        Integer count = userInfoMapper.updateUseSpaceByUserId(userId, useSpace, null);
        if (count == 0) {
            // 更新失败
            throw new BusinessException(ResponseCodeEnum.CODE_904);
        }
        // 更新数据库成功之后，更新缓存中的用户空间使用情况
        UserSpaceDTO userSpaceDTO = redisComponent.getUserSpaceUse(userId);
        userSpaceDTO.setUseSpace(userSpaceDTO.getUseSpace() + useSpace);
        redisComponent.saveUserSpaceUse(userId, userSpaceDTO);
    }

    /**
     * 文件转码
     *
     * @param fileId     文件ID
     * @param webUserDTO 封装的用户DTO
     */
    @Async
    public void transfer(String fileId, SessionWebUserDTO webUserDTO) {
        boolean transferSuccess = true;//是否转码成功
        String targetFilePath = null;//保存在服务器上面的文件名
        String cover = null;//封面
        FileTypeEnums fileTypeEnum = null;
        FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, webUserDTO.getUserId());
        try {
            if (fileInfo == null
                    || !fileInfo.getStatus().equals(FileStatusEnums.TRANSFER.getStatus())) {
                return;
            }
            //临时目录
            String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
            String curUserFolderName = webUserDTO.getUserId() + fileId;
            File tempFolder = new File(tempFolderName + curUserFolderName);

            String fileSuffix = StringTools.getFileSuffix(fileInfo.getFileName());

            //目标目录
            String targetFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE; // d:/LitePan/file/
            String mouth = DateUtil.format(fileInfo.getCreateTime(), DateTimePattenEnum.YYYYMM.getPatten());
            File targetFolder = new File(targetFolderName + mouth);
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }

            //真实文件名
            String realFileName = curUserFolderName + fileSuffix;
            targetFilePath = targetFolder.getPath() + "/" + realFileName;

            //合并文件
            union(tempFolder.getPath(), targetFilePath, fileInfo.getFileName(), true);

            //视频文件切割、生成缩略图以及封面设置
            fileTypeEnum = FileTypeEnums.getFileTypeBySuffix(fileSuffix);
            if (fileTypeEnum.equals(FileTypeEnums.VIDEO)) {//视频
                //视频切割
                cutFile4Video(fileId, targetFilePath);
                cover = mouth + "/" + curUserFolderName + Constants.IMAGE_PNG_SUFFIX;
                String coverPath = targetFolderName + cover;
                //创建封面
                ScaleFilter.createCover4Video(new File(targetFilePath), Constants.LENGTH_150, new File(coverPath));
            } else if (fileTypeEnum.equals(FileTypeEnums.IMAGE)) {//图片
                cover = mouth + "/" + realFileName.replace(".", "_.");
                String coverPath = targetFolderName + "/" + cover;
                //创建封面
                Boolean created = ScaleFilter.createThumbnailWidthFFmpeg(new File(targetFilePath), Constants.LENGTH_150, new File(coverPath), false);
                if (!created) {
                    FileUtils.copyFile(new File(targetFilePath), new File(coverPath));
                }
            }
        } catch (Exception e) {
            log.error("文件转码失败,fileId:{},userId:{}", fileId, webUserDTO.getUserId(), e);
            transferSuccess = false;
        } finally {
            FileInfo updateFIleInfo = new FileInfo();
            updateFIleInfo.setFileCover(cover)
                    .setStatus(transferSuccess ? FileStatusEnums.USING.getStatus() : FileStatusEnums.TRANSFER_FAIL.getStatus());
            fileInfoMapper.updateFileStatusWithOldStatus(updateFIleInfo, fileId, webUserDTO.getUserId(), FileStatusEnums.TRANSFER.getStatus());
        }
    }

    /**
     * 合并文件
     *
     * @param tempPath   存储临时分片的目录
     * @param toFilePath 合并文件之后存储文件的目录
     * @param fileName   文件名
     * @param delSource  是否删除临时文件
     */
    private void union(String tempPath, String toFilePath, String fileName, boolean delSource) {
        File tempFolder = new File(tempPath);
        if (!tempFolder.exists()) {
            throw new BusinessException("文件不存在");
        }
        //从临时目录中获取分片文件列表
        File[] listFile = tempFolder.listFiles();

        //准备目标文件
        File targetFile = new File(toFilePath);

        //创建缓冲区
        byte[] bytes = new byte[1024 * 10];

        //合并分片
        /*
         * RandomAccessFile 类属于 java.io 包。这个类允许对文件的随机访问（即，既可以读取文件，也可以写入文件，并且可以从文件的任何位置开始访问数据）。
         * RandomAccessFile 不同于 FileInputStream 和 FileOutputStream，后两者分别只支持读取和写入操作，并且只能从文件的开头或末尾开始访问数据。
         * 主要特点
         *   随机访问：你可以通过指定文件的偏移量来读取或写入数据，这允许你跳到文件的任何位置。
         *   同时读写：与 FileInputStream 和 FileOutputStream 不同，RandomAccessFile 可以在同一个文件中同时进行读取和写入操作。
         *   文件模式：在创建 RandomAccessFile 对象时，你需要指定一个模式字符串（"r" 表示只读，"rw" 表示读写）。如果文件不存在且模式为 "rw"，则会创建该文件。
         *   长度和位置：你可以获取文件的当前长度（以字节为单位），也可以获取和设置文件的读取/写入指针的当前位置。
         * 构造函数:RandomAccessFile(File file, String mode) throws FileNotFoundException
         *   file：要访问的 File 对象。
         *   mode：文件模式，可以是 "r"（只读）或 "rw"（读写）。
         * 常用方法
         *   读取方法：
         *       read()：读取并返回文件的下一个字节。
         *       read(byte[] b)：从文件中读取一些字节数，并将它们存储到字节数组 b 中。
         *       read(byte[] b, int off, int len)：从文件中读取最多 len 个字节的数据，并将它们存储到字节数组 b 中，从索引 off 开始。
         *   写入方法：
         *       write(int b)：写入指定的字节到文件。
         *       write(byte[] b)：将 b.length 个字节从指定的字节数组写入此文件。
         *       write(byte[] b, int off, int len)：从指定的字节数组写入 len 个字节到文件，从偏移量 off 处开始。
         *   文件长度操作：
         *       length()：返回此文件的长度（以字节为单位）。
         *   关闭文件：
         *       close()：关闭此文件流并释放与此流相关联的所有系统资源。
         *
         * */
        try (RandomAccessFile writeFile = new RandomAccessFile(targetFile, "rw")) {
            for (int i = 0; i < Objects.requireNonNull(listFile).length; i++) {
                int len = -1;
                try (RandomAccessFile readFile = new RandomAccessFile(new File(tempPath + "/" + i), "r")) {
                    while (//从readFile指向的分片文件中读取数据到bytes数组中。它返回读取的字节数
                            (len = readFile.read(bytes)) != -1
                    ) {
                        //将bytes数组中的前len个字节写入writeFile指向的目标文件中。这里0是偏移量，表示从bytes数组的起始位置开始写入
                        writeFile.write(bytes, 0, len);
                    }
                } catch (Exception e) {
                    log.error("合并分片失败", e);
                    throw new BusinessException("合并分片失败", e);
                }
            }
        } catch (Exception e) {
            log.error("合并文件{}失败", fileName, e);
            throw new BusinessException("合并文件" + fileName + "失败", e);
        } finally {
            if (delSource && tempFolder.exists()) {
                try {
                    FileUtils.deleteDirectory(tempFolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 调用ffmpeg，将mp4视频文件切割为ts文件
     *
     * @param fileId        文件Id
     * @param videoFilePath 视频文件存放路径
     */
    private void cutFile4Video(String fileId, String videoFilePath) {
        File tsFolder = new File(videoFilePath.substring(0, videoFilePath.lastIndexOf(".")));
        if (!tsFolder.exists()) {
            tsFolder.mkdirs();
        }
        final String CMD_TRANSFER_2TS = "ffmpeg -y -i %s  -vcodec copy -acodec copy -vbsf h264_mp4toannexb %s";//这里-vbsf好像会报错，我改成-bsf:v
        final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";

        String tsPath = tsFolder + "/" + Constants.TS_NAME;
        //生成index.ts
        String cmd = String.format(CMD_TRANSFER_2TS, videoFilePath, tsPath);
        ProcessUtils.executeCommand(cmd, false);
        //生成索引文件.m3u8 和切片.ts
        cmd = String.format(CMD_CUT_TS, tsPath, tsFolder.getPath() + "/" + Constants.M3U8_NAME, tsFolder.getPath(), fileId);
        ProcessUtils.executeCommand(cmd, false);
        //删除index.ts
        new File(tsPath).delete();
    }

}