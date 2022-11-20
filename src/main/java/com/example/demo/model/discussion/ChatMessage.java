package com.example.demo.model.discussion;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.example.demo.model.SiteUser;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ChatMessage {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	//SiteUserじゃない？→IDでいい。アカウント削除しても残る仕様
	private int user_id;
	
	private String display_name;
	
	private String message;
	
	//RoomIdは不要・削除予定
	private String roomId;
	
	private String remoteAddress;
	
	//登録タイミングで追加できるようにしたい
	private Date createdAt;
	
	@ManyToOne
	@JoinColumn(name = "postedRoom_id")
	
	private Room postedRoom;
	
	
	
	public ChatMessage() {
		
	}
	
	public ChatMessage(String message) {
			this.message = message;
		}
}
