package com.litepan.controller;

import com.litepan.annotation.GlobalInterceptor;
import com.litepan.annotation.VerifyParam;
import com.litepan.entity.constants.Constants;
import com.litepan.entity.dto.SessionShareDTO;
import com.litepan.entity.dto.SessionWebUserDTO;
import com.litepan.entity.po.FileInfo;
import com.litepan.entity.po.FileShare;
import com.litepan.entity.po.UserInfo;
import com.litepan.entity.query.FileInfoQuery;
import com.litepan.entity.vo.*;
import com.litepan.enums.FileDelFlagEnums;
import com.litepan.enums.ResponseCodeEnum;
import com.litepan.exception.BusinessException;
import com.litepan.service.FileInfoService;
import com.litepan.service.FileShareService;
import com.litepan.service.UserInfoService;
import com.litepan.utils.CopyUtils;
import com.litepan.utils.StringTools;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

/**
 * @author 李臣洋
 * @version 1.0
 * @description: 外部分享
 * @date 2024/8/5 21:22
 */
@RestController
@RequestMapping("/showShare")
public class WebShareController extends CommentFileController {

    @Resource
    private FileShareService fileShareService;

    @Resource
    private FileInfoService fileInfoService;

    @Resource
    private UserInfoService userInfoService;

    /**
     * 获取用户登录信息
     */
    @PostMapping("/getShareLoginInfo")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public ResponseVO<ShareInfoVO> getShareLoginInfo(HttpSession session,
                                                     @VerifyParam(required = true) String shareId) {
        SessionShareDTO shareDTO = getSessionShareFromSession(session, shareId);
        if (shareDTO == null) {
            return getSuccessResponseVO(null);
        }
        ShareInfoVO shareInfoVO = getShareInfoCommon(shareId);
        SessionWebUserDTO webUserDTO = getUserInfoFromSession(session);
        if (webUserDTO == null) {
            shareInfoVO.setCurrentUser(false);
        } else {
            shareInfoVO.setCurrentUser(webUserDTO.getUserId() != null && webUserDTO.getUserId().equals(shareDTO.getShareUserId()));
        }
        return getSuccessResponseVO(shareInfoVO);
    }

    /**
     * 获取分享信息
     */
    @PostMapping("/getShareInfo")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public ResponseVO<ShareInfoVO> getShareInfo(@VerifyParam(required = true) String shareId) {
        ShareInfoVO shareInfoVO = getShareInfoCommon(shareId);
        return getSuccessResponseVO(shareInfoVO);
    }

    /**
     * 校验提取码
     *
     * @param shareId 分享Id
     * @param code    提取码
     */
    @PostMapping("/checkShareCode")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public ResponseVO<Object> checkShareCode(HttpSession session,
                                             @VerifyParam(required = true) String shareId,
                                             @VerifyParam(required = true) String code) {
        SessionShareDTO sessionShareDTO = fileShareService.checkShareCode(shareId, code);
        session.setAttribute(Constants.SESSION_SHARE_KEY + shareId, sessionShareDTO);
        return getSuccessResponseVO(null);
    }


    /**
     * 获取分享的文件列表
     *
     * @param shareId 分享Id
     */
    @PostMapping("/loadFileList")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public ResponseVO<PaginationResultVO<FileInfoVO>> loadFileList(HttpSession session,
                                                                   @VerifyParam(required = true) String shareId,
                                                                   @VerifyParam(required = true) String filePid) {
        SessionShareDTO sessionShareDTO = checkShare(session, shareId);
        FileInfoQuery query = new FileInfoQuery();
        if (!StringTools.isEmpty(filePid) && !filePid.equals(Constants.ZERO_STR)) {
            fileShareService.checkRootFilePid(sessionShareDTO.getShareUserId(), sessionShareDTO.getFileId(), filePid);
            query.setFilePid(filePid);
        } else {
            query.setFileId(sessionShareDTO.getFileId());
        }
        query.setOrderBy("last_update_time desc");
        query.setUserId(sessionShareDTO.getShareUserId());
        query.setDelFlag(FileDelFlagEnums.NORMAL.getFlag());
        PaginationResultVO<FileInfo> resultVO = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(convert2PaginationVO(resultVO, FileInfoVO.class));
    }

