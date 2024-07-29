package com.litepan.controller;

import com.litepan.entity.config.AppConfig;
import com.litepan.entity.constants.Constants;
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

    /**
     * 响应图片
     *
     * @param imageFolder 图片文件夹
     * @param imageName   图片名
     */
    public void getImage(HttpServletResponse response, String imageFolder, String imageName) {
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
}
