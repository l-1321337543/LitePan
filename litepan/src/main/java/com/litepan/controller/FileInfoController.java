package com.litepan.controller;

import com.litepan.entity.vo.PaginationResultVO;
import com.litepan.service.FileInfoService;
import com.litepan.entity.query.FileInfoQuery;
import com.litepan.entity.vo.ResponseVO;
import com.litepan.entity.po.FileInfo;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.List;


/**
 * @Description: 文件信息表Controller
 * @date: 2024/07/26
 */
@RestController
@RequestMapping("/file")
public class FileInfoController extends ABaseController {

    @Resource
    private FileInfoService fileInfoService;

    /**
     * 根据条件分页查询
     */
    @RequestMapping("loadDataList")
    public ResponseVO<PaginationResultVO<FileInfo>> loadDataList(FileInfoQuery query) {
        return getSuccessResponseVO(fileInfoService.findListByPage(query));
    }


}