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
import com.example.demo.model.DTO.discussion.ForRoom;
import com.example.demo.model.DTO.discussion.ForSendLog;
import com.example.demo.model.discussion.ChatMessage;
import com.example.demo.model.discussion.Room;
import com.example.demo.model.discussion.DTO.ForAlreadyReadTime;
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
	public String createRoom(@ModelAttribute ForRoom room) {
		return "discussion/createRoom";
	}
	
	@PostMapping("/createRoom")
	public String postCreateRoom(Authentication authentication,@Valid @ModelAttribute ForRoom room, BindingResult result) throws ParseException {
		
		if(result.hasErrors()) {
			//syori
		}
		
		
		Room saveRoom = new Room();
		SiteUser user = callUserService.getSiteUserByMail(authentication.getName());
		
		UUID uuid = UUID.randomUUID();
		String enterRoom = uuid.toString();
		
		//parseException errorメッセージ
		callDiscussionService.registerRoom(room.getEndDiscussion(),room.isHasPassword(),room.getPassword(),room.getRoomName(),enterRoom,user,saveRoom,room);
		
		Room registerdRoom = callDiscussionService.getRoomByRoomURL(enterRoom);
		callDiscussionService.joinProcess(registerdRoom, authentication);
		
		return "redirect:/topic/sendMessage/" + enterRoom;
	}

	//ページングで部屋一覧取得 @PagebleDefaultで設定の上書きできる。 
	@GetMapping("/joinRoom")
	public String joinRoom(Model model , @PageableDefault(page=0, size=3) Pageable pageable,HttpServletRequest request,
			@ModelAttribute("sendJoinRoomForm") SendJoinRoomForm sendJoinRoomForm) throws LoseSessionException{
		/*
		Page<Room> roomPage = callDiscussionService.getAllRoom(pageable);
		
		
		// 部屋は日付順で表示　検索機能も追加予定
		// nullの時どうするか
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
		*/
		getRoomList(pageable, request, model);
		
		
		return "discussion/joinRoom";
	}
	
	
	@PostMapping("/joinRoom/moving")
	public String joinRoom_moving(@Valid @ModelAttribute("sendJoinRoomForm") SendJoinRoomForm sendJoinRoomForm, BindingResult result, Authentication authentication, 
			HttpServletRequest request,
			Model model, @PageableDefault(page=0, size=3) Pageable pageable) {
		
		//まだ未完成 エラーメツセージ追加予定
		if(result.hasErrors()) {
			getRoomList(pageable, request, model);
			return "discussion/joinRoom";
		}
		
//callDiscussioService内に処理を書いた方がいいのでは？
		String room = sendJoinRoomForm.getJoinId();
		Room joinRoom = callDiscussionService.getRoom(room);
		
		// 入室処理
		if(joinRoom.isHasPassword() == false) {
			
			//ここにもjoinProcess　未実装
			
			return "redirect:/topic/sendMessage/" + joinRoom.getRoomURL();
		}else {
			
			//パスワードが一致しているか
			if(joinRoom.getPassword().equals(sendJoinRoomForm.getJoinPassword())) {
				
				callDiscussionService.joinProcess(joinRoom, authentication);
				
				return "redirect:/topic/sendMessage/" + joinRoom.getRoomURL();
			}else {
				//メッセージを追加して表示 未実装
				getRoomList(pageable, request , model);
				return "discussion/joinRoom";
			}
			
		}
		
	}
	
	//廃止予定
	//password有効の場合は、一度POSTして検証と遷移を行う仕組みにする。例えばこのURLでパスワードの有無チェック→パスワード入力欄を返すといった仕組み
	@GetMapping("/joinRoom/move")
	public String joinRoom_move(@RequestParam("room")String room) {
		
		Room joinRoom = callDiscussionService.getRoom(room);
		
		return "redirect:/topic/sendMessage/" + joinRoom.getRoomURL();
		
	}
	
	
	
	//idは辞めた方がいい。ChatMessageとバインドされている。RoomIdもJSONで受け取れるので。　　　文字数制限かけて。
	@MessageMapping("/room/{id}") //hello/{id}
	  @SendTo("/topic/sendMessage/{id}" )//topic/greetings/{id} 　　　　　　　　　　　　　　　　　
	  public SendChatMessage SendChatMessage(ChatMessage message,@PathVariable String id, Authentication authentication) throws Exception {
		
	    Thread.sleep(1000); // simulated delay
	    
	    
	    String escapeMessage = HtmlUtils.htmlEscape(message.getMessage());
	    
	    callDiscussionService.saveMessage(message, message.getRoomId(), authentication);
	    System.out.println(id);
	    
	    
	    return new SendChatMessage(escapeMessage);
	  }
	
	@GetMapping("/topic/sendMessage/{id}")
	public String messageIndex(@PathVariable String id, Model model, Authentication authentication ,@PageableDefault(page=0, size=3) Pageable pageable, HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException, LoseSessionException {
		model.addAttribute("id", id);
		

		
		Page<ChatMessage> chatLog = callDiscussionService.getRoomMessage(id, pageable);
		
		/*
		if(request.getSession() != null) {
		DefaultCsrfToken token = (DefaultCsrfToken)	request.getSession().getAttribute("org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN"
				);
		Cookie cookie = new Cookie("X-CSRF-TOKEN", token.getToken());
		response.addCookie(cookie);
		}
		*/
		
		
		try {
			DefaultCsrfToken token = (DefaultCsrfToken)	request.getSession().getAttribute("org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN"
				);
		
		
		
		
		if(chatLog.hasContent()) {
			
	//部品化したい・IndexOutOfBoundsExceptionの対処
			List<ChatMessage> reverseLog = new ArrayList<>();
			
			//1pageあたりの履歴の数に対して操作を行う
			for(int i = chatLog.getContent().size()-1; 0 <= i; i--) {
				reverseLog.add(chatLog.getContent().get(i));
			}
		
			model.addAttribute("page", chatLog);
			model.addAttribute("log", reverseLog);
			model.addAttribute("url", "/topic/sendMessage/" + id + "/getLog");
			
			System.out.println(request.getContentLength());
			
			
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
	
	//命名変更予定
	@GetMapping("/topic/sendMessage/{roomURL}/roomManagement")
	public String roomManagement(@PathVariable String roomURL, Authentication authentication, Model model, @ModelAttribute("sendRoomManagementForm") SendRoomManagementForm form) {
		
		
		Room room = callDiscussionService.getRoomByRoomURL(roomURL);
		if(room.getAdminUser().getMail().equals(authentication.getName()) == false){
			// 管理者じゃないのでアクセスできない旨メッセージ
			return "/topic/sendMessage/" + roomURL;
		}
		
		model.addAttribute("room", roomURL);
		model.addAttribute("password", room.getPassword());
		model.addAttribute("name", room.getRoomName());
		
		return "discussion/roomManagement";
	}
	
	@Transactional
	@PostMapping("/topic/sendMessage/{roomURL}/roomManagement")
	public String roomManagement_post(@PathVariable String roomURL, Authentication authentication, Model model, @Valid @ModelAttribute("sendRoomManagementForm") SendRoomManagementForm form
										, BindingResult result) {
		//管理者かどうか確認
		
		if(result.hasErrors()) {
			//エラー処理 return ...
		}
		
		if(form.isDeleteRoom()) {
			//削除がデキたときのメッセージ
			
			Room room = callDiscussionService.getRoomByRoomURL(roomURL);
			callDiscussionService.deleteRoom(room);
			
			return "redirect:/joinRoom";
		}else {
			
			return "regirect:/topic/sendMessage/" + roomURL + "/roomManagement";
		}
		
	}
	
	
	
	//送信不可時の例外処理
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
			
			//idではなく最終的には表示名にする
			
			ChatMessage log = reverseLog.get(i);
			ForSendLog sendLog = new ForSendLog();
			sendLog.setId(log.getUser_id());
			sendLog.setMessage(log.getMessage());
			
			//JSONに変換する
			String json = mapper.writeValueAsString(sendLog);
			if(i <= reverseLog.size()-2) {
				allLog += json + ",";
			}else {
				allLog += json;
			}
		}
		
		allLog = allLog.concat("]");
		
		System.out.println(allLog);
		callDiscussionService.registerReadTime(authentication, id);
		
		return allLog;
	}
	
	
	//ルームでログを取得した最終日時を記録 roomURLどう受け取る？JSONで
	@MessageMapping("/updateReadTime")
	public void updateReadTime(Authentication authentication, ForAlreadyReadTime forAlreadyReadTime) {
		System.out.println("呼び出されている");
		
		//呼び出せない beforeunloadは動作している。
		String roomId = HtmlUtils.htmlEscape(forAlreadyReadTime.getRoomURL());
		callDiscussionService.registerReadTime(authentication, roomId);
	}
	
	
	//ページのリロードだけで復帰できる。めんどくさくないレベルでセッションを設定すればよさそう。　error頁にハンドリングした方がよさそう。
	///topic/sendMessage/{id}にthrows LoseSessionException、errorパッケージに作成済
	@ExceptionHandler(LoseSessionException.class)
	public String regetSession(Model model) {
		model.addAttribute("error", "接続が切れました。");
		return "/error";
	}
	
	
	// ------non api ---------------
	private void getRoomList(Pageable pageable , HttpServletRequest request, Model model) {
		Page<Room> roomPage = callDiscussionService.getAllRoom(pageable);
		
		
		// 部屋は日付順で表示　検索機能も追加予定
		// nullの時どうするか
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
