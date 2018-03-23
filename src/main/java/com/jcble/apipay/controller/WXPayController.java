package com.jcble.apipay.controller;

import java.math.BigDecimal;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jcble.apipay.dto.Response;
import com.jcble.apipay.dto.WXPayRequest;
import com.jcble.apipay.service.WXPayService;

@RestController
public class WXPayController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private WXPayService wxPayService;

	/**
	 * 微信支付统一下单(目前只支持微信扫码支付)
	 * 
	 * @param orderNo   商户订单号
	 * @param amount    支付金额
	 * @param body      订单描述
	 * @param subject   订单标题
	 * @param productId 商家产品id
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/wxpay")
	public Response unifiedOrder(@RequestParam(value = "out_trade_no", required = true) String outTradeNo,
			@RequestParam(value = "trade_amount", required = true) BigDecimal tradeAmount,
			@RequestParam(value = "body", required = true) String body,
			@RequestParam(value = "subject", required = true) String subject,
			@RequestParam(value = "product_id", required = true) String productId) throws Exception {
		Response resp = new Response();
		if (StringUtils.isBlank(outTradeNo) || tradeAmount == null || StringUtils.isBlank(subject) || StringUtils.isBlank(body)
				|| StringUtils.isBlank(productId)) {
			throw new Exception("request params error");
		}
		
		WXPayRequest order = new WXPayRequest();
		order.setOutTradeNo(outTradeNo);
		order.setTotalFee(tradeAmount);
		order.setSubject(subject);
		order.setBody(body);
		order.setProductId(productId);

		resp.setPayload(wxPayService.unifiedOrder(order));

		return resp;
	}

	/**
	 * 微信完成支付通知接口
	 * @throws Exception 
	 * 
	 */
	@RequestMapping(value = "/wxpay/notify")
	public void handleFlightPayNotify(@RequestBody String notifyData, HttpServletResponse response) throws Exception {
		logger.info("======================微信支付结果通知开始=========================");
		logger.info(notifyData);
		try {
			wxPayService.handleWxPayNotify(response, notifyData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("======================微信支付结果通知结束=========================");
	}

}
