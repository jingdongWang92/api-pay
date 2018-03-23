/**
 * 
 */
package com.jcble.apipay.repository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * <p>
 * Description: Plan Entity
 * </p>
 * 
 * @author Jingdong Wang
 * @date 2017年11月15日 上午11:57:51
 *
 */
@Entity
@Table(name = "`plan`")
@Data
public class Plan {

	@Id
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "price")
	private Integer price;
	
	@Column(name = "organization_count")
	private Integer organizationCount;

	@Column(name = "project_count")
	private Integer projectCount;

	@Column(name = "map_count")
	private Integer mapCount;

	@Column(name = "file_export")
	private Boolean fileExport;
	
}
