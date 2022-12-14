package com.example.demo.controller.discussion;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import com.example.demo.service.CallUserService;
import com.example.demo.service.Disccusion.CallDiscussionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.error.LoseSessionException;
import com.example.demo.model.SiteUser;
import com.example.demo.model.discussion.ChatMessage;
import com.example.demo.model.discussion.Room;
import com.example.demo.model.discussion.DTO.RegisterAlreadyReadTime;
import com.example.demo.model.discussion.DTO.GetRoomList;
import com.example.demo.model.discussion.DTO.GetMessageLog;
import com.example.demo.model.discussion.DTO.SendChatMessage;
import com.example.demo.model.discussion.DTO.SendJoinRoomForm;
import com.example.demo.model.discussion.DTO.SendRoomManagementForm;


@Controller
public class DiscussionController {
	
	@Autowired
	CallDiscussionService callDiscussionService;
	
	@Autowired
	CallUserService callUserService;
	
	
	@GetMapping("/home")
	public String home() {
		return "discussion/home";
	}
	
	@GetMapping("/createRoom")
	public String createRoom(@ModelAttribute GetRoomList room) {
		return "discussion/createRoom";
	}
	
	@PostMapping("/createRoom")
	public String postCreateRoom(Authentication authentication,@Valid @ModelAttribute GetRoomList room, BindingResult result) throws ParseException {
		
		if(result.hasErrors()) {
			//?????????????????????????????????????????????????????????????????????
		}
		
		
		Room saveRoom = new Room();
		SiteUser user = callUserService.getSiteUserByMail(authentication.getName());
		
		UUID uuid = UUID.randomUUID();
		String enterRoom = uuid.toString();
		
		//???????????????????????????parseException????????????error????????????????????????
		callDiscussionService.registerRoom(room.getEndDiscussion(),room.isHasPassword(),room.getPassword(),room.getRoomName(),enterRoom,user,saveRoom,room);
		
		Room registerdRoom = callDiscussionService.getRoomByRoomURL(enterRoom);
		
		callDiscussionService.joinProcess(registerdRoom, authentication);
		
		return "redirect:/topic/sendMessage/" + enterRoom;
	}

	 
	@GetMapping("/joinRoom")
	public String joinRoom(Model model , @PageableDefault(page=0, size=3) Pageable pageable,HttpServletRequest request,
			@ModelAttribute("sendJoinRoomForm") SendJoinRoomForm sendJoinRoomForm) throws LoseSessionException{
		
		getRoomList(pageable, request, model);
			
		return "discussion/joinRoom";
	}
	
	
	@PostMapping("/joinRoom/moving")
	public String movingJoinRoom(@Valid @ModelAttribute("sendJoinRoomForm") SendJoinRoomForm sendJoinRoomForm, BindingResult result, Authentication authentication, 
			HttpServletRequest request,
			Model model, @PageableDefault(page=0, size=3) Pageable pageable) {
		
		// ?????????????????????????????????
		if(result.hasErrors()) {
			getRoomList(pageable, request, model);
			return "discussion/joinRoom";
		}
		
		//callDiscussioService???????????????????????????????????????
		String room = sendJoinRoomForm.getJoinId();
		Room joinRoom = callDiscussionService.getRoom(room);
		
		// ????????????
		if(joinRoom.isHasPassword() == false) {
			
			callDiscussionService.joinProcess(joinRoom, authentication);
			
			return "redirect:/topic/sendMessage/" + joinRoom.getRoomURL();
		}else {
			
			//???????????????????????????????????????
			if(joinRoom.getPassword().equals(sendJoinRoomForm.getJoinPassword())) {
				
				callDiscussionService.joinProcess(joinRoom, authentication);
				
				return "redirect:/topic/sendMessage/" + joinRoom.getRoomURL();
			}else {
				//??????????????????????????????????????????????????????????????? ?????????
				getRoomList(pageable, request , model);
				return "discussion/joinRoom";
			}
			
		}
		
	}	
	
	//URL????????????id???????????????????????????????????????
	@MessageMapping("/room/{id}")
	  @SendTo("/topic/sendMessage/{id}" )
	  public SendChatMessage SendChatMessage(ChatMessage message,@PathVariable String id, Authentication authentication) throws Exception {
		
	    Thread.sleep(1000);
	    String escapeMessage = HtmlUtils.htmlEscape(message.getMessage());
	    
	    callDiscussionService.saveMessage(message, message.getRoomId(), authentication);
	    
	    return new SendChatMessage(escapeMessage);
	  }
	
