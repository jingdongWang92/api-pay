package com.jcble.apipay.exception;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.jcble.apipay.dto.BaseResponse;
/**
 * <p>Description: 全局异常处理类 </p>
 * @author Jingdong Wang
 * @date 2017年11月6日 下午4:42:28
 *
 */
@ControllerAdvice
public class GlobalExceptionHandler {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@ExceptionHandler(value = Exception.class)
	@ResponseBody
	public BaseResponse handleException(HttpServletRequest req, Exception e) {
		e.printStackTrace();
		JSONObject json = new JSONObject();
        Enumeration<String> enu = req.getParameterNames();
        while (enu.hasMoreElements()) {
            String key = enu.nextElement();
            Object value = req.getParameter(key);
            json.put(key, value);
        }
        logger.error("GlobalExceptionHandler===>Host: {}", req.getRemoteHost());
        logger.error("GlobalExceptionHandler===>Url: {} {}", req.getRequestURI(), req.getMethod());
        logger.error("GlobalExceptionHandler===>ERROR: {}", e.getMessage());
        logger.error("GlobalExceptionHandler===>Params: {}", json.toJSONString());

		BaseResponse resp = new BaseResponse();
		resp.setError(true);
		resp.setMessage(e.getMessage());
		return resp;
	}
}
