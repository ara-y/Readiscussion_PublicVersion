package com.example.demo.repository.discussion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.discussion.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>{
	Page<ChatMessage> findByRoomId(String id, Pageable pageable);
}
