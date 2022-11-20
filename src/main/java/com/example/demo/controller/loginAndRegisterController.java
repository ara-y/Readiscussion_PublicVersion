package com.example.demo.controller;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Calendar;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.error.UserAlreadyExistException;
import com.example.demo.event.OnRegistrationCompleteEvent;
import com.example.demo.model.SiteUser;
import com.example.demo.model.VerificationToken;
import com.example.demo.model.DTO.ForSiteUser;
import com.example.demo.service.CallUserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class loginAndRegisterController {
	
	@Autowired
	CallUserService callUserService;
	
	@Autowired
	MessageSource message;
	
	@Autowired
	ApplicationEventPublisher publisher;
	
	@Autowired
	private JavaMailSender mailSender;
	
	
	@GetMapping("/login")
	public String login(@ModelAttribute("forSiteUser") ForSiteUser forSiteUser,HttpServletRequest request) {
		System.out.println(request.getRemoteAddr());
		return "login";
	}

	@GetMapping("/register_account")
	public String register(@ModelAttribute("user") ForSiteUser user) {
		return "register";
	}
	
	@PostMapping("/register_account")
	public String register(@Validated @ModelAttribute("user") ForSiteUser user, BindingResult result,
			               Model model, HttpServletRequest request
			               ) {
		
		System.out.println(user.getIsUsing2FA());
		
		if(result.hasErrors()) {
			
			model.addAttribute("message", message.getMessage("message.failRegister",null,request.getLocale()));
			
			return "register";
		}
		
		/*
		 * callUserService.registerNewUserAccountではEmailが既に登録されている時にUserAlreadyExistException
		 * を投げる仕様なので、catch文で例外を受けとりメッセージを表示する。
		 * それ以外は何かしらの理由でメールが送れないのでその旨表示する。
		 */
	    try {
		SiteUser siteuser = callUserService.registerNewUserAccount(user);
		String appUrl = request.getScheme() +"://"+ request.getServerName() + ":" + request.getServerPort();
		
		publisher.publishEvent(new OnRegistrationCompleteEvent(appUrl,null,siteuser));
		
		}catch(UserAlreadyExistException uaeEx) {
			model.addAttribute("message",message.getMessage("message.userAlreadyExist",null,request.getLocale()));
			return "register";
		}catch(RuntimeException rEx) {
			model.addAttribute("message",message.getMessage("message.failSendEmail",null,request.getLocale()));
			return "register";
		}
		
		model.addAttribute("message",message.getMessage("message.loginSuccess",null,request.getLocale()));
		return "/login";
	}
	
	
	@GetMapping("/register_account/confirm")
	public String confirmRegistration(HttpServletRequest request, Model model, @RequestParam("token") String token) {
		Locale locale = request.getLocale();
		
		VerificationToken verificationToken = callUserService.getVerificationToken(token);
		if(verificationToken == null) {
			model.addAttribute("message",message.getMessage("auth.message.invalidRegistrationToken", null, locale));
			return "/badUser";
		}
		
		SiteUser user = verificationToken.getUser();
		Calendar cal = Calendar.getInstance();
		
		//verificationToken.getExpiryDate()では「Fri Feb 18 17:47:34 JST 2022」の形式。さらにgetTime()を使うことでナノ秒で返す
		if((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) < 0) {
			model.addAttribute("message",message.getMessage("auth.message.expiredRegistrationToken", null, locale));
			return "/badUser";
		}
		
		user.setEnabled(true);
		callUserService.saveRegisteredUser(user);
		model.addAttribute("message",message.getMessage("auth.message.successEnableAccount", null, locale));
		
		if(user.isUsing2FA()) {
			try {
			model.addAttribute("qr", callUserService.generateQRUrl(user));
			return "/qrcode";
			}catch(UnsupportedEncodingException e) {
				System.out.println("QRコードを生成できませんでした。");
			}
		}
		return "/login";
	}
	
	@GetMapping("/qrcode")
	public String qr() {
		return "qrCode";
	}
	
	@PostMapping("/resendConfirmationEmail")
	public String resendConfirmationEmail(@ModelAttribute("forSiteUser") ForSiteUser forSiteUser,HttpServletRequest request) {
		Locale locale = request.getLocale();
		
		SiteUser siteUser = callUserService.getSiteUserByMail(forSiteUser.getMail());
		VerificationToken token = callUserService.getVerificationTokenBySiteUser(siteUser);
		
		String appUrl = request.getScheme() +"://"+ request.getServerName() + ":" + request.getServerPort();
		
		SimpleMailMessage email = 
			      constructResendVerificationTokenEmail(appUrl, locale, token);
		mailSender.send(email);
		
		return "login";
	}
	
	
	
	@GetMapping("/badUser")
	public String badUser() {
		return "badUser";
	}
	
	
	// -------non api-------------
	private SimpleMailMessage constructResendVerificationTokenEmail
	(String contextPath , Locale locale , VerificationToken token){
		
		String confirmationUrl = contextPath + "/register_account/confirm?token=" + token.getToken();
		String messageInMail = message.getMessage("message.loginSuccess",null, locale);
		
		SimpleMailMessage email = new SimpleMailMessage();
		
		email.setTo(token.getUser().getMail());
		email.setSubject("ReaDiscussion_確認メールの再送信");
		email.setText(messageInMail + " /r/n " + confirmationUrl);
		return email;
	}
	
	
}
