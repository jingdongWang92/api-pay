package com.jcble.apipay.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
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
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.jcble.apipay.config.MyWXPayConfig;
import com.jcble.apipay.dto.PayCallbackDto;
import com.jcble.apipay.dto.WXPayRequest;
import com.jcble.apipay.repository.Order;
import com.jcble.apipay.repository.OrderRepository;
import com.jcble.apipay.repository.Organization;
import com.jcble.apipay.repository.OrganizationRepository;
import com.jcble.apipay.service.WXPayService;

@Service
public class WXPayServiceImpl implements WXPayService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${wxpay.appid}")
	private String appid;
	
	@Value("${wxpay.mchid}")
	private String mchid;
	
	@Value("${wxpay.key}")
	private String key;
	
	@Value("${wxpay.notify_url}")
	private String notifyUrl;
	
	@Value("${pay.callback_url}")
	private String payCallbackUrl;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private OrganizationRepository organizationRepository;

	@Override
	public Map<String, String> unifiedOrder(WXPayRequest request) throws Exception {

		MyWXPayConfig config = new MyWXPayConfig(appid,mchid,key,notifyUrl);
		WXPay wxpay = new WXPay(config);

		Map<String, String> data = new HashMap<String, String>();
		data.put("attach", "地图工具方案".equals(request.getBody()) ? "1" : "2");
		data.put("body", request.getBody());
		data.put("out_trade_no", request.getOutTradeNo());
		data.put("device_info", "");
		data.put("fee_type", "CNY");
		//支付接口统一以元为单位, 所以这里需对支付金额进行处理
		data.put("total_fee", mul(request.getTotalFee(), new BigDecimal(100),0).intValue()+"");
		data.put("spbill_create_ip", config.getSpBillCreateIp());
		data.put("notify_url", config.getNotifyUrl());
		data.put("trade_type", "NATIVE"); // 此处指定为扫码支付
		data.put("product_id", request.getProductId());

		return wxpay.unifiedOrder(data);
	}

	@Override
	@Transactional
	public void handleWxPayNotify(HttpServletResponse response, String notifyData) throws Exception {
		Map<String, String> resp = new HashMap<>();
		MyWXPayConfig config = new MyWXPayConfig(appid,mchid,key,notifyUrl);
		WXPay wxpay = new WXPay(config);
		try {
			Map<String, String> notifyMap = WXPayUtil.xmlToMap(notifyData);

			Assert.isTrue(wxpay.isPayResultNotifySignatureValid(notifyMap), "invalid sign");
			logger.info("1.验签通过");
			Assert.isTrue("SUCCESS".equals(notifyMap.get("return_code")), "return_code is not equal SUCCESS");

			String outTradeNo = notifyMap.get("out_trade_no");
			String totalFee = notifyMap.get("total_fee");
			String tradeStatus = notifyMap.get("result_code");
			
			if(outTradeNo.startsWith("1")) {
				// 注意特殊情况：订单已经退款，但收到了支付结果成功的通知，不应把商户侧订单状态从退款改成支付成功
				if ("SUCCESS".equals(tradeStatus)) {
					Order order = orderRepository.findByTradeNo(outTradeNo);
					Assert.notNull(order, "order" + outTradeNo + " must not be null");
					//判断订单的状态, 防止重复通知
					Assert.isTrue("unpay".equals(order.getPayStatus()) || StringUtils.isBlank(order.getPayStatus()), "order "+ outTradeNo +" have been processed");
					Assert.isTrue(mul(order.getTradeAmount(), new BigDecimal(100), 0).compareTo(new BigDecimal(totalFee)) == 0,"invalid trade amount");
					logger.info("2.数据验证通过");
					
					// 修改订单状态为success
					order.setPayStatus("success");
					order.setPayMethod("wxpay");
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

					resp.put("return_code", WXPayConstants.SUCCESS);
					resp.put("return_msg", "OK");
				}
			} else {
				logger.info("支付回调...");
				RestTemplate restTemplate=new RestTemplate();
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
				
				PayCallbackDto order = new PayCallbackDto();
				order.setTotal_amount(new BigDecimal(totalFee));
				order.setTrade_status(tradeStatus);
				order.setOut_trade_no(outTradeNo);
				order.setPay_type("wxpay");
				HttpEntity<PayCallbackDto> entity = new HttpEntity<PayCallbackDto>(order, headers);
				ResponseEntity<String> res = restTemplate.exchange(payCallbackUrl, HttpMethod.POST, entity, String.class);
				logger.info(res.toString());
				Assert.isTrue("200".equals(res.getStatusCodeValue()+""), "支付回调失败");
				JSONObject resBody = JSONObject.parseObject(res.getBody());
				Assert.isTrue("200".equals(resBody.get("status")), resBody.get("msg")+"");
				
				resp.put("return_code", WXPayConstants.SUCCESS);
				resp.put("return_msg", "OK");
				logger.info("支付回调完成.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			resp.put("return_code", WXPayConstants.FAIL);
			resp.put("return_msg", e.getMessage());
		}
		String xml = WXPayUtil.mapToXml(resp);
		response.getOutputStream().write(xml.getBytes());
	}
	
	 /**
     * 精确乘法运算
     * 
     * @param v1 被乘数
     * @param v2 乘数
     * @param scale 精度
     * @return 两个参数的积
     */
    public static BigDecimal mul(BigDecimal v1, BigDecimal v2, int scale) {
        return (v1.multiply(v2)).setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

}
