package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.example.demo.google2FA.CustomAuthenticationProvider;
import com.example.demo.google2FA.CustomWebAuthenticationDetailsSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class Config extends WebSecurityConfigurerAdapter{

private final UserDetailsService userdetailservice;
	
	@Autowired
	private CustomWebAuthenticationDetailsSource authenticationDetailsSource;
	
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/register*","/login*","/register_account/confirm*","/badUser","/resendConfirmationEmail"
							,"/qrcode*").permitAll()
				.anyRequest().authenticated()
				.and()
			.formLogin()
				.authenticationDetailsSource(authenticationDetailsSource)
				.loginPage("/login")	
				.permitAll()
				.failureUrl("/login?error=true")
				.defaultSuccessUrl("/home", true)
				.and()
			.logout()
				.logoutUrl("/logout")
				.logoutSuccessUrl("/login")
				.deleteCookies("remember-me","XSRF-TOKEN","JSESSIONID")
				.and()
			.rememberMe().alwaysRemember(true)
			.userDetailsService(userdetailservice)
				;
		
			
		
		//多重ログイン・UserDetailsオブジェクト検索もしくはprincipalのoverride必要
		http.sessionManagement(session -> session
									.maximumSessions(1)
												.maxSessionsPreventsLogin(true)
												)
			;
		
	}
	
	
	
	@Override
	protected void configure(AuthenticationManagerBuilder authenticationManager) throws Exception {
		
		authenticationManager.authenticationProvider(authProvider());
	}
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/webjars/**", "/css/**","/js/**","/backImage/**");
	}
	
	@Bean
	public DaoAuthenticationProvider authProvider() {
		CustomAuthenticationProvider authProvider = new CustomAuthenticationProvider();
		authProvider.setUserDetailsService(userdetailservice);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}
	
	//同時セッションの設定
	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}
	
	
	
	
}
