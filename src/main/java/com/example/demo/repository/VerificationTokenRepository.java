package com.example.demo.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.SiteUser;
import com.example.demo.model.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken,Long>{
	VerificationToken findByToken(String token);
	VerificationToken findByUser(SiteUser siteUser);
	
	Collection<VerificationToken> deleteByUser(SiteUser siteUser);
}
