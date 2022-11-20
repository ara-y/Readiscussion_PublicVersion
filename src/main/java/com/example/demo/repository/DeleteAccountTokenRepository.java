package com.example.demo.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.DeleteAccountToken;
import com.example.demo.model.SiteUser;

public interface DeleteAccountTokenRepository extends JpaRepository<DeleteAccountToken, Long>{

	DeleteAccountToken findByToken(String token);
	
	Collection<DeleteAccountToken> deleteByUser(SiteUser siteUser);
}
