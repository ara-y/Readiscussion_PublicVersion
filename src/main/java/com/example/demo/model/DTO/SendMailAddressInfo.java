package com.example.demo.model.DTO;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMailAddressInfo {

	@NotNull
	@Pattern(regexp="^(?=.[a-zA-Z0-9]*)(?=.*[a-z]).*(?=.*\\d).*(?=.*-).*$")
	private String token;
	
	@NotNull
	@NotBlank
	@Pattern(regexp="^(?=.[a-zA-Z0-9]*)(?=.*[a-z]).*(?=.*[A-Z]).*(?=.*\\d).*$")
	@Size(min=4, max=60)
	private String password;
	
	//form外で一致確認すれば良いので不要
	@Pattern(regexp="^(?=.[a-zA-Z0-9]*)(?=.*[a-z]).*(?=.*[A-Z]).*(?=.*\\d).*$")
	@Size(min=4, max=60)
	private String matchPassword;
}
