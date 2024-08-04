package com.litepan.controller;

import com.litepan.annotation.GlobalInterceptor;
import com.litepan.annotation.VerifyParam;
import com.litepan.entity.dto.SessionWebUserDTO;
import com.litepan.entity.po.FileInfo;
import com.litepan.entity.query.FileInfoQuery;
import com.litepan.entity.vo.FileInfoVO;
import com.litepan.entity.vo.PaginationResultVO;
import com.litepan.entity.vo.ResponseVO;
import com.litepan.enums.FileDelFlagEnums;
import com.litepan.service.FileInfoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * @author 李臣洋
 * @version 1.0
 * @description: 回收站controller
 * @date 2024/8/1 8:46
 */
@RestController
@RequestMapping("/recycle")
public class RecycleController extends ABaseController {
    @Resource
    private FileInfoService fileInfoService;

    /**
     * 获取回收站的文件列表
     */
    @PostMapping("/loadRecycleList")
    @GlobalInterceptor
    public ResponseVO<PaginationResultVO<FileInfoVO>> loadRecycleList(HttpSession session, Integer pageNo, Integer pageSize) {
        FileInfoQuery query = new FileInfoQuery();
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        query.setUserId(getUserInfoFromSession(session).getUserId());
        query.setOrderBy("recover_time desc");
        query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());

        // 获取封装好的分页结果
        PaginationResultVO<FileInfo> result = fileInfoService.findListByPage(query);

        // 返回转换类型后的分页结果
        return getSuccessResponseVO(convert2PaginationVO(result, FileInfoVO.class));
    }

    /**
     * 将回收站选中的文件恢复
     *
     * @param fileIds 文件Id
     */
    @PostMapping("/recoverFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO<Object> recoverFile(HttpSession session, @VerifyParam(required = true) String fileIds) {
        fileInfoService.recoverFileBatch(getUserInfoFromSession(session).getUserId(), fileIds);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/delFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO<Object> delFile(HttpSession session, @VerifyParam(required = true) String fileIds) {
        SessionWebUserDTO webUserDTO = getUserInfoFromSession(session);
        fileInfoService.delFileBatch(webUserDTO.getUserId(), fileIds, false);
        return getSuccessResponseVO(null);
    }

}
