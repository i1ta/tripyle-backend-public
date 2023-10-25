package com.tripyle.repository.message;

import com.tripyle.model.entity.message.Message;
import com.tripyle.model.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByRecipient(User user);
    List<Message> findAllBySender(User user);
}
