package com.litepan.controller;

import com.litepan.annotation.GlobalInterceptor;
import com.litepan.annotation.VerifyParam;
import com.litepan.entity.config.AppConfig;
import com.litepan.entity.dto.SessionWebUserDTO;
import com.litepan.entity.dto.SysSettingDTO;
import com.litepan.entity.po.FileInfo;
import com.litepan.entity.po.UserInfo;
import com.litepan.entity.query.FileInfoQuery;
import com.litepan.entity.query.UserInfoQuery;
import com.litepan.entity.vo.FileInfoVO;
import com.litepan.entity.vo.PaginationResultVO;
import com.litepan.entity.vo.ResponseVO;
import com.litepan.entity.vo.UserInfoVO;
import com.litepan.service.FileInfoService;
import com.litepan.service.UserInfoService;
import com.litepan.utils.RedisComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.List;


/**
 * @Description: 管理员Controller
 * @date: 2024/07/09
 */
@RestController
@Slf4j
@RequestMapping("/admin")
public class AdminController extends CommentFileController {

    @Resource
    private UserInfoService userInfoService;
    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private AppConfig appConfig;
    @Resource
    private RedisComponent redisComponent;

    @PostMapping("/loadFileList")
    @GlobalInterceptor(checkAdmin = true, checkParams = true)
    public ResponseVO<PaginationResultVO<FileInfo>> loadFileList(@VerifyParam FileInfoQuery fileInfoQuery) {
        fileInfoQuery.setOrderBy("last_update_time desc");
        fileInfoQuery.setQueryNickName(true);
        PaginationResultVO<FileInfo> resultVO = fileInfoService.findListByPage(fileInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 获取系统设置
     */
    @PostMapping("/getSysSettings")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO<SysSettingDTO> getSysSettings() {
        return getSuccessResponseVO(redisComponent.getSysSettingDTO());
    }

    /**
     * 修改系统设置
     */
    @PostMapping("/saveSysSettings")
    @GlobalInterceptor(checkAdmin = true, checkParams = true)
    public ResponseVO<Object> saveSysSettings(@VerifyParam(required = true) String registerEmailTitle,
                                              @VerifyParam(required = true) String registerEmailContent,
                                              @VerifyParam(required = true) Integer userInitUseSpace) {
        SysSettingDTO sysSettingDTO = new SysSettingDTO();
        sysSettingDTO.setRegisterEmailTitle(registerEmailTitle);
        sysSettingDTO.setRegisterEmailContent(registerEmailContent);
        sysSettingDTO.setUserInitUseSpace(userInitUseSpace);
        redisComponent.saveSysSettingDTO(sysSettingDTO);
        return getSuccessResponseVO(null);
    }

    /**
     * 加载用户列表
     */
    @PostMapping("/loadUserList")
    @GlobalInterceptor(checkAdmin = true, checkParams = true)
    public ResponseVO<PaginationResultVO<UserInfoVO>> loadUserList(@VerifyParam(required = true) UserInfoQuery query) {
        query.setOrderBy("join_time desc");
        PaginationResultVO<UserInfo> resultVO = userInfoService.findListByPage(query);
        return getSuccessResponseVO(convert2PaginationVO(resultVO, UserInfoVO.class));
    }

    /**
     * 修改用户状态
     */
    @PostMapping("/updateUserStatus")
    @GlobalInterceptor(checkAdmin = true, checkParams = true)
    public ResponseVO<Object> updateUserStatus(@VerifyParam(required = true) String userId,
                                               @VerifyParam(required = true) Integer status) {
        userInfoService.updateUserStatus(userId, status);
        return getSuccessResponseVO(null);
    }

    /**
     * 修改用户状态
     */
    @PostMapping("/updateUserSpace")
    @GlobalInterceptor(checkAdmin = true, checkParams = true)
    public ResponseVO<Object> updateUserSpace(@VerifyParam(required = true) String userId,
                                              @VerifyParam(required = true) Long changeSpace) {
        userInfoService.changeUserSpace(userId, changeSpace);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/getFolderInfo")
    @GlobalInterceptor(checkAdmin = true, checkParams = true)
    public ResponseVO<List<FileInfoVO>> getFolderInfo(@VerifyParam(required = true) String path) {
        return super.getFolderInfo(path, null);
    }

    /**
     * 视频预览播放
     *
     * @param fileId 要播放的文件Id
     */
    @GetMapping("/ts/getVideoInfo/{userId}/{fileId}")
    @GlobalInterceptor(checkAdmin = true, checkParams = true)
    public void getVideoInfo(HttpServletResponse response,
                             @PathVariable("userId") @VerifyParam(required = true) String userId,
                             @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
        super.getFile(response, fileId, userId);
    }

    @RequestMapping("/getFile/{userId}/{fileId}")
    @GlobalInterceptor(checkAdmin = true, checkParams = true)
    public void getFile(HttpServletResponse response,
                        @PathVariable("fileId") @VerifyParam(required = true) String fileId,
                        @PathVariable("userId") @VerifyParam(required = true) String userId) {
        super.getFile(response, fileId, userId);
    }

    /**
     * 创建一个下载code返回给前端，并不真正下载
     */
    @PostMapping("/createDownloadUrl/{userId}/{fileId}")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO<String> createDownloadUrl(@VerifyParam(required = true) @PathVariable("userId") String userId,
                                                @VerifyParam(required = true) @PathVariable("fileId") String fileId) {
        return super.createDownloadUrl(fileId, userId);
    }

    /**
     * 通过code获取要下载的文件信息，然后下载文件
     *
     * @throws UnsupportedEncodingException 编码方式不支持异常
     */
    @GetMapping("/download/{code}")
    @GlobalInterceptor(checkParams = true, checkLogin = false, checkAdmin = true)
    public void download(HttpServletResponse response, HttpServletRequest request,
                         @VerifyParam(required = true) @PathVariable("code") String code) throws UnsupportedEncodingException {
        super.download(request, response, code);
    }

    @PostMapping("/delFile")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO<Object> delFile(@VerifyParam(required = true) String fileIdAndUserIds) {
        String[] split = fileIdAndUserIds.split(",");
        for (String s : split) {
            String[] strings = s.split("_");
            fileInfoService.delFileBatch(strings[0], strings[1], true);
        }
        return getSuccessResponseVO(null);
    }

}