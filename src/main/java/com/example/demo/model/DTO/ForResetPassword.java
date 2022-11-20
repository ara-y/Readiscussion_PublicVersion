package com.example.demo.model.DTO;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForResetPassword {

	private String token;
	
	private String password;
	
	private String matchPassword;
}
