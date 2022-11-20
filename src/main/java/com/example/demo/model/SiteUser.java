package com.example.demo.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.jboss.aerogear.security.otp.api.Base32;

import com.example.demo.model.discussion.AlreadyReadTime;
import com.example.demo.model.discussion.ChatMessage;
import com.example.demo.model.discussion.Room;
import com.example.demo.model.discussion.RoomParticipant_users;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SiteUser {
	
	@Id
	@Column(unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@NotBlank
	private String username;
	
	private String display_name;
	
	@Size(min=4, max=60)
	private String password;
	
	@NotBlank
	private String mail;
	
	private boolean enabled; 
	
	private boolean isUsing2FA;
	
	private String secret;
	
	@OneToMany(mappedBy = "participant")
	private List<RoomParticipant_users> participant;
	
	@OneToMany(mappedBy = "alreadyReadUser")
	private List<AlreadyReadTime> alreadyReadTime;
	
	@OneToMany(mappedBy="adminUser")
	private List<Room> hostingRoom;
	
	
	
	public SiteUser() {
		super();
		this.enabled = false;
		this.secret = Base32.random();
	}
	
}
