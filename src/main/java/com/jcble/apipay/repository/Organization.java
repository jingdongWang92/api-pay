package com.jcble.apipay.repository;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

/**
 * <p>
 * 	Organization Entity
 * </p>
 * 
 * @author Jingdong Wang
 * @date 2017年11月28日 下午5:00:25
 *
 */
@Entity
@Table(name = "`organization`")
@Data
public class Organization {
	
	@Id
	private Long id;
	
	@Column(name = "created_at", updatable = false)
	private Date createdAt = new Date();

	@Column(name = "updated_at")
	private Date updatedAt;

	@Column(name = "name")
	private String name;

	@Column(name = "personal")
	private Boolean personal;

	@OneToOne
	@JoinColumn(name = "plan_id")
	private Plan plan;

	@Column(name = "owner_id")
	private String ownerId;
	
	@Column(name = "plan_expired_at")
	private Date planExpireAt;
	
	//TODO: json序列化递归问题
//	@ManyToMany(mappedBy = "organizations")
//	@JsonIgnore
//	private Set<User> members;

}
