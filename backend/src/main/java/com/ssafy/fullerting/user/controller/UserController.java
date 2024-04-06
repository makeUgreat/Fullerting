package com.ssafy.fullerting.user.controller;

import com.ssafy.fullerting.global.utils.MessageUtils;
import com.ssafy.fullerting.user.model.dto.request.UserRegisterRequest;
import com.ssafy.fullerting.user.model.dto.request.UserTownRequest;
import com.ssafy.fullerting.user.model.dto.request.UserUpdateRequest;
import com.ssafy.fullerting.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
@Tag(name = "유저 기능 API", description = "유저와 관련된 기능 제공")
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "유저 회원가입", description = "이메일, 비밀번호, 닉네임을 입력받아 회원가입을 진행한다")
    public ResponseEntity<MessageUtils> register(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        userService.registUser(userRegisterRequest);
        log.info("[New User]: {}", userRegisterRequest.toString());
        return ResponseEntity.ok().body(MessageUtils.success());
    }

    @GetMapping("/info")
    @Operation(summary = "유저정보조회", description = "현재 로그인 중인 유저의 상세 정보를 조회한다 <br> [헤더 Bearer: Access토큰 필요] <br> 토큰을 통해 유저정보를 조회한다")
    public ResponseEntity<MessageUtils> getUserInfo() {
        return ResponseEntity.ok().body(MessageUtils.success(userService.getUserInfo()));
    }


    @GetMapping("/info/{userid}")
    @Operation(summary = "유저정보조회", description = "user id로   유저의 상세 정보를 조회한다 <br> [헤더 Bearer: Access토큰 필요] <br> 토큰을 통해 유저정보를 조회한다")
    public ResponseEntity<MessageUtils> getUserInfobyid(@PathVariable Long userid) {
        return ResponseEntity.ok().body(MessageUtils.success(userService.getUserInfobyid(userid)));

    }
    @PatchMapping("")
    @Operation(summary = "유저 정보 수정", description = "유저의 정보를 수정한다 <br> 현재는 닉네임만 ")
    public ResponseEntity<MessageUtils> updateUserInfo(@RequestBody @Valid UserUpdateRequest userUpdateRequest) {
        userService.updateCurrentUserInfo(userUpdateRequest);
        return ResponseEntity.ok().body(MessageUtils.success());
    }


    @PostMapping("/profile")
    @Operation(summary = "유저 프로필 사진 업로드", description = "유저의 프로필 사진을 업로드한다")
    public ResponseEntity<MessageUtils> uploadProfileImg(MultipartFile multipartFile) {
        return ResponseEntity.ok().body(MessageUtils.success(userService.uploadThumbAndSaveDB(multipartFile)));
    }


    @PatchMapping("/town")
    @Operation(summary = "유저 동네정보 저장", description = "유저의 동네정보를 저장한다.")
    public ResponseEntity<MessageUtils> updatetown(@RequestBody @Valid UserTownRequest userTownRequest) {
        userService.updatetown(userTownRequest);
        return ResponseEntity.ok().body(MessageUtils.success());
    }
}
