package com.litepan.controller;

import com.litepan.entity.constants.Constants;
import com.litepan.entity.dto.SessionShareDTO;
import com.litepan.entity.dto.SessionWebUserDTO;
import com.litepan.entity.vo.PaginationResultVO;
import com.litepan.enums.ResponseCodeEnum;
import com.litepan.entity.vo.ResponseVO;
import com.litepan.exception.BusinessException;
import com.litepan.utils.CopyUtils;
import com.litepan.utils.StringTools;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Slf4j
public class ABaseController {
    protected static final String STATUS_SUCCESS = "success";
    protected static final String STATUS_ERROR = "error";


    protected <T> ResponseVO<T> getSuccessResponseVO(T t) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUS_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    protected <T> ResponseVO<T> getBusinessErrorResponseVO(BusinessException e, T t) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setStatus(STATUS_ERROR);
        if (e.getCode() == null) {
            vo.setCode(ResponseCodeEnum.CODE_600.getCode());
        } else {
            vo.setCode(e.getCode());
        }
        vo.setInfo(e.getMessage());
        vo.setData(t);
        return vo;
    }

    protected <T> ResponseVO<T> getServerErrorResponseVO(BusinessException e, T t) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setStatus(STATUS_ERROR);
        vo.setCode(ResponseCodeEnum.CODE_500.getCode());
        vo.setInfo(ResponseCodeEnum.CODE_500.getMsg());
        vo.setData(t);
        return vo;
    }

    /**
     * 读取传入的文件路径的文件中的内容，并响应到前端
     */
    protected void readFile(HttpServletResponse response, String filePath) {
        if (!StringTools.pathIsOk(filePath)) {
            return;
        }
        OutputStream out = null;
        FileInputStream in = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            in = new FileInputStream(file);
            byte[] byteData = new byte[1024];
            out = response.getOutputStream();
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            log.error("读取文件异常", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("IO异常", e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("IO异常", e);
                }
            }
        }
    }

    /**
     * 将PaginationResultVO<S>中的类型S转换为类型T
     *
     * @param result 原始PaginationResultVO
     * @param clazz  目标PaginationResultVO中的类型
     * @param <S>    源类型
     * @param <T>    目标类型
     * @return 转换后的PaginationResultVO<T>
     */
    protected <S, T> PaginationResultVO<T> convert2PaginationVO(PaginationResultVO<S> result, Class<T> clazz) {
        PaginationResultVO<T> resultVO = new PaginationResultVO<>();
        resultVO.setList(CopyUtils.copyList(result.getList(), clazz));
        resultVO.setPageNo(result.getPageNo());
        resultVO.setPageSize(result.getPageSize());
        resultVO.setPageTotal(result.getPageTotal());
        resultVO.setTotalCount(result.getTotalCount());
        return resultVO;
    }

    /**
     * 获取用户信息，从session中拿到登陆时存入的用户信息
     */
    protected SessionWebUserDTO getUserInfoFromSession(HttpSession session) {
        return (SessionWebUserDTO) session.getAttribute(Constants.SESSION_KEY);
    }

    protected SessionShareDTO getSessionShareFromSession(HttpSession session, String shareId) {
        return (SessionShareDTO)
                session.getAttribute(Constants.SESSION_SHARE_KEY + shareId);
    }
}
