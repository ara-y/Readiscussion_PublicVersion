package com.example.demo.service.Disccusion;

import java.text.ParseException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import com.example.demo.model.SiteUser;
import com.example.demo.model.DTO.discussion.ForRoom;
import com.example.demo.model.discussion.ChatMessage;
import com.example.demo.model.discussion.Room;
import com.example.demo.model.discussion.RoomParticipant_users;

public interface CallDiscussionService {
	Date ConvertToDate(String date) throws ParseException;
	Date StartDiscussion();
	
	void deleteRoom(Room room);
	
	void deleteRoomParticipant_users(SiteUser user);
	
	void deleteAlreadyReadTime(SiteUser user);
	
	boolean exsistsParticipant(SiteUser user, Room room);
	
	Page<Room> getAllRoom(Pageable pageable);
	
	Page<ChatMessage> getRoomMessage(String id , Pageable pageable);
	
	Room getRoom(String roomId);
	
	Room getRoomByRoomURL(String roomURL);
	
	boolean isRoomHost(Authentication authentication, String roomURL);
	
	void registerRoom(String endDiscussion, boolean hasPassword, String password, String roomName, String roomURL,SiteUser user, Room saveRoom, ForRoom room) throws ParseException;
	
	void registerReadTime(Authentication authentication, String roomURL);
	
	void saveMessage(ChatMessage message, String roomId, Authentication authentication);
	
	void autoTransferRoomAdminPrivilege(SiteUser user);
	
	void transferRoomAdminPrivilege(Room room);
	
	void joinProcess(Room joinRoom, Authentication authentication);
	
	void saveRoomParticipant(RoomParticipant_users participant);
	
	
	
}
