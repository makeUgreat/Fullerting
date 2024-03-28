package com.ssafy.fullerting.alarm.service;

import com.ssafy.fullerting.alarm.model.EventAlarmType;
import com.ssafy.fullerting.alarm.model.entity.EventAlarm;
import com.ssafy.fullerting.alarm.repository.EventAlarmRepository;
import com.ssafy.fullerting.exArticle.model.entity.ExArticle;
import com.ssafy.fullerting.user.model.entity.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventAlarmService {
    private final EventAlarmRepository eventAlarmRepository;
    // 알림 트리거
    // 1. 내가 쓴 게시물에 댓글이 달렸을 때
    // 2. 채팅이 왔을 때 (채팅이 생성됐을 때)
    // 3. 가격제안 왔을 때
    // 4. 내가 입찰한 게시물의 거래가 종료되었을 때
    // 5. 뱃지 획득했을 때
    // 6. 등급 올랐을 때


    // 현재 사용자의 알림함에 저장하는 메서드
    // 실행조건 : 입찰자가 입찰하기를 눌렀을 때
    public EventAlarm notifyAuctionBidReceived(CustomUser bidUser, ExArticle exArticle, String redirectURL) {
        // 내가 가격제안 게시물을 올렸는데
        // 누군가가 입찰을 했을 때 알림

        EventAlarm alarm = EventAlarm.builder()
                .receiveUser(exArticle.getUser())
                .sendUser(bidUser)
                .type(EventAlarmType.작물거래)
                .content(bidUser + "님이 가격을 제안하셨어요.")
                .redirect(redirectURL)
                .build();

        return eventAlarmRepository.save(alarm);
    }
}
