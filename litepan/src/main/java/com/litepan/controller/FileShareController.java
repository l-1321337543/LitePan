package com.litepan.controller;

import com.litepan.annotation.GlobalInterceptor;
import com.litepan.annotation.VerifyParam;
import com.litepan.entity.dto.SessionWebUserDTO;
import com.litepan.entity.vo.PaginationResultVO;
import com.litepan.service.FileShareService;
import com.litepan.entity.query.FileShareQuery;
import com.litepan.entity.vo.ResponseVO;
import com.litepan.entity.po.FileShare;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;


/**
 * @Description: 分享信息表Controller
 * @date: 2024/08/01
 */
@RestController
@RequestMapping("/share")
public class FileShareController extends ABaseController {

    @Resource
    private FileShareService fileShareService;

    /**
     * 加载分享文件列表
     */
    @PostMapping("/loadShareList")
    @GlobalInterceptor
    public ResponseVO<PaginationResultVO<FileShare>> loadDataList(HttpSession session, Integer pageNo, Integer pageSize) {
        FileShareQuery fileShareQuery = new FileShareQuery();
        fileShareQuery.setPageNo(pageNo);
        fileShareQuery.setPageSize(pageSize);
        fileShareQuery.setUserId(getUserInfoFromSession(session).getUserId());
        fileShareQuery.setOrderBy("share_time desc");
        fileShareQuery.setQueryFileName(true);
        PaginationResultVO<FileShare> resultVO = fileShareService.findListByPage(fileShareQuery);
        return getSuccessResponseVO(resultVO);
    }

    @PostMapping("/shareFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO<FileShare> shareFile(HttpSession session,
                                           @VerifyParam(required = true) String fileId,
                                           @VerifyParam(required = true) Integer validType,
                                           String code) {
        SessionWebUserDTO webUserDTO = getUserInfoFromSession(session);
        FileShare fileShare = new FileShare();
        fileShare.setCode(code);
        fileShare.setFileId(fileId);
        fileShare.setValidType(validType);
        fileShare.setUserId(webUserDTO.getUserId());
        fileShareService.saveShareInfo(fileShare);
        return getSuccessResponseVO(fileShare);
    }

    @PostMapping("/cancelShare")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO<Object> cancelShare(HttpSession session,
                                             @VerifyParam(required = true) String shareIds) {
        SessionWebUserDTO webUserDTO = getUserInfoFromSession(session);
        fileShareService.delFileShareBatch(webUserDTO.getUserId(), shareIds);
        return getSuccessResponseVO(null);
    }


}