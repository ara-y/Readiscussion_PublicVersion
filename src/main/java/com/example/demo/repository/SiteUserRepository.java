package com.example.demo.repository;

import java.util.Collection;

import org.springframework.data.repository.CrudRepository;

import com.example.demo.model.SiteUser;

public interface SiteUserRepository extends CrudRepository<SiteUser, Integer>{
	SiteUser findByMail(String mail);
	
	Collection<SiteUser> deleteByUsername(String username);
}
