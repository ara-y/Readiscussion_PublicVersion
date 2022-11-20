package com.example.demo.model;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class VerificationToken {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	
	private String token;
	
	@OneToOne(targetEntity = SiteUser.class, fetch = FetchType.EAGER)
	@JoinColumn(nullable = false, name = "user_id")
	private SiteUser user;
	
	private Date expiryDate;
	
	private final int expiration = 60*24;
	
	private Date calculateExpiryDate(int expiryTimeInMinutes) {
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.add(calendar.MINUTE, expiryTimeInMinutes);
		
		return new Date(calendar.getTime().getTime());
	}
	
	public VerificationToken(){
		super();
	}
	
	//いらない
	public VerificationToken(String token){
		super();
		
		this.token = token;
		this.expiryDate = calculateExpiryDate(expiration);
	}
	
	public VerificationToken(String token,SiteUser user){
		super();
		
		this.token = token;
		this.user = user;
		this.expiryDate = calculateExpiryDate(expiration);
	}
	
}
