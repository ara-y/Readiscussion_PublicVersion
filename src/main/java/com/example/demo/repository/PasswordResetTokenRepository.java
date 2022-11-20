package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.PasswordResetToken;
import com.example.demo.model.SiteUser;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long>{
	
	PasswordResetToken findByToken(String token);
	
	void deleteByUser(SiteUser user);

}
