package com.example.demo.createCustomSecurityContext;

import java.lang.annotation.Retention;

import org.springframework.security.test.context.support.WithSecurityContext;
import java.lang.annotation.RetentionPolicy;

//カスタマイズ用のSecurityContextで使用するユーザ情報
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {
	String username() default "test@test2022.test.jp";
	String password() default "Akdk90000";
	String Mail() default "test@test2022.test.jp";
}
