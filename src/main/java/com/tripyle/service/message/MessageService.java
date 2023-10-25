package com.tripyle.service.message;

import com.tripyle.common.exception.NotFoundException;
import com.tripyle.model.dto.user.UserRes;
import com.tripyle.model.entity.message.Message;
import com.tripyle.model.entity.user.User;
import com.tripyle.repository.message.MessageRepository;
import com.tripyle.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserRes.MessageDto writeMessage(UserRes.MessageDto messageDto){
        User recipient = userRepository.findByUsername(messageDto.getRecipientName());
        User sender = userRepository.findByUsername(messageDto.getSenderName());

        // message 객체 생성 후, 데이터베이스에 저장
        Message message = Message.builder()
                        .recipient(recipient)
                        .sender(sender)
                        .content(messageDto.getContent())
                        .build();

        messageRepository.save(message);

        return UserRes.MessageDto.builder()
                .content(message.getContent())
                .senderName(message.getSender().getName())
                .recipientName(message.getRecipient().getName())
                .sendTime(message.getRegDateTime())
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserRes.MessageDto> receivedMessage(User user){
        // 받은 편지함 불러오기
        // 한 명의 유저가 받은 모든 메시지 -> 유저 별로 채팅창 구분
        // 추후 JWT를 이용해서 재구현 예정
        List<Message> messages = messageRepository.findAllByRecipient(user);
        List<UserRes.MessageDto> messageDtos = new ArrayList<>();

        for(Message message : messages){
            // message 에서 받은 편지함에서 삭제하지 않았으면 보낼 때 추가해서 보내줌
            messageDtos.add(UserRes.MessageDto.builder()
                    .content(message.getContent())
                    .senderName(message.getSender().getName())
                    .recipientName(message.getRecipient().getName())
                    .sendTime(message.getRegDateTime())
                    .build());
        }
        return messageDtos;
    }

    @Transactional(readOnly = true)
    public List<UserRes.MessageDto> sentMessage(User user){
        // 보낸 편지함 불러오기
        // 한 명의 유저가 받은 모든 메시지 -> 유저 별로 채팅창 구분
        // 추후 JWT를 이용해서 재구현 예정
        List<Message> messages = messageRepository.findAllBySender(user);
        List<UserRes.MessageDto> messageDtos = new ArrayList<>();

        for(Message message : messages) {
            // message 에서 받은 편지함에서 삭제하지 않았으면 보낼 때 추가해서 보내줌
            messageDtos.add(UserRes.MessageDto.builder()
                    .content(message.getContent())
                    .senderName(message.getSender().getName())
                    .recipientName(message.getRecipient().getName())
                    .sendTime(message.getRegDateTime())
                    .build());
        }
        return messageDtos;
    }

    @Transactional
    public String deleteMessage(long id, User user) {
        Message message = messageRepository.findById(id).orElseThrow(() -> {
            throw new NotFoundException("메시지를 찾을 수 없습니다."); //NotFound 에러로 404 반환
        });

        if (user == message.getRecipient() || user == message.getSender()) {
            message.setDeleteYn(true); // DeleteYn = 1
            return "데이터베이스에서 메세지 삭제";

        }
        else {
            return ("유저 정보가 일치하지 않습니다.");
        }
    }

}
