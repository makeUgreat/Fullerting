package com.ssafy.fullerting.chat.service;

import com.ssafy.fullerting.alarm.service.EventAlarmService;
import com.ssafy.fullerting.chat.exception.ChatErrorCode;
import com.ssafy.fullerting.chat.exception.ChatException;
import com.ssafy.fullerting.chat.model.dto.request.CreateChatRoomRequest;
import com.ssafy.fullerting.chat.model.dto.response.CreateChatRoomResponse;
import com.ssafy.fullerting.chat.model.dto.response.GetAllChatRoomResponse;
import com.ssafy.fullerting.chat.model.dto.response.GetDetailChatRoomResponse;
import com.ssafy.fullerting.chat.model.entity.Chat;
import com.ssafy.fullerting.chat.model.entity.ChatRoom;
import com.ssafy.fullerting.chat.repository.ChatRepository;
import com.ssafy.fullerting.chat.repository.ChatRoomRepository;
import com.ssafy.fullerting.exArticle.exception.ExArticleErrorCode;
import com.ssafy.fullerting.exArticle.exception.ExArticleException;
import com.ssafy.fullerting.exArticle.model.entity.ExArticle;
import com.ssafy.fullerting.exArticle.repository.ExArticleRepository;
import com.ssafy.fullerting.user.exception.UserErrorCode;
import com.ssafy.fullerting.user.exception.UserException;
import com.ssafy.fullerting.user.model.dto.response.UserResponse;
import com.ssafy.fullerting.user.model.entity.CustomUser;
import com.ssafy.fullerting.user.repository.UserRepository;
import com.ssafy.fullerting.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ssafy.fullerting.chat.exception.ChatErrorCode.NOT_EXISTS_CHAT_ROOM;
import static com.ssafy.fullerting.exArticle.exception.ExArticleErrorCode.NOT_EXISTS;
import static com.ssafy.fullerting.user.exception.UserErrorCode.NOT_EXISTS_USER;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatRoomServiceImpl implements ChatRoomService{
    private final UserService userService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRepository chatRepository;
    private final ExArticleRepository exArticleRepository;
    private final UserRepository userRepository;
    private final EventAlarmService eventAlarmService;

    @Override
    public CreateChatRoomResponse createChatRoom(CreateChatRoomRequest createChatRoomRequest) {
        //게시글 존재하는지 확인
        ExArticle exArticle = exArticleRepository.findById(createChatRoomRequest.getExArticleId()).orElseThrow(() -> new ExArticleException(NOT_EXISTS));

        Optional<ChatRoom> chatRoomOptional; //채팅방 조회
        UserResponse userResponse = null; //일반거래, 나눔 유저 정보
        CustomUser customUser = null; //제안 유저 정보
        
        //일반거래, 나눔일 경우
        if(createChatRoomRequest.getBuyerId() == null){
            userResponse = userService.getUserInfo();
            //이미 존재하는 채팅방인지 조회
            chatRoomOptional = chatRoomRepository.findByExArticleAndBuyer(exArticle, UserResponse.toEntity(userResponse));
        }
        //제안일 경우
        else {
            customUser = userRepository.findById(createChatRoomRequest.getBuyerId()).orElseThrow(()->new UserException(NOT_EXISTS_USER));
            chatRoomOptional = chatRoomRepository.findByExArticleAndBuyer(exArticle, customUser);
        }

        //존재하지 않을 경우 채팅방 생성
        if(chatRoomOptional.isEmpty()){
            ChatRoom chatRoom;
            //일반거래, 나눔일 경우 -> 구매자가 채팅방을 생성
            if(createChatRoomRequest.getBuyerId() == null){
                chatRoom = chatRoomRepository.save(ChatRoom.builder()
                        .exArticle(exArticle)
                        .buyer(UserResponse.toEntity(userResponse))
                        .build());


                // 채팅방 생성 알림 전송 -> 판매자에게 전송
                String redirectURL = "/trade/" + chatRoom.getId() + "/chat";
                eventAlarmService.notifyCreateChatRoomAuthor(userService.getUserEntityById(userResponse.getId()), exArticle, redirectURL);
            }
            //제안일 경우 -> 판매자가 제안 중에 하나를 눌러서 구매자에게 대화 요청을 거는 상황
            else {
                chatRoom = chatRoomRepository.save(ChatRoom.builder()
                        .exArticle(exArticle)
                        .buyer(customUser)
                        .build());
                
                // 채팅방 생성 알림 전송 -> 구매자에게 알림 전송
                String redirectURL = "/trade/" + chatRoom.getId() + "/chat";
                eventAlarmService.notifyCreateChatRoomBidder(customUser, exArticle, redirectURL);
            }
            
            return CreateChatRoomResponse.toResponse(chatRoom);
        }
        //존재하는 경우 해당 채팅방 응답
        else{
            return CreateChatRoomResponse.toResponse(chatRoomOptional.get());
        }

    }

    @Override
    public List<GetAllChatRoomResponse> getAllChatRoom() {
        //현재 유저 정보 받아오기
        UserResponse userResponse = userService.getUserInfo();

        //현재 유저가 구매자이거나 판매자인 경우 모든 채팅방 조회
        List<ChatRoom> chatRoomList = chatRoomRepository.findByBuyerOrExArticleUser(UserResponse.toEntity(userResponse));

        return chatRoomList.stream()
                // 채팅방 리스트를 최신 채팅 메시지가 먼저 오도록 정렬
                .sorted((chatRoom1, chatRoom2) -> {
                    Chat lastChat1 = chatRepository.findLatestChatByChatRoomId(chatRoom1.getId());
                    Chat lastChat2 = chatRepository.findLatestChatByChatRoomId(chatRoom2.getId());

                    // 최신 채팅 메시지가 없는 경우에는 정렬하지 않음
                    if (lastChat1 == null && lastChat2 == null) {
                        return 0;
                    } else if (lastChat1 == null) {
                        return 1; // lastChat2가 존재하면 lastChat1 보다 나중으로 간주
                    } else if (lastChat2 == null) {
                        return -1; // lastChat1이 존재하면 lastChat2 보다 나중으로 간주
                    } else {
                        return lastChat2.getSendAt().compareTo(lastChat1.getSendAt());
                    }
                })
                .map(chatRoom -> {
                    //상대방 ID 조회
                    ExArticle exArticle = exArticleRepository.findById(chatRoom.getExArticle().getId()).orElseThrow(() -> new ExArticleException(NOT_EXISTS));
                    CustomUser otherUser = null;
                    //현재 유저가 판매자와 동일할 경우
                    if (exArticle.getUser().getId().equals(userResponse.getId())) {
                        //상대방 정보에 구매자 저장
                        otherUser = userRepository.findById(chatRoom.getBuyer().getId()).orElseThrow(() -> new UserException(NOT_EXISTS_USER));
                    }
                    //현재 유저가 구매자와 동일할 경우
                    else {
                        //상대방 정보에 판매자 저장
                        otherUser = exArticle.getUser();
                    }

                    //채팅방 최근 메시지 조회
                    Chat lastChat = chatRepository.findLatestChatByChatRoomId(chatRoom.getId());

                    return GetAllChatRoomResponse.builder()
                            .chatRoomId(chatRoom.getId())
                            .chatRoomOtherThumb(otherUser.getThumbnail())
                            .chatOtherNick(otherUser.getNickname())
                            .chatRoomLastMessage(lastChat != null ? lastChat.getMessage() : null)
                            .chatRoomLastMessageSendAt(lastChat != null ? lastChat.getSendAt() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public GetDetailChatRoomResponse getDetailChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(()->new ChatException(NOT_EXISTS_CHAT_ROOM));
        GetDetailChatRoomResponse getDetailChatRoomResponse = GetDetailChatRoomResponse.toResponse(chatRoom.getExArticle());
        getDetailChatRoomResponse = getDetailChatRoomResponse.toBuilder()
                .chatRoomBuyerId(chatRoom.getBuyer().getId()).build();

        return getDetailChatRoomResponse;
    }
}
