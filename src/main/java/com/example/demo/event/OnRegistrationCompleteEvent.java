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
	private Locale locale;
	private SiteUser siteUser;
	
	public OnRegistrationCompleteEvent(String appUrl, Locale locale, SiteUser siteUser) {
		super(siteUser);
		
		this.siteUser = siteUser;
		this.locale = locale;
		this.appUrl = appUrl;
	}
	
	
}
