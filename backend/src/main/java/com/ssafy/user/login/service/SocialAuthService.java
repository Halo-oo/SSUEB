package com.ssafy.user.login.service;

import com.ssafy.user.login.model.GoogleUserInfo;
import com.ssafy.user.login.model.KakaoAccessToken;
import com.ssafy.user.login.model.KakaoUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class SocialAuthService {
    private static final Logger logger = LoggerFactory.getLogger(SocialAuthService.class);

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String KAKAO_CLIENT_ID;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String REDIRECT_URL;

    private String KAKAO_REQ_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private String KAKAO_REQ_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private String GOOGLE_REQ_USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    /**
     * Kakao 인가 코드를 통해 인증 토큰 발급받기
     * @param code Kakao Auth Code
     * **/
    public String getKakaoAccessToken(String code) {

        RestTemplate restTemplate = new RestTemplate();     // RestTemplate 사용

        // HttpHeader
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HttpBody
        // 인증토큰 요청에 필요한 파라미터 넣기
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", KAKAO_CLIENT_ID);
        params.add("redirect_uri", REDIRECT_URL);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenReq = new HttpEntity<>(params, headers);
//        ResponseEntity<JSONObject> response = restTemplate.exchange(KAKAO_REQ_TOKEN_URL, HttpMethod.POST, kakaoTokenReq, JSONObject.class);
//        JSONObject responseBody = response.getBody();

        // 인증 토큰 요청 및 응답 처리
        ResponseEntity<KakaoAccessToken> response = restTemplate.exchange(KAKAO_REQ_TOKEN_URL, HttpMethod.POST, kakaoTokenReq, KakaoAccessToken.class);
        logger.info("#OAuth# Kakao 인증토큰 확인: {}", response.getBody().getAccess_token());

        return response.getBody().getAccess_token();
    }

    /**
     * Kakao 인증토큰를 통해 사용자 정보 조회
     * @param
     * **/
    public KakaoUserInfo getKakaoUserInfo(String accessToken) {
        // User 정보 요청
        RestTemplate restTemplate = new RestTemplate();

        // HttpHeader
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HttpHeader와 HttpBody 담기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoReq = new HttpEntity<>(headers);
//        kakaoUserInfo = restTemplate.exchange(KAKAO_REQ_USER_INFO_URL, HttpMethod.GET, kakaoUserInfoReq, KakaoUserInfo.class).getBody();
//        logger.info("#OAuth# _카카오 유저정보 조회: {}", kakaoUserInfo);

        return restTemplate.exchange(KAKAO_REQ_USER_INFO_URL, HttpMethod.GET, kakaoUserInfoReq, KakaoUserInfo.class).getBody();
    }

    /**
     * Google 인증토큰를 통해 사용자 정보 조회
     * @param
     * **/
    public GoogleUserInfo getGoogleUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(GOOGLE_REQ_USER_INFO_URL).queryParam("access_token", accessToken);
        URI uri = builder.build().toUri();

        return restTemplate.exchange(uri, HttpMethod.GET, null, GoogleUserInfo.class).getBody();
    }
}
