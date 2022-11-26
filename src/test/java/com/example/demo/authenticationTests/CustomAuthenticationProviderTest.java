package com.example.demo.authenticationTests;

import static org.junit.jupiter.api.Assertions.*;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.jboss.aerogear.security.otp.Totp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;

import com.example.demo.createCustomSecurityContext.WithMockCustomUser;
import com.example.demo.google2FA.CustomAuthenticationProvider;
import com.example.demo.google2FA.CustomWebAuthenticationDetails;
import com.example.demo.google2FA.CustomWebAuthenticationDetailsSource;
import com.example.demo.model.SiteUser;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.service.UserDetailsServiceImple;


@SpringBootTest //Spring Bootの機能を有効にする
@Transactional
class CustomAuthenticationProviderTest {

	@Autowired
	SiteUserRepository siteUserRepository;
	
	CustomAuthenticationProvider customAuthenticationProvider;
	
	@Autowired
	UserDetailsServiceImple userDetailsServiceImpl;
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Test
	@WithMockCustomUser
	void whenOTPUserExists_expectToGetAuthenticationObject() {
	
		Authentication authentication = (Authentication)SecurityContextHolder.getContext().getAuthentication();
		
		customAuthenticationProvider.setUserDetailsService(userDetailsServiceImpl);
		customAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		Authentication authenticatedUser =  customAuthenticationProvider.authenticate(authentication);
		
		assertEquals(authentication.getName(), authenticatedUser.getName());
		
	}
	
	//試しにコンストラクタインジェクション
	@Autowired
	CustomAuthenticationProviderTest(CustomAuthenticationProvider customAuthenticationProvider){
		this.customAuthenticationProvider = customAuthenticationProvider;
	}

}
