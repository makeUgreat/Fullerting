package com.ssafy.fullerting.security.model.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IssuedToken {
    private String accessToken;
    private String refreshToken;
}
