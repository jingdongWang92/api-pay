package com.jcble.apipay.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.jcble.apipay.dto.AlipayRequest;
import com.jcble.apipay.service.AlipayService;

@RestController
@RequestMapping("/alipay")
public class AlipayController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	protected AlipayService alipayService;
	
	@Value("${alipay.public_key}")
	private String alipayPublicKey;
	
	/**
	 * 支付宝网页支付(将完整的表单html输出到请求页面)
	 * 
	 * @param outTradeNo   商户订单号
	 * @param totalAmount  支付金额(单位: 元)
	 * @param body         订单描述
	 * @param subject      订单标题
	 * @param httpResponse 
	 * @throws Exception
	 */
	@RequestMapping("/page-pay")
	void pay(@RequestParam(value = "out_trade_no", required = true) String outTradeNo,
			@RequestParam(value = "trade_amount", required = true) BigDecimal totalAmount,
			@RequestParam(value = "body", required = true) String body,
			@RequestParam(value = "subject", required = true) String subject, HttpServletResponse httpResponse)
			throws Exception {

		if (outTradeNo == null || totalAmount == null || subject == null || body == null) {
			throw new Exception("请求参数错误");
		}

		AlipayRequest order = new AlipayRequest();
		order.setOutTradeNo(outTradeNo);
		order.setTotalAmount(totalAmount);
		order.setSubject(subject);
		order.setBody(body);

		String form = alipayService.createAlipayPageForm(order);
		httpResponse.setContentType("text/html;charset=" + "UTF-8");
		httpResponse.getWriter().write(form);// 直接将完整的表单html输出到页面
		httpResponse.getWriter().flush();
		httpResponse.getWriter().close();
	}

	/**
	 * 支付宝网页支付完成返回请求
	 * 
	 */
	@RequestMapping("/return")
	public void reutrn(HttpServletRequest request, HttpServletResponse response)
			throws IOException, AlipayApiException {
		// 获取支付宝GET过来反馈信息
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String[]> requestParams = request.getParameterMap();
		for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}
			// 乱码解决，这段代码在出现乱码时使用
			valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
			params.put(name, valueStr);
		}

		boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, "UTF-8", "RSA2"); // 调用SDK验证签名

		String orderinfo = "";

		// ——请在这里编写您的程序（以下代码仅作参考）——
		if (signVerified) {
			String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
			String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");
			String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"), "UTF-8");
			orderinfo = "交易成功"+"支付宝交易号:" + trade_no + "<br/>订单号:" + out_trade_no + "<br/>交易金额:"
					+ total_amount;
		} else {
			orderinfo = "验签失败";
		}

		response.setContentType("text/html;charset=" + "UTF-8");
		response.getWriter().write(orderinfo);// 直接将完整的表单html输出到页面
		response.getWriter().flush();
		response.getWriter().close();
	}

	/**
	 * 支付宝完成支付通知接口
	 * 
	 */
	@RequestMapping(value = "/notify", produces = "text/html;charset=UTF-8")
	public String handleFlightPayNotify(HttpServletRequest request) throws Exception {
		logger.info("==========================支付宝通知开始==============================");
		String respStr = "";
		try {
			respStr = alipayService.handleAliPayNotify(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("==========================支付宝通知结束==============================");
		return respStr;
	}

}
