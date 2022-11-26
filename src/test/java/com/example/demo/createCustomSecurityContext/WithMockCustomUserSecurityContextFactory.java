package com.example.demo.createCustomSecurityContext;

import org.jboss.aerogear.security.otp.Totp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.example.demo.google2FA.CustomAuthenticationProvider;
import com.example.demo.google2FA.CustomWebAuthenticationDetailsSource;
import com.example.demo.model.SiteUser;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.service.UserDetailsServiceImple;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser>{

	@Autowired
	SiteUserRepository siteUserRepository;
	
	private UserDetailsServiceImple userDetailsServiceImpl;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	
	/*
	 * 概要
	 * ユーザー情報の登録
	 * AuthenticationへOTP登録
	 * SecurityContextへAuthenticationを保存
	 */
	@Override
	public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
		
		
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		
		MockHttpServletRequest request = new MockHttpServletRequest();	
		String secretKeyForOTP = "TestOTPCode";
		
		setOTPToHttpServletRequest(secretKeyForOTP, request);
		registerUserRepository(customUser, secretKeyForOTP);
		
		//SecurityContextで使用するAuthenticationの準備
		UserDetails principal = userDetailsServiceImpl.loadUserByUsername(customUser.Mail());
		UsernamePasswordAuthenticationToken testAuth = new UsernamePasswordAuthenticationToken(principal, customUser.password());
		addDetailToAuthentication(testAuth, request);
		
		context.setAuthentication(testAuth);
		return context;
		
	}
	
	
	@Autowired
	public WithMockCustomUserSecurityContextFactory(UserDetailsServiceImple userDetailsService) {
		this.userDetailsServiceImpl = userDetailsService;
	}
	
	private void registerUserRepository(WithMockCustomUser customUser, String secretKeyForOTP) {
		SiteUser testUser = new SiteUser();
		testUser.setUsername(customUser.username());
		testUser.setMail(customUser.Mail());
		testUser.setPassword(passwordEncoder.encode(customUser.password()));
		testUser.setSecret(secretKeyForOTP);
		testUser.setEnabled(true);
		siteUserRepository.save(testUser);
	}
	
	private void setOTPToHttpServletRequest(String secretKeyForOTP, MockHttpServletRequest request) {
		
		Totp totp = new Totp(secretKeyForOTP);
		String currentOTP = totp.now();
		request.setParameter("code", currentOTP);
	}

	private void addDetailToAuthentication(UsernamePasswordAuthenticationToken testAuth,MockHttpServletRequest request ) {

		CustomWebAuthenticationDetailsSource addDetails = new CustomWebAuthenticationDetailsSource();
		testAuth.setDetails(addDetails.buildDetails(request));
		
	}
}
