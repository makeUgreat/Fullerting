package com.ssafy.fullerting.chat.controller;

import com.ssafy.fullerting.chat.model.dto.request.CreateChatRoomRequest;
import com.ssafy.fullerting.chat.service.ChatRoomService;
import com.ssafy.fullerting.global.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/chat-room")
@RequiredArgsConstructor //생성자 주입
@Slf4j
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    /**
     * 채팅방 생성
     * @param createChatRoomRequest
     * @return
     */
    @PostMapping
    public ResponseEntity<MessageUtils> createChatRoom(@RequestBody CreateChatRoomRequest createChatRoomRequest) {
        //채팅방 ID 반환
        return ResponseEntity.ok().body(MessageUtils.success(chatRoomService.createChatRoom(createChatRoomRequest)));
    }

    /**
     * 채팅방 전체조회
     * @return
     */
    @GetMapping
    public ResponseEntity<MessageUtils> getAllChatRoom() {
        return ResponseEntity.ok().body(MessageUtils.success(chatRoomService.getAllChatRoom()));
    }

    /**
     * 채팅방 상세조회
     * @param chatRoomId
     * @return
     */
    @GetMapping("/{chat_room_id}")
    public ResponseEntity<MessageUtils> getDetailChatRoom(@PathVariable("chat_room_id") Long chatRoomId) {
        return ResponseEntity.ok().body(MessageUtils.success(chatRoomService.getDetailChatRoom(chatRoomId)));
    }
}