	@GetMapping("/topic/sendMessage/{id}")
	public String RoomPage(@PathVariable String id, Model model, Authentication authentication ,@PageableDefault(page=0, size=3) Pageable pageable, HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException, LoseSessionException {
		model.addAttribute("id", id);
		
		Page<ChatMessage> chatLog = callDiscussionService.getRoomMessage(id, pageable);
		
		try {
			DefaultCsrfToken token = (DefaultCsrfToken)	request.getSession().getAttribute("org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN"
				);
	
			if(chatLog.hasContent()) {
			
				List<ChatMessage> reverseLog = new ArrayList<>();
			
				//1page???????????????????????????????????????????????????
				for(int i = chatLog.getContent().size()-1; 0 <= i; i--) {
					reverseLog.add(chatLog.getContent().get(i));
				}
			
				model.addAttribute("page", chatLog);
				model.addAttribute("log", reverseLog);
				model.addAttribute("url", "/topic/sendMessage/" + id + "/getLog");
			
				model.addAttribute("_csrf", token.getToken());
				model.addAttribute("pageLimit", chatLog.getTotalPages());
				
		}
		
		
		}catch(NullPointerException e) {
			throw new LoseSessionException();
		}
		
			callDiscussionService.registerReadTime(authentication, id);
			
			model.addAttribute("isHost", callDiscussionService.isRoomHost(authentication, id));
			
		return "discussion/index";
		
	}
	
	@GetMapping("/topic/sendMessage/{roomURL}/roomManagement")
	public String roomManagement(@PathVariable String roomURL, Authentication authentication, Model model, @ModelAttribute("sendRoomManagementForm") SendRoomManagementForm form) {
		
		
		Room room = callDiscussionService.getRoomByRoomURL(roomURL);
		if(room.getAdminUser().getMail().equals(authentication.getName()) == false){
			// ?????????????????????????????????????????????????????????????????? ?????????
			return "/topic/sendMessage/" + roomURL;
		}
		
		model.addAttribute("room", roomURL);
		model.addAttribute("password", room.getPassword());
		model.addAttribute("name", room.getRoomName());
		
		return "discussion/roomManagement";
	}
	
	@Transactional
	@PostMapping("/topic/sendMessage/{roomURL}/roomManagement")
	public String postRoomManagement(@PathVariable String roomURL, Authentication authentication, Model model, @Valid @ModelAttribute("sendRoomManagementForm") SendRoomManagementForm form
										, BindingResult result) {
		//????????????????????????????????????????????????????????????????????????
		
		if(result.hasErrors()) {
			//?????????????????????????????? ?????????
		}
		
		if(form.isDeleteRoom()) {
			//???????????????????????????????????????????????????
			
			Room room = callDiscussionService.getRoomByRoomURL(roomURL);
			callDiscussionService.deleteRoom(room);
			
			return "redirect:/joinRoom";
		}else {
			
			return "regirect:/topic/sendMessage/" + roomURL + "/roomManagement";
		}
		
	}
	
	
	
	//?????????????????????????????? ?????????
	@GetMapping("/topic/sendMessage/{id}/getLog")
	@ResponseBody
	public String getJson(@PathVariable String id, Model model, Authentication auhentication ,@PageableDefault(page=0, size=3) Pageable pageable, HttpServletRequest request
			, Authentication authentication) throws JsonProcessingException {
		model.addAttribute("id", id);
		Page<ChatMessage> chatLog = callDiscussionService.getRoomMessage(id, pageable);
		
		System.out.println(chatLog.getSize());
		
		List<ChatMessage> reverseLog = new ArrayList<>();
		
		
		for(int i = chatLog.getContent().size()-1; 0 <= i; i--) {
			System.out.println(chatLog.getContent().get(i).getMessage());
			reverseLog.add(chatLog.getContent().get(i));
			
		}
		
		ObjectMapper mapper = new ObjectMapper();
		
		String allLog = "[";
		for(int i=0; i<reverseLog.size(); i++) {
			
			//id?????????????????????????????????????????????????????????
			
			ChatMessage log = reverseLog.get(i);
			GetMessageLog sendLog = new GetMessageLog();
			sendLog.setId(log.getUser_id());
			sendLog.setMessage(log.getMessage());
			
			String json = mapper.writeValueAsString(sendLog);
			if(i <= reverseLog.size()-2) {
				allLog += json + ",";
			}else {
				allLog += json;
			}
		}
		
		allLog = allLog.concat("]");
		
		callDiscussionService.registerReadTime(authentication, id);
		
		return allLog;
	}
	
	@MessageMapping("/updateReadTime")
	public void updateReadTime(Authentication authentication, RegisterAlreadyReadTime forAlreadyReadTime) {
		//???????????????????????????????????????????????????????????????????????????????????? ?????????
		String roomId = HtmlUtils.htmlEscape(forAlreadyReadTime.getRoomURL());
		callDiscussionService.registerReadTime(authentication, roomId);
	}
	
	
	//error?????????????????????????????????????????????????????????????????????????????????????????????
	@ExceptionHandler(LoseSessionException.class)
	public String regetSession(Model model) {
		model.addAttribute("error", "???????????????????????????");
		return "/error";
	}
	
	
	// ---------------------
	private void getRoomList(Pageable pageable , HttpServletRequest request, Model model) {
		Page<Room> roomPage = callDiscussionService.getAllRoom(pageable);
		
		
		// ??????????????????????????????????????????????????????
		// null???????????????????????????
		model.addAttribute("page", roomPage);
		model.addAttribute("rooms", roomPage.getContent());
		model.addAttribute("url", "/joinRoom");
		model.addAttribute("numberOfLogs", roomPage.getContent().size());
		try {
		DefaultCsrfToken token = (DefaultCsrfToken)	request.getSession().getAttribute("org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN"
				);
		model.addAttribute("_csrf", token.getToken());
		}catch(NullPointerException e) {
			throw new LoseSessionException();
		}
	}
	

	
	
}
