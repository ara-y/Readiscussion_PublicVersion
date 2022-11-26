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

@Getter
@Setter
@Entity
public class PasswordResetToken {

	private static final int EXPIRATION = 60;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	
	private String token;
	
	@OneToOne(targetEntity = SiteUser.class ,fetch = FetchType.EAGER)
	@JoinColumn(nullable = false, name = "user_id")
	private SiteUser user;
	
	private Date expiryDate;
	
	private Date calculateExpiryDate(int expiryTimeInMinutes) {
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.add(calendar.MINUTE, expiryTimeInMinutes);
		
		return new Date(calendar.getTime().getTime());
	}
	
	public PasswordResetToken(){
		super();
	}
	
	public PasswordResetToken(String token, SiteUser user){
		super();
		this.token = token;
		this.user = user;
		this.expiryDate = calculateExpiryDate(EXPIRATION);
	}
}
