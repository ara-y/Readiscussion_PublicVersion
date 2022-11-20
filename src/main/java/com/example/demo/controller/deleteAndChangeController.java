package com.example.demo.controller;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.error.UserNotFoundException;
import com.example.demo.event.OnDeleteAccountCompleteEvent;
import com.example.demo.model.DeleteAccountToken;
import com.example.demo.model.SiteUser;
import com.example.demo.model.DTO.ForResetPassword;
import com.example.demo.service.CallUserService;
import com.example.demo.service.Disccusion.CallDiscussionService;
import com.example.demo.util.GenericResponse;

@Controller
public class deleteAndChangeController {
	
	@Autowired
	CallUserService callUserService;
	
	@Autowired
	CallDiscussionService callDiscussionService;
	
	
	@Autowired
	ApplicationEventPublisher publisher;
	
	@Autowired
	MessageSource message;
	
	@Autowired
	JavaMailSender mailSender;
	
	
	@GetMapping("/delete_account")
	public String deleteAccount( ) {
		return "deleteAccount";
	}
	
	@PostMapping("/delete_account")
	public String PreDeleteAccount(Authentication authentication,HttpServletRequest request,Model model) {
			
		Locale locale = request.getLocale();
		
		try {
			
				
				String Username = authentication.getName();
				SiteUser user = callUserService.getSiteUserByMail(Username);
				
				
				//2要素認証無の時
				SiteUser siteUser = callUserService.getSiteUserByMail(authentication.getName());
			
				//2要素認証の時 authentication使えない？
				if(callUserService.getSiteUserByMail(user.getMail()) != null){
					 siteUser = callUserService.getSiteUserByMail(user.getMail());
				}
				
				
			String appUrl = request.getScheme() +"://"+ request.getServerName() + ":" + request.getServerPort();
			
			publisher.publishEvent(new OnDeleteAccountCompleteEvent(siteUser,appUrl,locale));
		}catch(RuntimeException ex) {
			model.addAttribute("message",message.getMessage("message.failSendEmail", null, locale));
			return "deleteAccount";
		}
		
			model.addAttribute("message",message.getMessage("message.successSendingDeleteEmail", null, locale));
		return "deleteAccount";
	}
	
	//アカウント削除できるように改変
	@Transactional
	@GetMapping("/delete_account/confirm")
	public String deleteAccountConfirm(@RequestParam("token") String token, Model model , HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
		
		
		Locale locale = request.getLocale();
		
		DeleteAccountToken deleteAccountToken = callUserService.getDeleteAccountToken(token);
		
		
		if(deleteAccountToken == null) {
			model.addAttribute("message", message.getMessage("message.invalidDeleteAccountToken", null, locale));
			return "/delete_account";
		}
		
		Calendar cal = Calendar.getInstance();
		
		if(deleteAccountToken.getExpiryDate().getTime() - cal.getTime().getTime() < 0) {
			model.addAttribute("message", message.getMessage("message.expiredDeleteAccountToken", null, locale));
			return "/delete_account";
		}
		
		SiteUser siteUser = deleteAccountToken.getUser();
		
	//	callMyTaskService.deleteAllTasksForDeleteAccount(siteUser);
		
		callDiscussionService.deleteAlreadyReadTime(siteUser);
		callDiscussionService.deleteRoomParticipant_users(siteUser);
		callDiscussionService.autoTransferRoomAdminPrivilege(siteUser);
		callUserService.deleteUser(siteUser);
		
		callUserService.logoutProcess(request, response);
		
		return "redirect:/login";
	}
	
	
	
