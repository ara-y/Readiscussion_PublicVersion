package com.example.demo.authenticationTests;

import static org.junit.jupiter.api.Assertions.*;

import javax.transaction.Transactional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.demo.model.SiteUser;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.service.UserDetailsServiceImple;

@SpringBootTest //Spring Bootの機能を有効にする
@Transactional
class UserDetailsServiceImplTest {

	@Autowired
	SiteUserRepository siteUserRepository;
	
	@Autowired
	UserDetailsServiceImple userDetailsServiceImpl;
	
	@Test
	@DisplayName("メールが既に登録されている場合にユーザーの詳細オブジェクトを取得します")
	void whenMailExists_expectToGetUserDetails() {
		
		SiteUser user = new SiteUser();
		user.setMail("掲載のため削除");
		user.setPassword("掲載のため削除");
		user.setUsername("掲載のため削除");
		siteUserRepository.save(user);
		
		UserDetails actual = userDetailsServiceImpl.loadUserByUsername("edomon2022_11_21@docomo.ne.jpp");
		assertEquals(user.getMail(), actual.getUsername());
		
	}
	
	@Test
	@DisplayName("メールが存在しない場合は、例外をスローします。")
	void whenMailDoesNotExsists_throwException() {
		
		assertThrows(RuntimeException.class, () -> userDetailsServiceImpl.loadUserByUsername("someone_2022@docomo.ne.jpp"));
		
	}

}
