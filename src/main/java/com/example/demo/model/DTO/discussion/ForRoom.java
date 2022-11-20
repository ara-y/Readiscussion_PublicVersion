package com.example.demo.model.DTO.discussion;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForRoom {
	private String roomName;
	private String endDiscussion;
	private String password;
	private boolean hasPassword;
}
