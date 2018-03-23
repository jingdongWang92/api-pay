package com.jcble.apipay.service;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.jcble.apipay.dto.WXPayRequest;

public interface WXPayService {
	
	/**
	 * 微信支付: 统一下单
	 * 
	 * @param payRequest 支付请求数据
	 * @return 
	 * @throws Exception
	 */
	Map<String, String> unifiedOrder(WXPayRequest payRequest) throws Exception;

	/**
	 * 微信支付异步回调处理
	 * 
	 * @param response 
	 * @param notifyData 回调数据
	 * 
	 * @throws Exception
	 */
	void handleWxPayNotify(HttpServletResponse response, String notifyData) throws Exception;

}
