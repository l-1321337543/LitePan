package com.litepan.controller;

import com.litepan.entity.config.AppConfig;
import com.litepan.entity.constants.Constants;
import com.litepan.entity.po.FileInfo;
import com.litepan.enums.FileCategoryEnums;
import com.litepan.service.FileInfoService;
import com.litepan.utils.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

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

    /**
     * 响应图片
     *
     * @param imageFolder 图片文件夹
     * @param imageName   图片名
     */
    protected void getImage(HttpServletResponse response, String imageFolder, String imageName) {
        if (StringUtils.isEmpty(imageFolder) || StringUtils.isEmpty(imageName)
                || !StringUtils.pathIsOk(imageFolder) || !StringUtils.pathIsOk(imageName)) {
            return;
        }
        String projectFolder = appConfig.getProjectFolder();
        String imagePath = projectFolder + Constants.FILE_FOLDER_FILE + imageFolder + "/" + imageName;
        String imageSuffix = StringUtils.getFileSuffix(imageName).replace(".", "");
        String contentType = "image/" + imageSuffix;
        response.setContentType(contentType);
        response.setHeader("Cache-Control", "max-age=2592000");
        readFile(response, imagePath);
    }

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
            filePath = StringUtils.getFileNameNoSuffix(dbFilePath) + "/" + fileName;
        } else {//预览其他文件
            FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId, userId);
            if (fileInfo == null) {
                return;
            }
            if (fileInfo.getFileCategory().equals(FileCategoryEnums.VIDEO.getCategory())) {//预览视频的m3u8文件

                //数据库文件的绝对路径
                String dbFilePath = fileFolderPath + fileInfo.getFilePath();

                //要响应的文件的路径
                filePath = StringUtils.getFileNameNoSuffix(dbFilePath) + "/" + Constants.M3U8_NAME;
                readFile(response, filePath);
            }else {
                //要响应的文件的路径
                filePath = fileFolderPath + fileInfo.getFilePath();
            }

        }
        readFile(response, filePath);
    }
}
