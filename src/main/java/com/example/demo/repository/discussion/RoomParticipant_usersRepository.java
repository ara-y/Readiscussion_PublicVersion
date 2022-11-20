package com.example.demo.repository.discussion;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.PasswordResetToken;
import com.example.demo.model.SiteUser;
import com.example.demo.model.discussion.Room;
import com.example.demo.model.discussion.RoomParticipant_users;

public interface RoomParticipant_usersRepository   extends JpaRepository<RoomParticipant_users, Long>{

	boolean existsByParticipantAndRoomId(SiteUser user, Room room);
	
	void deleteByParticipant(SiteUser user);
	

}
