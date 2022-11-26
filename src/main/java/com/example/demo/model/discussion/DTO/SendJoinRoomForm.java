package com.example.demo.model.discussion.DTO;

import javax.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendJoinRoomForm {

	@Pattern(regexp = "^[0-9]+$")
	private String joinId;
	
	@Pattern(regexp = "^[a-zA-Z0-9]+$")
	private String joinPassword;
	
	
}
