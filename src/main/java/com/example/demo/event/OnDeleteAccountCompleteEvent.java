package com.example.demo.event;

import java.util.Locale;

import org.springframework.context.ApplicationEvent;

import com.example.demo.model.SiteUser;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OnDeleteAccountCompleteEvent extends ApplicationEvent{

	private SiteUser user;
	private String appUrl;
	
	public OnDeleteAccountCompleteEvent(SiteUser user, String appUrl){
		
		super(user);
		this.user = user;
		this.appUrl = appUrl;
	}
}
