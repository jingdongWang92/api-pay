package com.jcble.apipay.service.impl;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.jcble.apipay.dto.AlipayRequest;
import com.jcble.apipay.dto.PayCallbackDto;
import com.jcble.apipay.repository.Order;
import com.jcble.apipay.repository.OrderRepository;
import com.jcble.apipay.repository.Organization;
import com.jcble.apipay.repository.OrganizationRepository;
import com.jcble.apipay.service.AlipayService;

@Service
public class AlipayServiceImpl implements AlipayService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private OrganizationRepository organizationRepository;

	private static final String ALIPAY_GATEWAY_URL = "https://openapi.alipay.com/gateway.do";
	private static final String RETURN_SCUCCESS = "success";
	private static final String RETURN_FAILURE = "failure";
	
	@Value("${alipay.appid}")
	private String alipayAppId;
	@Value("${alipay.private_key}")
	private String alipayPrivateKey;
	@Value("${alipay.public_key}")
	private String alipayPublicKey;
	@Value("${alipay.return_url}")
	private String alipayReturnUrl;
	@Value("${alipay.notify_url}")
	private String alipayNotifyUrl;
	
	@Value("${pay.callback_url}")
	private String payCallbackUrl;
	
	@Override
	public String createAlipayPageForm(AlipayRequest request) throws AlipayApiException {
		AlipayClient alipayClient = new DefaultAlipayClient(ALIPAY_GATEWAY_URL, alipayAppId, alipayPrivateKey,
				AlipayConstants.FORMAT_JSON, AlipayConstants.CHARSET_UTF8, alipayPublicKey,
				AlipayConstants.SIGN_TYPE_RSA2); // 获得初始化的AlipayClient
		AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();// 创建API对应的request
		alipayRequest.setReturnUrl(alipayReturnUrl);
		alipayRequest.setNotifyUrl(alipayNotifyUrl);// 在公共参数中设置回跳和通知地址

		alipayRequest.setBizContent("{\"out_trade_no\":\"" + request.getOutTradeNo() + "\"," + "\"total_amount\":\""
				+ request.getTotalAmount() + "\"," + "\"subject\":\"" + request.getSubject() + "\"," + "\"body\":\""
				+ request.getBody() + "\"," + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

		return alipayClient.pageExecute(alipayRequest).getBody();// 调用SDK生成表单
	}

	@Override
	@Transactional
	public String handleAliPayNotify(HttpServletRequest request) throws AlipayApiException, ParseException {
		String respStr = RETURN_FAILURE;
		// 获取支付宝POST过来反馈信息
		Map<String, String> requestParamMap = new HashMap<String, String>();
		Set<String> keySet = request.getParameterMap().keySet();
		for (Object keyObject : keySet.toArray()) {
			String[] valueArr = (String[]) request.getParameterMap().get(keyObject);
			requestParamMap.put((String) keyObject, valueArr[0]);
		}
		
		logger.info(JSONObject.toJSONString(requestParamMap));
		
		// 验证消息是否是支付宝发出的合法消息
		Assert.isTrue(signVerify(requestParamMap), "invalid sign");
		logger.info("1.验签通过");
		
		String outTradeNo = requestParamMap.get("out_trade_no");
		String tradeStatus = requestParamMap.get("trade_status");
		String totalAmount = requestParamMap.get("total_amount");
		String appId = requestParamMap.get("app_id");
		
		Assert.isTrue(alipayAppId.equals(appId), "invalid alipay appid");
		logger.info("2.数据验证通过");
		
		if(outTradeNo.startsWith("1")) {
			/**
			 * 1、商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号
			 * 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额
			 * 3、校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
			 * 4、验证app_id是否为该商户本身。
			 */
			Order order = orderRepository.findByTradeNo(outTradeNo);
			Assert.notNull(order, "order " + outTradeNo + " not found");
			//判断订单的状态, 防止重复通知
			Assert.isTrue("unpay".equals(order.getPayStatus()) || StringUtils.isBlank(order.getPayStatus()), "order "+ outTradeNo +" have been processed");
			Assert.isTrue(new BigDecimal(totalAmount).compareTo(order.getTradeAmount()) == 0, "invalid trade amount");
			
			// 业务处理
			if ("TRADE_SUCCESS".equals(tradeStatus)) {
				// 修改订单状态
				order.setPayStatus("success");
				order.setPayMethod("alipay");
				order.setPayedAt(new Date());
				orderRepository.save(order);
				logger.info("3.订单信息更新成功");
				
				if(order.getOrganization() != null) {
					LocalDate today = LocalDate.now();
					LocalDate nextYear = today.minusYears(-1);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

					Organization organization = order.getOrganization();
					organization.setPlan(order.getPlan());
					organization.setUpdatedAt(new Date());
					organization.setPlanExpireAt(sdf.parse(nextYear.toString()));
					organizationRepository.save(organization);
					logger.info("4.方案信息更新成功");
				}
				
				respStr = RETURN_SCUCCESS;
			}
		} else {
			logger.info("支付回调...");
			RestTemplate restTemplate=new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			
			PayCallbackDto order = new PayCallbackDto();
			order.setTotal_amount(new BigDecimal(totalAmount));
			order.setTrade_status(tradeStatus);
			order.setOut_trade_no(outTradeNo);
			order.setPay_type("alipay");
			HttpEntity<PayCallbackDto> entity = new HttpEntity<PayCallbackDto>(order, headers);
			ResponseEntity<String> res = restTemplate.exchange(payCallbackUrl, HttpMethod.POST, entity, String.class);
			logger.info(res.toString());
			Assert.isTrue("200".equals(res.getStatusCodeValue()+""), "支付回调失败");
			JSONObject resBody = JSONObject.parseObject(res.getBody());
			Assert.isTrue("200".equals(resBody.get("status")), resBody.get("msg")+"");
			respStr = RETURN_SCUCCESS;
			logger.info("支付回调完成.");
		}
		return respStr;
	}

	/**
	 * 支付宝异步通知验签
	 * 
	 * @param request
	 * @return
	 * @throws AlipayApiException
	 */
	private boolean signVerify(Map<String, String> requestParamMap) throws AlipayApiException {
		// 切记alipaypublickey是支付宝的公钥，请去open.alipay.com对应应用下查看。
		return AlipaySignature.rsaCheckV1(requestParamMap, alipayPublicKey, AlipayConstants.CHARSET_UTF8,
				AlipayConstants.SIGN_TYPE_RSA2);
	}
}
