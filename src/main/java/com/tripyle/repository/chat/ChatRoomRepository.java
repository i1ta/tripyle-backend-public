package com.tripyle.repository.chat;

import com.tripyle.model.entity.chat.ChatRoom;
import com.tripyle.model.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByUserZero(User user);
    List<ChatRoom> findByUserOne(User user);
    ChatRoom findByUserZeroAndUserOne(User userZero, User userOne);
}
