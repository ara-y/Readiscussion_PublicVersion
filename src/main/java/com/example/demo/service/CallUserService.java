package com.example.demo.service;

import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.demo.error.UserAlreadyExistException;
import com.example.demo.model.DeleteAccountToken;
import com.example.demo.model.PasswordResetToken;
import com.example.demo.model.SiteUser;
import com.example.demo.model.VerificationToken;
import com.example.demo.model.DTO.SendSiteUserInformation;

public interface CallUserService {
	//-----login and registration-----------
	void createPasswordResetToken(SiteUser user, String token);
	
	void createVerificationTokenForUser(SiteUser user, String token);
	
	String generateQRUrl(SiteUser user)throws UnsupportedEncodingException;
	
	SiteUser getSiteUserByMail(String mail);
	
	VerificationToken getVerificationToken(String verificationToken) ;
	
	VerificationToken getVerificationTokenBySiteUser(SiteUser siteUser);
	
	void logoutProcess(HttpServletRequest request, HttpServletResponse response)throws ServletException;
	
	SiteUser registerNewUserAccount(SendSiteUserInformation siteuser) throws UserAlreadyExistException;
	
	void saveRegisteredUser(SiteUser user);
	
	//------------delete and change-------------------
	
	void createDeleteAccountToken(SiteUser user, String token);
	
	DeleteAccountToken getDeleteAccountToken(String token);
	
	void deleteUser(SiteUser user);
	
	String validatePasswordResetToken(String token);
	
	PasswordResetToken getSiteUserByPasswordResetToken(String token);
	
	void savePassword(SiteUser user, String password);
	
}
