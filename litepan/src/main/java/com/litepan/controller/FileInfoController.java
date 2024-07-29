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
     * 根据条件分页查询
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
     * 响应图片
     *
     * @param imageFolder 图片文件夹
     * @param imageName   图片名
     */
    @GetMapping("/getImage/{imageFolder}/{imageName}")
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
    public void getVideoInfo(HttpServletResponse response,
                             HttpSession session,
                             @PathVariable("fileId") String fileId) {
        SessionWebUserDTO webUserDTO = getUserInfoFromSession(session);
        super.getFile(response, fileId, webUserDTO.getUserId());
    }

    @RequestMapping("/getFile/{fileId}")
    public void getFile(HttpServletResponse response,
                             HttpSession session,
                             @PathVariable("fileId") String fileId) {
        SessionWebUserDTO webUserDTO = getUserInfoFromSession(session);
        super.getFile(response, fileId, webUserDTO.getUserId());
    }

}