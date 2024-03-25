package com.ssafy.fullerting.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.fullerting.global.utils.MessageUtils;
import com.ssafy.fullerting.security.model.dto.response.IssuedToken;
import com.ssafy.fullerting.security.service.TokenService;
import com.ssafy.fullerting.user.model.entity.CustomUser;
import com.ssafy.fullerting.user.repository.UserRepository;
import com.ssafy.fullerting.user.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final UserService userService;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //http://localhost:8080/login 에서 확인
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        CustomUser customUser = CustomUser.of(oAuth2User);
        log.info("Oauth 사용자 정보 확인 : {}", oAuth2User.toString());

        // DB에 유저 있는지 조회
        userRepository.findByEmail(oAuth2User.getName()).ifPresentOrElse(user -> {
            writeResponse(response, MessageUtils.success(tokenService.issueToken(authentication)));
            try {
                response.sendRedirect("https://j10c102.p.ssafy.io/auth/callback");
            } catch (IOException e) {
                log.error("리디렉션 중 오류 발생", e);
                // 에러 처리 로직
            }
        }, () -> {
            try {
                // OAuth2 인증으로 얻은 사용자 정보를 바탕으로 CustomUser 객체 생성
                customUser.setPassword("DummyPasswordeoar!@3");
                userService.registOauthUser(customUser);

                IssuedToken issuedToken = tokenService.issueToken(authentication);
                writeResponse(response, MessageUtils.success(tokenService.issueToken(authentication)));
                try {
                    response.sendRedirect("https://j10c102.p.ssafy.io/auth/callback");
                } catch (IOException e) {
                    log.error("리디렉션 중 오류 발생", e);
                    // 에러 처리 로직
                }
            } catch (Exception e) {
                log.error("OAuth 유저 등록 오류", e);
            }
        });
    }

    // 쿠키로 만들 때 사용
    private void setTokenCookies(HttpServletResponse response, IssuedToken issuedToken) {
        // 액세스 토큰 쿠키
        Cookie accessTokenCookie = new Cookie("accessToken", issuedToken.getAccessToken());
        accessTokenCookie.setPath("/");
        accessTokenCookie.setSecure(true); // HTTPS를 사용하는 경우에만 쿠키를 전송
        accessTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 쿠키 유효 기간 설정 (예: 7일)

        // 리프레시 토큰 쿠키
        Cookie refreshTokenCookie = new Cookie("refreshToken", issuedToken.getRefreshToken());
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setSecure(true); // HTTPS를 사용하는 경우에만 쿠키를 전송
        refreshTokenCookie.setMaxAge(14 * 24 * 60 * 60); // 쿠키 유효 기간 설정 (예: 14일)

        // HttpOnly 플래그 제거하여 쿠키 추가
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }


    // JSON 생성
    private void writeResponse(HttpServletResponse response, Object tokenResponse) {
        try {
            String json = objectMapper.writeValueAsString(tokenResponse);
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(json);
        } catch (IOException e) {
            log.error("Response write error", e);
        }
    }
}