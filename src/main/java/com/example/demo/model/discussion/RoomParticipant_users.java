package com.example.demo.model.discussion;

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
public class RoomParticipant_users {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@ManyToOne
	@JoinColumn(name = "roomId_id")
	
	private Room roomId;
	
	//命名ミス
	@ManyToOne
	@JoinColumn(name = "partcipant_id")
	private SiteUser participant;
	
	public RoomParticipant_users() {
		
	}

}
