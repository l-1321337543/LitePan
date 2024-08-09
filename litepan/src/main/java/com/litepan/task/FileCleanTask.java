package com.litepan.task;

import com.litepan.entity.po.FileInfo;
import com.litepan.entity.query.FileInfoQuery;
import com.litepan.enums.FileDelFlagEnums;
import com.litepan.service.FileInfoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 李臣洋
 * @version 1.0
 * @description: 定时清除回收站的文件
 * @date 2024/8/8 21:31
 */
@Component
public class FileCleanTask {
    @Resource
    private FileInfoService fileInfoService;

    @Scheduled(fixedDelay = 1000 * 60 * 3)
    private void execute() {
        FileInfoQuery query = new FileInfoQuery();
        query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
        query.setQueryExpire(true);
        List<FileInfo> recycleList = fileInfoService.findListByParam(query);
        Map<String, List<FileInfo>> recycleFileMap = recycleList.stream()
                .collect(Collectors.groupingBy(FileInfo::getFileId));
        for (Map.Entry<String, List<FileInfo>> entry : recycleFileMap.entrySet()) {
            List<String> fileIds = entry.getValue().stream()
                    .map(p -> p.getFileId()).collect(Collectors.toList());
            fileInfoService.delFileBatch(entry.getKey(), String.join(",", fileIds), false);
        }
    }
}
