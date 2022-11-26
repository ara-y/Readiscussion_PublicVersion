package com.example.demo.event;

import java.util.Locale;

import org.springframework.context.ApplicationEvent;

import com.example.demo.model.SiteUser;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OnRegistrationCompleteEvent extends ApplicationEvent{

	private String appUrl;
	private SiteUser siteUser;
	private String verificationToken;
	
	public OnRegistrationCompleteEvent(String appUrl, SiteUser siteUser, String verificationToken) {
		super(siteUser);
		
		this.siteUser = siteUser;
		this.appUrl = appUrl;
		this.verificationToken = verificationToken;
	}
	
	
}
