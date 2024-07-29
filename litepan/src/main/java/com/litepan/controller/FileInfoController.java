package com.litepan.controller;

import com.litepan.annotation.GlobalInterceptor;
import com.litepan.annotation.VerifyParam;
import com.litepan.entity.dto.SessionWebUserDTO;
import com.litepan.entity.dto.UploadResultDTO;
import com.litepan.entity.vo.FileInfoVO;
import com.litepan.entity.vo.PaginationResultVO;
import com.litepan.enums.FileCategoryEnums;
import com.litepan.enums.FileDelFlagEnums;
import com.litepan.enums.FileStatusEnums;
import com.litepan.service.FileInfoService;
import com.litepan.entity.query.FileInfoQuery;
import com.litepan.entity.vo.ResponseVO;
import com.litepan.entity.po.FileInfo;
import com.sun.deploy.net.HttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;


/**
 * @Description: 文件信息表Controller
 * @date: 2024/07/26
 */
@RestController
@RequestMapping("/file")
public class FileInfoController extends CommentFileController {

    @Resource
    private FileInfoService fileInfoService;

    /**
     * 加载文件列表
     */
    @PostMapping("loadDataList")
    @GlobalInterceptor
    public ResponseVO<PaginationResultVO<FileInfoVO>> loadDataList(HttpSession session, FileInfoQuery query, String category) {
        // 通过枚举判断传入的category属于哪种类型
        FileCategoryEnums categoryEnums = FileCategoryEnums.getByCode(category);
        if (categoryEnums != null) {
            query.setFileCategory(categoryEnums.getCategory());
        }

        query.setUserId(getUserInfoFromSession(session).getUserId());
        query.setOrderBy("last_update_time desc");
        query.setDelFlag(FileDelFlagEnums.NORMAL.getFlag());

        // 获取封装好的分页结果
        PaginationResultVO<FileInfo> result = fileInfoService.findListByPage(query);

        // 返回转换类型后的分页结果
        return getSuccessResponseVO(convert2PaginationVO(result, FileInfoVO.class));
    }

    /**
     * 上传文件，前端将文件分块后，一块一块上传
     *
     * @param fileId     非必传，文件的第一块没有fileId
     * @param file       上传的文件本身
     * @param fileName   文件名
     * @param filePid    父级Id
     * @param fileMd5    文件MD5值
     * @param chunkIndex 分块索引
     * @param chunks     分块总数
     * @return 文件上传状态
     */
    @PostMapping("/uploadFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO<UploadResultDTO> uploadFile(HttpSession session,
                                                  String fileId,
                                                  MultipartFile file,
                                                  @VerifyParam(required = true) String fileName,
                                                  @VerifyParam(required = true) String filePid,
                                                  @VerifyParam(required = true) String fileMd5,
                                                  @VerifyParam(required = true) Integer chunkIndex,
                                                  @VerifyParam(required = true) Integer chunks) {
        SessionWebUserDTO webUserDTO = getUserInfoFromSession(session);
        UploadResultDTO uploadResultDTO = fileInfoService.uploadFile(webUserDTO, fileId, file,
                fileName, filePid, fileMd5, chunkIndex, chunks);
        return getSuccessResponseVO(uploadResultDTO);
    }

    /**
     * 响应缩略图
     *
     * @param imageFolder 图片文件夹
     * @param imageName   图片名
     */
    @GetMapping("/getImage/{imageFolder}/{imageName}")
    @GlobalInterceptor
    public void getImage(HttpServletResponse response,
                         @PathVariable("imageFolder") String imageFolder,
                         @PathVariable("imageName") String imageName) {
        super.getImage(response, imageFolder, imageName);
    }

    /**
     * 视频预览播放
     *
     * @param fileId 要播放的文件Id
     */
    @GetMapping("/ts/getVideoInfo/{fileId}")
    @GlobalInterceptor
    public void getVideoInfo(HttpServletResponse response,
                             HttpSession session,
                             @PathVariable("fileId") String fileId) {
        SessionWebUserDTO webUserDTO = getUserInfoFromSession(session);
        super.getFile(response, fileId, webUserDTO.getUserId());
    }

    /**
     * 预览除视频以外其他文件
     *
     * @param fileId 文件Id
     */
    @RequestMapping("/getFile/{fileId}")
    @GlobalInterceptor
    public void getFile(HttpServletResponse response,
                        HttpSession session,
                        @PathVariable("fileId") String fileId) {
        SessionWebUserDTO webUserDTO = getUserInfoFromSession(session);
        super.getFile(response, fileId, webUserDTO.getUserId());
    }

    /**
     * 新建文件夹
     *
     * @param fileName 文件夹名称
     * @param filePid  文件Pid
     * @return 新建好的FileInfo
     */
    @PostMapping("/newFoloder")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO<FileInfo> newFolder(HttpSession session,
                                          @VerifyParam(required = true) String fileName,
                                          @VerifyParam(required = true) String filePid) {
        SessionWebUserDTO webUserDTO = getUserInfoFromSession(session);
        FileInfo fileInfo = fileInfoService.newFolder(webUserDTO.getUserId(), fileName, filePid);
        return getSuccessResponseVO(fileInfo);
    }

    /**
     * 不是获取获取目录下的文件，而是获取该文件夹的层级目录关系
     *
     * @param path 该文件夹以及父级文件的fileId，以"/"拼接 <br>
     *             例如：path=P63epeXZzC/7mOpybEXR0/gHTP3Lb7yK
     * @return 该文件夹及其所有父级目录的列表
     */
    @PostMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO<List<FileInfo>> getFolderInfo(HttpSession session,
                                                    @VerifyParam(required = true) String path) {
        SessionWebUserDTO webUserDTO = getUserInfoFromSession(session);
        return super.getFolderInfo(path, webUserDTO.getUserId());
    }


}