package com.example.demo.service.Disccusion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.SiteUser;
import com.example.demo.model.discussion.AlreadyReadTime;
import com.example.demo.model.discussion.ChatMessage;
import com.example.demo.model.discussion.Room;
import com.example.demo.model.discussion.RoomParticipant_users;
import com.example.demo.model.discussion.DTO.GetRoomList;
import com.example.demo.repository.DeleteAccountTokenRepository;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.repository.VerificationTokenRepository;
import com.example.demo.repository.discussion.AlreadyReadTimeRepository;
import com.example.demo.repository.discussion.ChatMessageRepository;
import com.example.demo.repository.discussion.RoomParticipant_usersRepository;
import com.example.demo.repository.discussion.RoomRepository;
import com.example.demo.service.CallUserService;
import com.example.demo.service.UserService;

import lombok.RequiredArgsConstructor;


@Service
@Transactional
public class DiscussionService implements CallDiscussionService{
	
	@Autowired
	RoomRepository roomRepository;
	
	@Autowired
	SiteUserRepository siteUserRepository;
	
	@Autowired
	ChatMessageRepository chatMessageRepository;
	
	@Autowired
	RoomParticipant_usersRepository roomParticipant_usersRepository;
	
	@Autowired
	AlreadyReadTimeRepository alreadyReadTimeRepository;
	
	@Autowired
	CallUserService callUserService;
	
	@Override
	public Date ConvertToDate(String stringDate) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = dateFormat.parse(stringDate);
		return date;
	}
	
	@Override
	public Date StartDiscussion() {
		Date date = new Date();
		return date;
	}
	
	@Override
	public void deleteRoom(Room room) {
		roomRepository.delete(room);
	}
	
	@Override
	public void deleteRoomParticipant_users(SiteUser user) {
		roomParticipant_usersRepository.deleteByParticipant(user);
		
	}
	
	@Override
	public void deleteAlreadyReadTime(SiteUser user) {
		alreadyReadTimeRepository.deleteByAlreadyReadUser(user);
		
	}
	
	
	@Override
	public boolean exsistsParticipant(SiteUser user, Room room) {
		return roomParticipant_usersRepository.existsByParticipantAndRoomId(user, room);
	}
	
	@Override
	public void joinProcess(Room joinRoom, Authentication authentication) {
		
		String userName = authentication.getName();
		SiteUser user = callUserService.getSiteUserByMail(userName);
		
		//既に参加している場合と初参加の場合
		if(exsistsParticipant(user, joinRoom) == false) {
			RoomParticipant_users setParticipant = new RoomParticipant_users();
			setParticipant.setRoomId(joinRoom);
			setParticipant.setParticipant(user);
			saveRoomParticipant(setParticipant);
			
		}
		
		
	}
	
	
	@Override
	public Page<Room> getAllRoom(Pageable pageable){
		return roomRepository.findAll(pageable);
	}
	
	@Override
	public Page<ChatMessage> getRoomMessage(String id, Pageable pageable) {
		return chatMessageRepository.findByRoomId(id, pageable);
	}
	
	@Override
	public Room getRoom(String roomId) {
		int id = Integer.parseInt(roomId);
		return roomRepository.findById(id);
	}
	
	@Override
	public Room getRoomByRoomURL(String roomURL) {
		return roomRepository.findByRoomURL(roomURL);
	}
	
	@Override
	public boolean isRoomHost(Authentication authentication, String roomURL) {
		
		Room room = getRoomByRoomURL(roomURL);
		
		return room.getAdminUser().getMail().equals(authentication.getName());
	}
	
	
	@Override
	public void registerReadTime(Authentication authentication, String roomURL) {
		
		String userName = authentication.getName();
		SiteUser user = siteUserRepository.findByMail(userName);
		
		Room room = getRoomByRoomURL(roomURL);
		LocalDateTime now = LocalDateTime.now();
		
		AlreadyReadTime readNow = new AlreadyReadTime();
		
		AlreadyReadTime pastRead = alreadyReadTimeRepository.findByAlreadyReadUserAndTargetRoom(user, room);
		if(pastRead == null) {
			readNow.setTargetRoom(room);
			readNow.setAlreadyReadUser(user);
			readNow.setAlreadyReadTime(now);
			alreadyReadTimeRepository.save(readNow);
		}else {
			pastRead.setAlreadyReadTime(now);
		}
		
		
		}
	
	@Override
	public void registerRoom(String endDiscussion,  boolean hasPassword, String password, String roomName, String roomURL,SiteUser user, Room saveRoom, GetRoomList room) throws ParseException {
		saveRoom.setEndDiscussion(ConvertToDate(endDiscussion));
		saveRoom.setStartDiscussion(StartDiscussion());
		saveRoom.setHasPassword(room.isHasPassword());
		saveRoom.setPassword(room.getPassword());
		saveRoom.setRoomName(room.getRoomName());
		saveRoom.setRoomURL(roomURL);
		saveRoom.setAdminUser(user);
		
		roomRepository.save(saveRoom);
	}
	
	@Override
	public void saveMessage(ChatMessage message, String roomId, Authentication authentication) {
		
		String display_name = siteUserRepository.findByMail(authentication.getName()).getDisplay_name();
		int userId = siteUserRepository.findByMail(authentication.getName()).getId();
		
		Calendar cal = Calendar.getInstance();
		Date created_at = cal.getTime();
		
		message.setUser_id(userId);
		message.setDisplay_name(display_name);
		message.setRoomId(roomId);
		message.setCreatedAt(created_at);
		
		chatMessageRepository.save(message);
	}
	
	@Override
	public void saveRoomParticipant(RoomParticipant_users participant) {
		roomParticipant_usersRepository.save(participant);
	}
	
	@Override
	public void autoTransferRoomAdminPrivilege(SiteUser user) {
		int numberOfHostingRoom = user.getHostingRoom().size();
		
		//ユーザーがホストしている部屋があるかどうか
		if(numberOfHostingRoom != 0) {
			
		for(int i=0; i< numberOfHostingRoom; i++) {
			
			Room hostingRoom = user.getHostingRoom().get(i);
			
			//ホストしている部屋の参加者が他にいる場合
			if(hostingRoom.getParticipatingUsers().isEmpty() != true && hostingRoom.getParticipatingUsers().size() != 1) {
					List<RoomParticipant_users> participants = user.getHostingRoom().get(i).getParticipatingUsers();
					
					//他の参加者へ管理権限の移譲をする　DBリセットも忘れずに
					for(int t=0; t<participants.size(); t++) {
						SiteUser otherParticipant = participants.get(t).getParticipant();
						if(otherParticipant.getMail().equals(user.getMail())) {
							continue;
						}
						
						//管理権限の移譲
						hostingRoom.setAdminUser(otherParticipant);
						transferRoomAdminPrivilege(hostingRoom);
						break;
					}
					
			}else {
					deleteRoom(hostingRoom);
			}
		}
		
		}
		
	}
	
	@Override
	public void transferRoomAdminPrivilege(Room room) {
		roomRepository.save(room);
	}
}
