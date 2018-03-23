package com.jcble.apipay.service;

import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;

import com.alipay.api.AlipayApiException;
import com.jcble.apipay.dto.AlipayRequest;

public interface AlipayService {
	
	/**
	 * 处理支付宝支付通知
	 * 
	 * @param request  
	 * @return
	 * @throws AlipayApiException 
	 * @throws ParseException 
	 */
	String handleAliPayNotify(HttpServletRequest request) throws AlipayApiException, ParseException;

	/**
	 * 生成网页支付表单
	 * 
	 * @param request 支付请求相数据
	 * @return  支付表单
	 * @throws AlipayApiException 
	 */
	String createAlipayPageForm(AlipayRequest payRequest) throws AlipayApiException;

}
