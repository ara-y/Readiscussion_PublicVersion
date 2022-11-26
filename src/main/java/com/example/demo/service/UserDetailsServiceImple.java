package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.SiteUser;
import com.example.demo.repository.SiteUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserDetailsServiceImple implements UserDetailsService{
	private final SiteUserRepository siteUserRepository;
	
	@Override
	public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException{
		SiteUser user = siteUserRepository.findByMail(mail);
		
		//各状態が未実装のため仮で状態をtrueとする
		boolean accountNonExpired = true;
		boolean credentialsNonExpired = true;
		boolean accountNonLocked = true;
			
		try {
		if(user == null) {
			throw new UsernameNotFoundException("not found");
		}
		
		List<GrantedAuthority> authority = new ArrayList<>();
		authority.add(new SimpleGrantedAuthority("ROLE_USER"));
		
		return new User(
				user.getMail(),
				user.getPassword(),
				user.isEnabled(),
				accountNonExpired,
				credentialsNonExpired,
				accountNonLocked,
				authority);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
