package com.example.demo.event;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.demo.error.UserAlreadyExistException;
import com.example.demo.model.SiteUser;
import com.example.demo.service.CallUserService;
import com.sun.mail.handlers.message_rfc822;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent>{

	@Autowired
	private CallUserService callUserService;
	
	@Autowired
	private MessageSource message;
	
	@Autowired
	private JavaMailSender mailSender;
	
	
	@Override
	public void onApplicationEvent(OnRegistrationCompleteEvent event) {
		confirmRegistration(event);
	}
	
	private void confirmRegistration(OnRegistrationCompleteEvent event) {
		
		SiteUser siteUser = event.getSiteUser();
		String token = UUID.randomUUID().toString();
		
		callUserService.createVerificationTokenForUser(siteUser, token);
		
		String recipientAddress = siteUser.getMail();
		String subject = "Readiscussion登録に伴なう確認メールの送信";
		String confirmationUrl = event.getAppUrl() + "/register_account/confirm?token=" + token;
		String messageInMail = message.getMessage("message.loginSuccess",null, event.getLocale());
		
		SimpleMailMessage email = new SimpleMailMessage();
		
		email.setTo(recipientAddress);
		email.setSubject(subject);
		email.setText(messageInMail + " \r\n " + confirmationUrl);
		
		mailSender.send(email);
	}
	
}
