package com.jcble.apipay.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Jingdong Wang
 * @date 2017年11月6日 下午5:54:29
 *
 */
@Repository
public interface OrderRepository extends CrudRepository<Order, String> {
	
	Order findByTradeNo(String tradeNo);

}
