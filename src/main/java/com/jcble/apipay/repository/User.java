package com.jcble.apipay.repository;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.Data;

/**
 * <p>
 * 用户实体类
 * </p>
 * 
 * @author Jingdong Wang
 * @date 2017年11月7日 下午6:21:51
 *
 */
@Entity
@Table(name = "`user`")
@Data
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private String id;

	@Column(name = "created_at", updatable = false)
	private Date createdAt = new Date();

	@Column(name = "updated_at")
	private Date updatedAt;
	
	@Column(name = "email")
	private String email;

	@Column(name = "password")
	private String password;

	@Column(name = "access_key")
	private String accessKey;

	@Column(name = "accessSecret")
	private String accessSecret;

	@Column(name = "role")
	private String role;

	@Column(name = "username")
	private String username;

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "organization_member", joinColumns = {
			@JoinColumn(name = "user_id", nullable = false, updatable = false) },
			inverseJoinColumns = { @JoinColumn(name = "organization_id",
					nullable = false, updatable = false) })
	private Set<Organization> organizations;

}
