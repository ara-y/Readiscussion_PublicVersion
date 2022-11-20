package com.example.demo.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


//削除予定

@Configuration
public class WebConfig implements WebMvcConfigurer{
	
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolver) {
		PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
		resolver.setFallbackPageable(PageRequest.of(0, 5));
		argumentResolver.add(resolver);
	}
}
