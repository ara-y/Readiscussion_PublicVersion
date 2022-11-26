package com.example.demo.google2FA;


import org.jboss.aerogear.security.otp.Totp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;

import com.example.demo.model.SiteUser;
import com.example.demo.repository.SiteUserRepository;


public class CustomAuthenticationProvider extends DaoAuthenticationProvider{
	
	@Autowired
	private SiteUserRepository siteUserRepository;
	
	@Override
	public Authentication authenticate(Authentication auth) throws AuthenticationException{
		
		String verificationCode = ((CustomWebAuthenticationDetails) auth.getDetails()).getVerificationCode();
		
		SiteUser user = siteUserRepository.findByMail(auth.getName());
		if(user == null) {
			throw new BadCredentialsException("ログインに失敗しました。");
		}
		
		if(user.isUsing2FA()) {
			Totp totp = new Totp(user.getSecret());
			
			
			
			if(!isValidLong(verificationCode) || !totp.verify(verificationCode)) {
				throw new BadCredentialsException("ログインに失敗しました。");
			}
		}
		
		Authentication result = super.authenticate(auth);
		
		return new UsernamePasswordAuthenticationToken(result.getPrincipal(), result.getCredentials(), result.getAuthorities());	
	}
	
	private boolean isValidLong(String code) {
		try {
			Long.parseLong(code);
		}catch(NumberFormatException e) {
			return false;
		}
		
		return true;
	}
	

}
