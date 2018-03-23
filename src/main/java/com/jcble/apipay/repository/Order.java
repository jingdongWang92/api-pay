package com.jcble.apipay.repository;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;


/**
 * 
 * @author Jingdong Wang
 * @date 2017年11月6日 下午5:56:25
 *
 */
@Entity
@Table(name = "`order`")
@Data
public class Order {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private String id;
	
	@Column(name = "created_at", updatable = false)
	private Date createdAt = new Date();

	@Column(name = "updated_at")
	private Date updatedAt;
	
	@Column(name = "trade_no")
	private String tradeNo;

	@Column(name = "trade_amount")
	private BigDecimal tradeAmount;

	@Column(name = "pay_method")
	private String payMethod;

	@Column(name = "payed_at")
	private Date payedAt;

	@Column(name = "pay_status")
	private String payStatus;

	@Column(name = "invoiced")
	private Boolean invoiced = false;

	@Column(name = "merchandise_name")
	private String merchandiseName;

	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	@OneToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@OneToOne
	@JoinColumn(name = "plan_id")
	private Plan plan;

}
