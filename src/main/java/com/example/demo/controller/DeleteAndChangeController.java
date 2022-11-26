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
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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
import com.example.demo.event.OnPasswordResetCompleteEvent;
import com.example.demo.model.DeleteAccountToken;
import com.example.demo.model.SiteUser;
import com.example.demo.model.DTO.SendMailAddressInfo;
import com.example.demo.service.CallUserService;
import com.example.demo.service.Disccusion.CallDiscussionService;

@Controller
public class DeleteAndChangeController {
	
	@Autowired
	CallUserService callUserService;
	
	@Autowired
	CallDiscussionService callDiscussionService;
	
	
	@Autowired
	ApplicationEventPublisher publisher;
	
	@Autowired
	JavaMailSender mailSender;
	
	
	@GetMapping("/deleteAccount")
	public String deleteAccount( ) {
		return "deleteAccount";
	}
	
	@PostMapping("/deleteAccount")
	public String sendEmailForDeleteAccount(Authentication authentication,HttpServletRequest request,Model model) {
			
		
		try {
				SiteUser siteUser = callUserService.getSiteUserByMail(authentication.getName());
			
				String appUrl = request.getScheme() +"://"+ request.getServerName() + ":" + request.getServerPort();
			
				publisher.publishEvent(new OnDeleteAccountCompleteEvent(siteUser,appUrl));
		}catch(RuntimeException ex) {
			model.addAttribute("message","メールの送信ができませんでした。");
			return "deleteAccount";
		}
		
			model.addAttribute("message","アカウント削除のメールを送らせていただきました。");
		return "deleteAccount";
	}
	
	@Transactional
	@GetMapping("/deleteAccount/confirm")
	public String deleteAccountConfirm(@RequestParam("token") String token, Model model , HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
		
		DeleteAccountToken deleteAccountToken = callUserService.getDeleteAccountToken(token);
		
		
		if(deleteAccountToken == null) {
			model.addAttribute("message", "無効なトークンのため削除ができませんでした。アカウント削除のメールを再送信してください。");
			return "/deleteAccount";
		}
		
		Calendar cal = Calendar.getInstance();
		
		if(deleteAccountToken.getExpiryDate().getTime() - cal.getTime().getTime() < 0) {
			model.addAttribute("message", "トークンの有効期限が切れているため削除できませんでした。アカウント削除のメールを再送信しトークンを再発行してください。");
			return "/deleteAccount";
		}
		
		SiteUser siteUser = deleteAccountToken.getUser();
		
		
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
	
	@PostMapping("/resetPassword")
	public void sendMailForResetPassword(@RequestParam("mail") String mail, HttpServletRequest request,Model model,Authentication authentication) {
		
		SiteUser user = callUserService.getSiteUserByMail(mail);
		if(user == null) {
			throw new UserNotFoundException();
		}
		
		//エラーメッセージ未実装
		if((mail.equals(authentication.getName())) == false) {
			System.out.println("送れません");
		}
		
	
		String contextPath = request.getScheme() +"://"+ request.getServerName() + ":" + request.getServerPort();
		publisher.publishEvent(new OnPasswordResetCompleteEvent(user, contextPath));
		
		
		//cookieの中にあるcsrfトークンを呼び出す。
		for(int i =0; i<request.getCookies().length; i++) {
			if(request.getCookies()[i].getName().equals("XSRF-TOKEN")) {
				model.addAttribute("_csrf", request.getCookies()[i].getValue());
			}
		}
	}
	
	
	@GetMapping("/resetPassword/change")
	public String checkChangePasswordToken(@RequestParam("token") String token,Model model) {
		
		String result = callUserService.validatePasswordResetToken(token);
		if(result != null) {
			return "redirect:/login?message=" + result;
		}else {
			return "redirect:/resetPassword/update?token=" + token;
		}
	}
	
	@GetMapping("/resetPassword/update")
	public String inputNewPassword(@ModelAttribute("passwordDTO")SendMailAddressInfo passwordDTO) {
		return "updatePassword";
	}
	
	@PostMapping("/resetPassword/update")
	public String updatePassword(@Validated @ModelAttribute("passwordDTO")SendMailAddressInfo passwordDTO, BindingResult errorResult,Model model) {
		
		
		if(errorResult.hasErrors()) {
			System.out.println("エラーメッセージ実装予定");
			return "redirect:/resetPassword/change?token=" + passwordDTO.getToken();
		}
	
		
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
