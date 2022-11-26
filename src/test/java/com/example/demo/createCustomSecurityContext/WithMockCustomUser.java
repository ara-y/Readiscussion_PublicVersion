package com.example.demo.createCustomSecurityContext;

import java.lang.annotation.Retention;

import org.springframework.security.test.context.support.WithSecurityContext;
import java.lang.annotation.RetentionPolicy;

//カスタマイズ用のSecurityContextで使用するユーザ情報
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {
	String username() default "掲載のため削除";
	String password() default "掲載のため削除";
	String Mail() default "掲載のため削除";
}
