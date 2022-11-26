package com.example.demo.model.discussion;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

@Getter
@Setter
@Entity
public class AlreadyReadTime {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@ManyToOne
	private Room targetRoom;
	
	
	@ManyToOne
	private SiteUser alreadyReadUser;
	
	private LocalDateTime alreadyReadTime;
	
	

}
