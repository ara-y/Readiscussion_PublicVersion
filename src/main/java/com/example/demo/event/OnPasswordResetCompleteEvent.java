package com.example.demo.event;

import org.springframework.context.ApplicationEvent;

import com.example.demo.model.SiteUser;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OnPasswordResetCompleteEvent extends ApplicationEvent{

	private SiteUser user;
	private String appUrl;
	
	public OnPasswordResetCompleteEvent(SiteUser user, String appUrl) {
		super(user);
		this.user = user;
		this.appUrl = appUrl;
	}

}
