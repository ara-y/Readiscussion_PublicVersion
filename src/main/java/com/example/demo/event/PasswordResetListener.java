package com.example.demo.event;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.demo.model.SiteUser;
import com.example.demo.service.CallUserService;

@Component
public class PasswordResetListener implements ApplicationListener<OnPasswordResetCompleteEvent>{

	@Autowired
	private CallUserService callUserService;
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Override
	public void onApplicationEvent(OnPasswordResetCompleteEvent event) {
		confirmPasswordReset(event);
	}
	
	private void confirmPasswordReset(OnPasswordResetCompleteEvent event) {
		
		String resetToken = UUID.randomUUID().toString();
		SiteUser user = event.getUser();
		String url = event.getAppUrl() + "/resetPassword/change?token=" + resetToken;
		
		callUserService.createPasswordResetToken(user, resetToken);
		
		String subject = "パスワードリセットのご案内";
		
		SimpleMailMessage email = new SimpleMailMessage();
		email.setSubject(subject);
	    email.setText("パスワードリセットには下記URLへアクセスして行ってください" + "\r\n" + url);
	    email.setTo(user.getMail());
	    
	    mailSender.send(email);
	}
	

}
