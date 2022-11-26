package com.example.demo.model.discussion.DTO;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterAlreadyReadTime {
	
	private String roomURL;
	
	public RegisterAlreadyReadTime() {
	}

}
