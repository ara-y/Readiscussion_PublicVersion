package com.example.demo.repository.discussion;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.SiteUser;
import com.example.demo.model.discussion.AlreadyReadTime;
import com.example.demo.model.discussion.Room;

public interface AlreadyReadTimeRepository extends JpaRepository<AlreadyReadTime, Long>{
	
	void deleteByAlreadyReadUser(SiteUser user);
	
	AlreadyReadTime findByAlreadyReadUserAndTargetRoom(SiteUser user, Room room);

}
