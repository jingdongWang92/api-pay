/**
 * 
 */
package com.jcble.apipay.dto;

import java.math.BigDecimal;

import lombok.Data;

/**
 * <p>Description: </p>
 * @author Jingdong Wang
 * @date 2018年1月2日 上午11:57:14
 *
 */
@Data
public class PayCallbackDto {
	
	String out_trade_no;
	
	BigDecimal total_amount;
	
	String trade_status;
	
	String pay_type;
	
}