    /**
     * 加载文件列表
     *
     * @param shareId 分享Id
     */
    @PostMapping("/getFolderInfo")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public ResponseVO<List<FolderVO>> getFolderInfo(HttpSession session,
                                                    @VerifyParam(required = true) String shareId,
                                                    @VerifyParam(required = true) String path) {
        SessionShareDTO sessionShareDTO = checkShare(session, shareId);
        return super.getFolderInfo(path, sessionShareDTO.getShareUserId());
    }


    /**
     * 预览文件
     */
    @PostMapping("/getFile/{shareId}/{fileId}")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public void getFile(HttpSession session,
                        HttpServletResponse response,
                        @PathVariable("shareId") @VerifyParam(required = true) String shareId,
                        @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
        SessionShareDTO sessionShareDTO = checkShare(session, shareId);
        super.getFile(response, fileId, sessionShareDTO.getShareUserId());
    }

    /**
     * 视频预览播放
     */
    @GetMapping("/ts/getVideoInfo/{shareId}/{fileId}")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public void getVideoInfo(HttpServletResponse response,
                             HttpSession session,
                             @PathVariable("shareId") @VerifyParam(required = true) String shareId,
                             @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
        SessionShareDTO shareDTO = checkShare(session, shareId);
        super.getFile(response, fileId, shareDTO.getShareUserId());
    }

    /**
     * 创建一个下载code返回给前端，并不真正下载
     */
    @PostMapping("/createDownloadUrl/{shareId}/{fileId}")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public ResponseVO<String> createDownloadUrl(HttpSession session,
                                                @VerifyParam(required = true) @PathVariable("shareId") String shareId,
                                                @VerifyParam(required = true) @PathVariable("fileId") String fileId) {
        SessionShareDTO shareDTO = checkShare(session, shareId);
        return super.createDownloadUrl(fileId, shareDTO.getShareUserId());
    }

    /**
     * 通过code获取要下载的文件信息，然后下载文件
     *
     * @throws UnsupportedEncodingException 编码方式不支持异常
     */
    @GetMapping("/download/{code}")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public void download(HttpServletResponse response, HttpServletRequest request,
                         @VerifyParam(required = true) @PathVariable("code") String code) throws UnsupportedEncodingException {
        super.download(request, response, code);
    }

    /**
     * 保存到我的网盘
     */
    @PostMapping("/saveShare")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO<Object> saveShare(HttpSession session,
                          @VerifyParam(required = true) String shareId,
                          @VerifyParam(required = true) String shareFileIds,
                          @VerifyParam(required = true) String myFolderId) {
        SessionShareDTO sessionShareDTO = checkShare(session, shareId);
        SessionWebUserDTO webUserDTO = getUserInfoFromSession(session);
        if (sessionShareDTO.getShareUserId().equals(webUserDTO.getUserId())) {
            throw new BusinessException("自己分享的文件不能保存到自己的网盘");
        }
        fileShareService.saveShare(webUserDTO.getUserId(), sessionShareDTO.getShareUserId(), null, shareFileIds, myFolderId);

        return getSuccessResponseVO(null);
    }


    private ShareInfoVO getShareInfoCommon(String shareId) {
        FileShare fileShare = fileShareService.getFileShareByShareId(shareId);
        if (fileShare == null || (fileShare.getExpireTime() != null && new Date().after(fileShare.getExpireTime()))) {
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        ShareInfoVO shareInfoVO = CopyUtils.copy(fileShare, ShareInfoVO.class);
        FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileShare.getFileId(), fileShare.getUserId());
        UserInfo userInfo = userInfoService.getUserInfoByUserId(fileShare.getUserId());
        shareInfoVO.setUserId(userInfo.getUserId());
        shareInfoVO.setFileName(fileInfo.getFileName());
        shareInfoVO.setFileId(fileShare.getFileId());
        shareInfoVO.setNickName(userInfo.getNickName());
        shareInfoVO.setAvatar(userInfo.getQqAvatar());
        return shareInfoVO;
    }

    private SessionShareDTO checkShare(HttpSession session, String shareId) {
        SessionShareDTO sessionShareDTO = getSessionShareFromSession(session, shareId);
        if (sessionShareDTO == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_903);
        }
        if (sessionShareDTO.getExpireTime() != null && new Date().after(sessionShareDTO.getExpireTime())) {
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        return sessionShareDTO;
    }
}
