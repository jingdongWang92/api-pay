package com.jcble.apipay.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Jingdong Wang
 * @date 2017年11月7日 下午6:21:44
 *
 */
@Repository
public interface UserRepository extends CrudRepository<User, String>{

}
