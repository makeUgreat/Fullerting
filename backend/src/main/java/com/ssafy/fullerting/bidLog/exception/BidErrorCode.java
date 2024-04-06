package com.ssafy.fullerting.bidLog.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BidErrorCode {

    NOT_EXISTS("존재하지 않는 입찰 입니다.",HttpStatus.BAD_REQUEST),
    NOT_DEAL("제안게시물이 아닙니다. 게시물 타입을 확인하세요 ",HttpStatus.BAD_REQUEST),
    ALREADY_IN("이미 등록된 입찰 입니다.", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus httpStatus;
}
