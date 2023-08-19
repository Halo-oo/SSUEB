package com.ssafy.user.login.response;

import com.ssafy.common.model.response.BaseResponseBody;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * user 로그인 API ([POST] /api/user/auth) 요청에 대한 응답값 정의.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@ApiModel("UserLoginPostResponse")
public class UserLoginPostResponse extends BaseResponseBody{
	
	@ApiModelProperty(name="JWT 인증 토큰", example="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN...")
	String token;
	
	@ApiModelProperty(name = "성공/실패 여부", example = "success / failure")
	String response;

	@ApiModelProperty(name = "소셜 로그인 유저 email", example = "ssafy@naver.com")
	String socialUserEmail = null;

	@ApiModelProperty(name = "소셜 로그인 유저 nickname", example = "싸피")
	String socialUserNickname = null;

	
	public static UserLoginPostResponse of(Integer statusCode, String response, String message, String token) {
		UserLoginPostResponse res = new UserLoginPostResponse();
		
		res.setStatusCode(statusCode);
		res.setResponse(response);
		res.setMessage(message);
		res.setToken(token);
		
		return res;
	}

	public static UserLoginPostResponse of(Integer statusCode, String response, String message, String token, String socialUserEmail, String socialUserNickname) {
		UserLoginPostResponse res = new UserLoginPostResponse();

		res.setStatusCode(statusCode);
		res.setResponse(response);
		res.setMessage(message);
		res.setToken(token);
		res.setSocialUserEmail(socialUserEmail);
		res.setSocialUserNickname(socialUserNickname);

		return res;
	}
}
