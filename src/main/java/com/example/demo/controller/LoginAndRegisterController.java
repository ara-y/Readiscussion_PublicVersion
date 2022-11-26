package com.example.demo.controller;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

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
import com.example.demo.model.DTO.SendSiteUserInformation;
import com.example.demo.service.CallUserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LoginAndRegisterController {
	
	@Autowired
	CallUserService callUserService;
	
	@Autowired
	ApplicationEventPublisher publisher;	
	
	@GetMapping("/login")
	public String login(@ModelAttribute("forSiteUser") SendSiteUserInformation forSiteUser,HttpServletRequest request) {
		return "login";
	}

	@GetMapping("/registerAccount")
	public String register(@ModelAttribute("user") SendSiteUserInformation user) {
		return "register";
	}
	
	@PostMapping("/registerAccount")
	public String postRegister(@Validated @ModelAttribute("user") SendSiteUserInformation user, BindingResult result,
			               Model model, HttpServletRequest request
			               ) {
		
		if(result.hasErrors()) {
			
			model.addAttribute("message","登録に失敗しました。入力情報に誤りがないか確認してください。");
			
			return "register";
		}
		
		/*
		 * callUserService.registerNewUserAccountではEmailが既に登録されている時にUserAlreadyExistException
		 * を投げる仕様なので、catch文で例外を受けとりメッセージを表示する。
		 * それ以外は何かしらの理由でメールが送れないのでその旨表示する。
		 */
	    try {
		SiteUser siteUser = callUserService.registerNewUserAccount(user);
		String appUrl = request.getScheme() +"://"+ request.getServerName() + ":" + request.getServerPort();
		
		String token = UUID.randomUUID().toString();
		callUserService.createVerificationTokenForUser(siteUser, token);
		
		publisher.publishEvent(new OnRegistrationCompleteEvent(appUrl,siteUser,token));
		
		}catch(UserAlreadyExistException uaeEx) {
			model.addAttribute("message","既にこのメールアドレスで登録されているユーザーがいます。");
			//再登録要否確認＋処理　未実装
			return "register";
		}catch(RuntimeException rEx) {
			model.addAttribute("message","メールの送信ができませんでした。");
			return "register";
		}
		
		model.addAttribute("message","登録が完了しましたので、確認メールを送らせていただきました。有効期限は24時間です。");
		return "/login";
	}
	
	
	@GetMapping("/registerAccount/confirm")
	public String confirmRegistration(HttpServletRequest request, Model model, @RequestParam("token") String token) {
		
		VerificationToken verificationToken = callUserService.getVerificationToken(token);
		if(verificationToken == null) {
			model.addAttribute("message","無効なトークンです。確認メールを再送信するかアカウントを作り直してください。");
			return "/badUser";
		}
		
		SiteUser user = verificationToken.getUser();
		Calendar cal = Calendar.getInstance();
		
		if((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) < 0) {
			model.addAttribute("message","トークンの有効期限が切れています。 ");
			return "/badUser";
		}
		
		user.setEnabled(true);
		callUserService.saveRegisteredUser(user);
		model.addAttribute("message","アカウントが登録されました。");
		
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
	public String resendConfirmationEmail(@ModelAttribute("forSiteUser") SendSiteUserInformation forSiteUser,HttpServletRequest request) {
		
		SiteUser siteUser = callUserService.getSiteUserByMail(forSiteUser.getMail());
		VerificationToken token = callUserService.getVerificationTokenBySiteUser(siteUser);
		
		String appUrl = request.getScheme() +"://"+ request.getServerName() + ":" + request.getServerPort();
		
		publisher.publishEvent(new OnRegistrationCompleteEvent(appUrl, siteUser, token.getToken()));
	
		return "login";
	}
		
	@GetMapping("/badUser")
	public String badUser() {
		return "badUser";
	}
	
	
	
}