	@GetMapping("/resetPassword")
	public String resetPassword (HttpServletRequest request,Model model) {
		
	//sessionの中にあるcsrftokenを呼び出す
	DefaultCsrfToken token = (DefaultCsrfToken)	request.getSession().getAttribute("org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN"
			);
	model.addAttribute("_csrf", token.getToken());
	
		return "resetPassword";
	}
	
	
	//★★★★★ voidに変更　GenericResponse何にも使っていない
	//送信後は送信完了のページに飛ばす
	@PostMapping("/resetPassword")
	public GenericResponse PreResetPassword(@RequestParam("mail") String mail, HttpServletRequest request,Model model,Authentication authentication) {
		
		SiteUser user = callUserService.getSiteUserByMail(mail);
		if(user == null) {
			throw new UserNotFoundException();
		}
		
		System.out.println(authentication.getName());
		System.out.println(mail);
		
		if(mail.equals(authentication.getName())) {
			System.out.println("送れません");
			return new GenericResponse("送れません。");
		}
		
		String token = UUID.randomUUID().toString();
		
		callUserService.createPasswordResetToken(user, token);
		//--emailの送信からスタート
		
	
		String contextPath = request.getScheme() +"://"+ request.getServerName() + ":" + request.getServerPort();
		
		mailSender.send(constructResetTokenEmail(contextPath, user, token));
		model.addAttribute("message", "送信に成功しました");
		
		//cookieの中にあるcsrfトークンを呼び出す。
		for(int i =0; i<request.getCookies().length; i++) {
			if(request.getCookies()[i].getName().equals("XSRF-TOKEN")) {
				model.addAttribute("_csrf", request.getCookies()[i].getValue());
			}
		}
		return new GenericResponse("現在未使用");
	}
	
	
	@GetMapping("/resetPassword/change")
	public String preChangePassword(@RequestParam("token") String token,Model model) {
		
		String result = callUserService.validatePasswordResetToken(token);
		if(result != null) {
			return "redirect:/login?message=" + result;
		}else {
			return "redirect:/resetPassword/update?token=" + token;
		}
	}
	
	@GetMapping("/resetPassword/update")
	public String changePassword(@ModelAttribute("passwordDTO")ForResetPassword passwordDTO) {
		return "updatePassword";
	}
	
	@PostMapping("/resetPassword/update")
	public String updatePassword( @ModelAttribute("passwordDTO")ForResetPassword passwordDTO,Model model) {
		
	
		
		String result = callUserService.validatePasswordResetToken(passwordDTO.getToken());
		if(result != null) {
			model.addAttribute("message",result + "もう一度発行しなおしてください。" );
			return "resetPassword" ;
		}
		
		String passResult = checkInputPassword(passwordDTO.getPassword(), passwordDTO.getMatchPassword());
		
		if(passResult != null) {
			model.addAttribute("message", passResult + "確認メールから再度入力してください。");
			return "updatePassword";
		}
		
		SiteUser user = callUserService.getSiteUserByPasswordResetToken(passwordDTO.getToken()).getUser();
		
		callUserService.savePassword(user, passwordDTO.getPassword());
		model.addAttribute("message", "パスワードの変更ができました。");
		
		return "login";
	}
	
	/*
	@GetMapping("/logout")
	public String logout() {
		return "login";
	}*/
	
	//---non api---
	private SimpleMailMessage constructResetTokenEmail(String contextPath, SiteUser user, String token) {
			String url = contextPath + "/resetPassword/change?token="  + token;
			return constructEmail(user, "パスワードリセットには下記URLへアクセスして行ってください" + "\r\n" + url);
	}
	
	private SimpleMailMessage constructEmail(SiteUser user, String body) {
		
		String subject = "パスワードリセットのご案内";
		
		
		SimpleMailMessage email = new SimpleMailMessage();
		email.setSubject(subject);
	    email.setText(body);
	    email.setTo(user.getMail());
	    
	    return email;
	}
	
	private String checkInputPassword(String password, String matchPassword) {
		
		if(!(password.equals(matchPassword))) {
			return "新しいパスワードと確認のパスワードが一致していません。";
		}
		
		if(password.matches("^(?=.[a-zA-Z0-9]*)(?=.*[a-z]).*(?=.*[A-Z]).*(?=.*\\d).*$") == false || password.length() < 8){
			return "8文字以上のアルファベット大文字小文字を組み合わせた値を設定してください";
		}
		
		return null;
	}
	
	
	
}
