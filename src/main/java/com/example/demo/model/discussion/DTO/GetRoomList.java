package com.example.demo.model.discussion.DTO;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetRoomList {
	private String roomName;
	private String endDiscussion;
	private String password;
	private boolean hasPassword;
}
