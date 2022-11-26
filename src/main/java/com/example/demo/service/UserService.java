package com.example.demo.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.error.UserAlreadyExistException;
import com.example.demo.model.DeleteAccountToken;
import com.example.demo.model.PasswordResetToken;
import com.example.demo.model.SiteUser;
import com.example.demo.model.VerificationToken;
import com.example.demo.model.DTO.SendSiteUserInformation;
import com.example.demo.repository.DeleteAccountTokenRepository;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.repository.VerificationTokenRepository;
import com.example.demo.service.Disccusion.CallDiscussionService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class UserService implements CallUserService{
	
	
	@Autowired
	private final DeleteAccountTokenRepository deleteAccountTokenRepository;
	
	@Autowired
	private final VerificationTokenRepository tokenRepository;
	
	@Autowired
	private final SiteUserRepository siteUserRepository;
	
	@Autowired
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	
	
	
	@Override
	public void createDeleteAccountToken(final SiteUser user, final String token) {
		final DeleteAccountToken myToken = new DeleteAccountToken(user, token);
		deleteAccountTokenRepository.save(myToken);
	}
	
	@Override
	public void createPasswordResetToken(SiteUser user, String token) {
		PasswordResetToken myToken = new PasswordResetToken(token, user);
		passwordResetTokenRepository.save(myToken);
	}
	
	@Override
	public void createVerificationTokenForUser(final SiteUser user, final String token) {
		final VerificationToken myToken = new VerificationToken(token, user);
		tokenRepository.save(myToken);
	}
	
	@Override
	public void deleteUser(SiteUser user) {
		
		deleteAccountTokenRepository.deleteByUser(user);
		tokenRepository.deleteByUser(user);
		passwordResetTokenRepository.deleteByUser(user);
		
		siteUserRepository.deleteByUsername(user.getUsername());
	}
	
	
	@Override
	public DeleteAccountToken getDeleteAccountToken(final String token) {
		return deleteAccountTokenRepository.findByToken(token);
	}
	
	

	@Override
	public String generateQRUrl(SiteUser user) throws UnsupportedEncodingException {
		final String QR_PREFIX = 
				  "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=";
		
		return QR_PREFIX + URLEncoder.encode(String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", 
															"OneWeek",
															user.getMail(),
															user.getSecret(),
															"OneWeek"),"UTF-8");
	}
	
	@Override
	public SiteUser getSiteUserByMail(final String mail) {
		return siteUserRepository.findByMail(mail);
	}
	
	@Override
	public PasswordResetToken getSiteUserByPasswordResetToken(String token) {
		return passwordResetTokenRepository.findByToken(token);
	}
	
	@Override
	public VerificationToken getVerificationToken(final String verificationToken) {
		return tokenRepository.findByToken(verificationToken);
	}
	
	@Override
	public VerificationToken getVerificationTokenBySiteUser(final SiteUser siteUser) {
		return tokenRepository.findByUser(siteUser);
	}
	
	@Override
	public void logoutProcess(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		
		request.logout();
		
		Cookie[] cookies = request.getCookies();
		for(Cookie cookie: cookies) {
			if("remember-me".equals(cookie.getName())) {
				
				cookie.setMaxAge(0);
				cookie.setPath("/");
				response.addCookie(cookie);
			}
			
			
		}
	}
	
	
	@Override
	public SiteUser registerNewUserAccount(final SendSiteUserInformation userinfo) {
		
		if(emailExists(userinfo.getMail())) {
			throw new UserAlreadyExistException("既に" + userinfo.getMail()+"は登録されています。");
		}
		
		final SiteUser user = new SiteUser();
		
        user.setUsername(userinfo.getUsername());
        user.setPassword(passwordEncoder.encode(userinfo.getPassword()));
        user.setMail(userinfo.getMail());
        user.setUsing2FA(userinfo.getIsUsing2FA());
        
        
        return siteUserRepository.save(user);
		
	}
	
	@Override
	public void savePassword(SiteUser user, String password) {
		user.setPassword(passwordEncoder.encode(password));
		siteUserRepository.save(user);
	}
	
	@Override
	public void saveRegisteredUser(final SiteUser user) {
		siteUserRepository.save(user);
	}
	
	@Override
	public String validatePasswordResetToken(String token) {
		final PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token);
		
		return !isTokenFound(resetToken) ? "トークンが無効です"
				:isTokenExpired(resetToken) ? "トークンの有効期限が切れています"
				:null;
	}
	
	//---オブジェクトのチェックなどに使用---
	
	private boolean emailExists(final String email) {
		return siteUserRepository.findByMail(email) != null;
	}
	
	private boolean isTokenFound(PasswordResetToken resetToken) {
		return resetToken != null;
	}
	
	private boolean isTokenExpired(PasswordResetToken resetToken) {
		final Calendar cal = Calendar.getInstance();
		return resetToken.getExpiryDate().getTime() < cal.getTime().getTime();
	}
}
