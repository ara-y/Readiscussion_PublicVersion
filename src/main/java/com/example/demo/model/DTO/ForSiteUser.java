package com.example.demo.model.DTO;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForSiteUser {
	
	@Size(min=1,max=30)
	private String username;
	
	@Size(min=8, max=60)
	@Pattern(regexp="^(?=.[a-zA-Z0-9]*)(?=.*[a-z]).*(?=.*[A-Z]).*(?=.*\\d).*$")
	private String password;
	
	@Size(max=60)
	@NotBlank
	private String mail;
	
	private boolean isUsing2FA;
	
	public boolean getIsUsing2FA() {
		return this.isUsing2FA;
	}
	public void setIsUsing2FA(boolean isUsing2FA) {
		this.isUsing2FA = isUsing2FA;
	}

}
