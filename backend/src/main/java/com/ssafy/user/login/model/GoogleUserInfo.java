package com.ssafy.user.login.model;

import lombok.Data;

/**
 * Google 소셜 로그인 시 로그인 한 유저의 정보 DTO
 */
@Data
public class GoogleUserInfo {
    private String id;
    private String email;
    private Boolean verifiedEmail;
    private String name;
    private String givenName;
    private String familyName;
    private String picture;
    private String locale;
}
