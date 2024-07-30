package com.litepan.controller;

import com.litepan.entity.config.AppConfig;
import com.litepan.entity.constants.Constants;
import com.litepan.entity.dto.DownloadFileDTO;
import com.litepan.entity.po.FileInfo;
import com.litepan.entity.query.FileInfoQuery;
import com.litepan.entity.vo.FileInfoVO;
import com.litepan.entity.vo.ResponseVO;
import com.litepan.enums.FileCategoryEnums;
import com.litepan.enums.FileDelFlagEnums;
import com.litepan.enums.FileFolderTypeEnums;
import com.litepan.enums.ResponseCodeEnum;
import com.litepan.exception.BusinessException;
import com.litepan.service.FileInfoService;
import com.litepan.utils.CopyUtils;
import com.litepan.utils.RedisComponent;
import com.litepan.utils.StringTools;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author 李臣洋
 * @version 1.0
 * @description: 文件读写
 * @date 2024/7/29 16:43
 */
public class CommentFileController extends ABaseController {

    @Resource
    private AppConfig appConfig;

    @Resource
    private FileInfoService fileInfoService;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 响应图片
     *
     * @param imageFolder 图片文件夹
     * @param imageName   图片名
     */
    protected void getImage(HttpServletResponse response, String imageFolder, String imageName) {
        if (StringTools.isEmpty(imageFolder) || StringTools.isEmpty(imageName)
                || !StringTools.pathIsOk(imageFolder) || !StringTools.pathIsOk(imageName)) {
            return;
        }
        String projectFolder = appConfig.getProjectFolder();
        String imagePath = projectFolder + Constants.FILE_FOLDER_FILE + imageFolder + "/" + imageName;
        String imageSuffix = StringTools.getFileSuffix(imageName).replace(".", "");
        String contentType = "image/" + imageSuffix;
        response.setContentType(contentType);
        response.setHeader("Cache-Control", "max-age=2592000");
        readFile(response, imagePath);
    }


    /**
     * 预览存入云盘的文件
     *
     * @param fileId 文件Id
     * @param userId 用户Id
     */
    protected void getFile(HttpServletResponse response, String fileId, String userId) {

        // D:\LitePan\file\
        String fileFolderPath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;

        //要响应的文件的路径
        String filePath = null;

        if (fileId.endsWith(".ts")) {//预览视频
            String fileName = fileId;
            //从xxx_0000.ts中截取出fileId
            fileId = fileId.substring(0, fileId.lastIndexOf("_"));
            FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId, userId);
            if (fileInfo == null) {
                return;
            }
            //数据库文件的绝对路径
            String dbFilePath = fileFolderPath + fileInfo.getFilePath();

            //要响应的文件的路径
            filePath = StringTools.getFileNameNoSuffix(dbFilePath) + "/" + fileName;
        } else {//预览其他文件
            FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId, userId);
            if (fileInfo == null) {
                return;
            }
            if (fileInfo.getFileCategory().equals(FileCategoryEnums.VIDEO.getCategory())) {//预览视频的m3u8文件

                //数据库文件的绝对路径
                String dbFilePath = fileFolderPath + fileInfo.getFilePath();

                //要响应的文件的路径
                filePath = StringTools.getFileNameNoSuffix(dbFilePath) + "/" + Constants.M3U8_NAME;
                readFile(response, filePath);
            } else {
                //要响应的文件的路径
                filePath = fileFolderPath + fileInfo.getFilePath();
            }

        }
        readFile(response, filePath);
    }

    /**
     * 获取该文件夹的层级目录关系
     *
     * @param path   该文件夹以及父级文件的fileId，以"/"拼接
     * @param userId 用户Id
     * @return 该文件夹及其所有父级目录的列表
     */
    protected ResponseVO<List<FileInfoVO>> getFolderInfo(String path, String userId) {
        //从path中分离出所有的fileId
        String[] fileIdArray = path.split("/");
        //设置好query属性
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setFileIdArray(fileIdArray);
        fileInfoQuery.setFolderType(FileFolderTypeEnums.FOLDER.getType());
        fileInfoQuery.setDelFlag(FileDelFlagEnums.NORMAL.getFlag());
        //此处设置排序规则
        String orderBy = "field(file_id,\"" + StringUtils.join(fileIdArray, "\",\"") + "\")";
        fileInfoQuery.setOrderBy(orderBy);
        //查询符合条件的文件夹列表
        List<FileInfo> fileInfoList = fileInfoService.findListByParam(fileInfoQuery);

        return getSuccessResponseVO(CopyUtils.copyList(fileInfoList, FileInfoVO.class));
    }

    /**
     * 将要下载的文件信息保存在DownloadFileDTO中，生成一个50位的code<br/>
     * 将code作为key，DownloadFileDTO作为value存入缓存<br/>
     * 并返给前端一个下载code
     *
     * @param fileId 文件Id
     * @param userId 用户Id
     * @return 50位随机code
     */
    protected ResponseVO<String> createDownloadUrl(String fileId, String userId) {
        FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId, userId);
        if (fileInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (fileInfo.getFolderType().equals(FileFolderTypeEnums.FOLDER.getType())) {
            //文件夹不可下载
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String code = StringTools.getRandomString(Constants.LENGTH_50);
        DownloadFileDTO downloadFileDTO = new DownloadFileDTO();
        downloadFileDTO.setDownloadCode(code);
        downloadFileDTO.setFileName(fileInfo.getFileName());
        downloadFileDTO.setFilePath(fileInfo.getFilePath());

        //将code作为key，DownloadFileDTO作为value存入缓存
        redisComponent.saveDownloadCode(code, downloadFileDTO);

        return getSuccessResponseVO(code);
    }

    /**
     * 通过code获取缓存中DownloadFileDTO的文件信息，以此来下载文件
     *
     * @param code 前端传入的下载code
     * @throws UnsupportedEncodingException 不支持的编码异常
     */
    protected void download(HttpServletRequest request, HttpServletResponse response, String code) throws UnsupportedEncodingException {
        DownloadFileDTO downloadFileDto = redisComponent.getDownloadDTO(code);
        if (downloadFileDto == null) {
            return;
        }
        String filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + downloadFileDto.getFilePath();
        String fileName = downloadFileDto.getFileName();
        response.setContentType("application/x-msdownload; charset=UTF-8");
        if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0) {//IE浏览器
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } else {
            fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), "ISO8859-1");
        }
        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
        readFile(response, filePath);
    }


}
