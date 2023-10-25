package com.tripyle.controller.message;

import com.tripyle.common.exception.BadRequestException;
import com.tripyle.common.model.dto.HttpRes;
import com.tripyle.model.dto.user.UserRes;
import com.tripyle.model.entity.user.User;
import com.tripyle.repository.user.UserRepository;
import com.tripyle.service.message.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Api(tags={"04.Message"})
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/message", produces = MediaType.APPLICATION_JSON_VALUE)
public class MessageController {
    private final MessageService messageService;
    private final UserRepository userRepository;

    @ApiOperation(value = "쪽지 보내기", notes = "쪽지를 보낼 수 있습니다. ")
    @PostMapping("")
    public HttpRes<UserRes.MessageDto> sendMessage(@RequestBody UserRes.MessageDto messageDto){
        // 임의로 유저 정보를 넣었지만, JWT 도입하고 현재 로그인 된 유저의 정보를 넘겨줘야함
        User user = userRepository.findById(1L).orElseThrow(() -> {
            throw new BadRequestException("유저를 찾을 수 없습니다."); // BadRequest로 400 에러 반환
        });
        messageDto.setSenderName(user.getUsername());

        return new HttpRes<>(messageService.writeMessage(messageDto));
    }

    @ApiOperation(value = "받은 편지함 읽기", notes = "받은 편지함 확인")
    @GetMapping("/received")
    public HttpRes<UserRes.MessageDto> getReceivedMessage() {
        // 임의로 유저 정보를 넣었지만, JWT 도입하고 현재 로그인 된 유저의 정보를 넘겨줘야함
        User user = userRepository.findById(1L).orElseThrow(() -> {
            throw new BadRequestException("유저를 찾을 수 없습니다.");
        });

        return new HttpRes(messageService.receivedMessage(user));
    }

    @ApiOperation(value = "보낸 편지함 읽기", notes = "보낸 편지함 확인")
    @GetMapping("/sent")
    public HttpRes<UserRes.MessageDto> getSentMessage() {
        // 임의로 유저 정보를 넣었지만, JWT 도입하고 현재 로그인 된 유저의 정보를 넘겨줘야함
        User user = userRepository.findById(1L).orElseThrow(() -> {
            throw new BadRequestException("유저를 찾을 수 없습니다.");
        });

        return new HttpRes(messageService.sentMessage(user));
    }

    @ApiOperation(value = "쪽지 삭제하기", notes = "쪽지를 삭제합니다.")
    @DeleteMapping("/delete/{id}")
    public HttpRes<String> deleteMessage(@PathVariable("id") Long id) {
        // 임의로 유저 정보를 넣었지만, JWT 도입하고 현재 로그인 된 유저의 정보를 넘겨줘야함
        User user = userRepository.findById(1L).orElseThrow(() -> {
            throw new BadRequestException("유저를 찾을 수 없습니다.");
        });

        return new HttpRes(messageService.deleteMessage(id, user));
    }
}
