package com.tripyle.repository.chat;

import com.tripyle.model.entity.chat.Chat;
import com.tripyle.model.entity.chat.ChatRoom;
import com.tripyle.model.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByChatRoom(ChatRoom chatRoom);
}
