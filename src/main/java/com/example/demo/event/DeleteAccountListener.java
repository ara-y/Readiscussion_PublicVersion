package com.example.demo.event;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.demo.model.SiteUser;
import com.example.demo.service.CallUserService;

@Component
public class DeleteAccountListener implements ApplicationListener<OnDeleteAccountCompleteEvent>{

	@Autowired
	private CallUserService callUserService;
	
	@Autowired
	private JavaMailSender mailSender;
	
	
	@Override
	public void onApplicationEvent(OnDeleteAccountCompleteEvent event) {
		confirmDeleteAccount(event);
	}
	
	private void confirmDeleteAccount(OnDeleteAccountCompleteEvent event) {
		
		SiteUser siteUser = event.getUser();
		String token = UUID.randomUUID().toString();
		
		callUserService.createDeleteAccountToken(siteUser, token);
		
		String recipientAddress = siteUser.getMail();
		String subject = "ReaDisucussionアカウント削除に伴なう確認メールの送信";
		String confirmationUrl = event.getAppUrl() + "/deleteAccount/confirm?token=" + token;
		
		
		String messageInMail = "アカウント削除のメールを送らせていただきました。";
		
		SimpleMailMessage email = new SimpleMailMessage();
		
		email.setTo(recipientAddress);
		email.setSubject(subject);
		email.setText(messageInMail + " \r\n " + confirmationUrl);
		
		mailSender.send(email);
		}
}
