package com.example.demo.model.discussion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.example.demo.model.SiteUser;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Room {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private String roomName;
	
	
	@ManyToOne
    @JoinColumn(name="adminUser_id", nullable=false)
	private SiteUser adminUser;
	
	//命名ミス
	@OneToMany(mappedBy = "roomId", cascade=CascadeType.ALL)
	
	private List<RoomParticipant_users> participatingUsers;
	
	@OneToMany(mappedBy = "targetRoom" , cascade=CascadeType.ALL)
	
	private List<AlreadyReadTime> users_ReadTime;
	
	@OneToMany(mappedBy = "postedRoom" , cascade=CascadeType.ALL)
	
	private List<ChatMessage> messages;
	
	private Date startDiscussion;
	
	private Date endDiscussion;
	
	@Pattern(regexp = "^[a-zA-Z0-9]+$")
	private String password;
	
	private boolean hasPassword;
	
	private String roomURL;
	
	
}
