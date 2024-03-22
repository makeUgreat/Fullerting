package com.ssafy.fullerting.farm.controller;

import com.ssafy.fullerting.farm.service.FarmService;
import com.ssafy.fullerting.global.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/farms")
@RequiredArgsConstructor //생성자 주입
@Slf4j
public class FarmController {
    private final FarmService farmService;

    /**
     * 텃밭정보 전체조회
     * @return
     */
    @GetMapping
    public ResponseEntity<MessageUtils> getAllFarm(){
        return ResponseEntity.ok().body(MessageUtils.success(farmService.getAllFarm()));
    }

}