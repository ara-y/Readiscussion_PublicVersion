package com.example.demo.repository.discussion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.discussion.Room;

public interface RoomRepository extends JpaRepository<Room, Long>{

	public Page<Room> findAll(Pageable pageable);
	
	public Room findById(int id);
	
	public Room findByRoomURL(String roomURL);
}
