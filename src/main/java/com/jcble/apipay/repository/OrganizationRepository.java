/**
 * 
 */
package com.jcble.apipay.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * Description: Organization Repository
 * </p>
 * 
 * @author Jingdong Wang
 * @date 2017年11月29日 上午9:35:21
 *
 */
@Repository
public interface OrganizationRepository extends CrudRepository<Organization, String> {

}
