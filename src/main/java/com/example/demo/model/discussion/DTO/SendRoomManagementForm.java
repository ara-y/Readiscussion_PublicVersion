package com.example.demo.model.discussion.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendRoomManagementForm {
	
	private String roomName;
	
	private String roomPassword;
	
	private boolean deleteRoom;
	
	private String roomURL;
	
	public SendRoomManagementForm() {
		// TODO 自動生成されたコンストラクター・スタブ
	}

}
