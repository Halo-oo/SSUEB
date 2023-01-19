package com.ssafy.user.join;

import org.springframework.stereotype.Service;

import com.ssafy.user.join.request.ConsultantJoinRequest;
import com.ssafy.user.join.request.JoinRequest;

@Service
public interface UserJoinService {
	
	/**
	 * 회원정보를 Request DTO에서 User DTO로 옮기고 DB에 저장하기
	 * @param joinRequest 사용자 정보
	 * **/
	public boolean joinUser(JoinRequest joinRequest);
	
	/**
	 * 전문가 회원가입
	 * @param joinRequest 사용자 정보
	 * @param consultantJoinRequest 전문가 정보
	 * **/
	public boolean joinConsultant(String id, ConsultantJoinRequest consultantJoinRequest);
	
}
