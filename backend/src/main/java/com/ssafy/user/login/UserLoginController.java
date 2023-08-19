package com.ssafy.user.login;

import java.util.Optional;

import com.ssafy.common.util.BasicResponse;
import com.ssafy.user.login.model.KakaoUserInfo;
import com.ssafy.user.login.service.SocialAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.common.jwt.JwtAuthenticationFilter;
import com.ssafy.common.jwt.JwtTokenProvider;
import com.ssafy.common.util.CommonVariable;
import com.ssafy.common.util.ParameterCheck;
import com.ssafy.db.entity.Consultant;
import com.ssafy.db.entity.User;
import com.ssafy.user.join.UserJoinController;
import com.ssafy.user.join.repository.JoinUserRepository;
import com.ssafy.user.login.request.UserLoginPostRequest;
import com.ssafy.user.login.response.UserLoginPostResponse;
import com.ssafy.user.login.service.UserLoginService;
import com.ssafy.user.withdrawal.UserWithdrawalController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = {"User/Login"}, description = "로그인 및 Token 발급  API")
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/user/login")
public class UserLoginController {
	
	private static final Logger logger = LoggerFactory.getLogger(UserLoginController.class);
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	// for. 입력값 검증 
	ParameterCheck parameterCheck = new ParameterCheck(); 
	
	// for. JWT
	@Autowired
	JwtTokenProvider jwtTokenProvider; 
	@Autowired
	AuthenticationManagerBuilder authenticationManagerBuilder;
	
	@Autowired
	UserJoinController userJoinController;
	@Autowired
	JoinUserRepository joinUserRepository;
	
	@Autowired
	UserLoginService userLoginService;
	
	@Autowired
	CommonVariable commonVariable;
	
	@Autowired
	UserWithdrawalController userWithdrawalController;

	@Autowired
	SocialAuthService socialAuthService;
	
	/** 
	 * id와 pw를 통해 로그인 실행, 성공 시 JWT token 반환
	 * @param loginInfo
	 * @return UserToken
	 */
	@PostMapping("/")
	@ApiOperation(value = "로그인 - JWT 토큰 발급")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = UserLoginPostResponse.class, message = "로그인에 성공했습니다."),
			@ApiResponse(code = 401, response = UserLoginPostResponse.class, message = "id 또는 password를 다시 입력해 주세요.")
	})
	public ResponseEntity<UserLoginPostResponse> authorize(@RequestBody @ApiParam(value = "로그인 시 필요한 회원정보(email(ID), PW)", required = true) UserLoginPostRequest loginInfo) {
		
		try {
			logger.info("## [Controller]: authorize - 로그인 실행 {}", loginInfo);
			logger.info("#21# 암호화 비밀번호: {}", passwordEncoder.encode(loginInfo.getPassword()));
			
			// # 탙퇴 회원 판별 [true = 탈퇴]
//			if (userWithdrawalController.checkWithdrawalUser(loginInfo.getId())) {
//				return ResponseEntity.ok(UserLoginPostResponse.of(401, "failure", "탈퇴회원 입니다.", null));
//			}
			
			// # 소셜 로그인(Kakao, Google)인 경우 비밀번호 생성
//			if (loginInfo.getPassword().equals("socialKakao")) {
//				loginInfo.setPassword(createSocialPassword(loginInfo.getId(), "KAKAO"));
//			}
//			else if (loginInfo.getPassword().equals("socialGoogle")) {
//				loginInfo.setPassword(createSocialPassword(loginInfo.getId(), "GOOGLE"));
//			}
			
//			// # 입력값 검증
//			// i) id - 비어 있지 않은지 && ID 규칙에 맞는지
//			if(parameterCheck.isEmpty(loginInfo.getId()) || !parameterCheck.isValidId(loginInfo.getId())) {
//				return ResponseEntity.ok(UserLoginPostResponse.of(401, "failure", "id 또는 password를 다시 입력해 주세요.", null));
//			}
//			// ii) pw - 비어 있지 않은지 && PW 규칙에 맞는지
//			if (parameterCheck.isEmpty(loginInfo.getPassword()) || !parameterCheck.isValidPassword(loginInfo.getPassword())) {
//				return ResponseEntity.ok(UserLoginPostResponse.of(401, "failure", "id 또는 password를 다시 입력해 주세요.", null));
//			}
//
//			// # 소셜 로그인 ID 여부 검증
//			Optional<User> user = joinUserRepository.findById(loginInfo.getId());
//			if (user.get().getUserIsSocialId() == 1 && loginInfo.getSocialButton()==0) {
//				return ResponseEntity.ok(UserLoginPostResponse.of(401, "failure", "소셜 로그인을 이용해 주세요.", null));
//			}
//			logger.info("## [Controller]: authorize - 검증 성공");
			
			// # 로그인
			// i) 입력받은 loginInfo(id, pw)를 사용하여 Authentication(인증) 토큰 생성
			UsernamePasswordAuthenticationToken authenticationToken = 
					new UsernamePasswordAuthenticationToken(loginInfo.getId(), loginInfo.getPassword());
			logger.info("#21# i) 토큰생성: {}", authenticationToken); 

			
			// ii) i에서 만든 authenticationToken을 사용하여 Authentication 객체를 생성하기 위하여 authenticate 메소드가 실행될 때
			//     CustomUserDetailsService 에 loadUserByUsername 메소드가 실행됨
			Authentication authentication = 
					authenticationManagerBuilder.getObject().authenticate(authenticationToken);
			logger.info("#21# ii) authentication 객체 생성: {}", authentication);
			
			// ii-1) 전문가 권한인 경우 자격증 검증 값 확인
			Optional<? extends GrantedAuthority> authority =  authentication.getAuthorities().stream().findFirst();
			String authorityStr = authority.get().getAuthority();
//			logger.info("#21# ii-1) 전문가 자격증 검증: {}", authorityStr);
			if (authorityStr.equals("ROLE_CONSULTANT")) {
				// 전문가 자격증 검증 컬럼 확인
				logger.info("#21# id에 해당되는 전문가 정보: {}", userLoginService.getConsultantById(loginInfo.getId()));
				Consultant consultant = userLoginService.getConsultantById(loginInfo.getId());
				if (consultant.getConsultantCertified() == 0) {
					return ResponseEntity.ok(UserLoginPostResponse.of(401, "unverified", "자격증 미검증 상태입니다.", null));
				}
			}
			
			// iii) ii에 생성한 Authentication 객체를 사용하여 
			//      - SecurityContext에 저장
			SecurityContextHolder.getContext().setAuthentication(authentication);
			//      - token 생성
			String token = jwtTokenProvider.createToken(authentication);
//			logger.info("#21# iii) token 생성: {}", token);
			
			// iv) token을 Response Header에 넣고
			HttpHeaders httpHeaders = new HttpHeaders(); 
			httpHeaders.add(JwtAuthenticationFilter.AUTHORIZATION_HEADER, "Bearer " + token);
			
			// v) Response Body에도 넣어서 return 한다.
//			return new ResponseEntity<>(new UserToken(token), httpHeaders, HttpStatus.OK);
			return ResponseEntity.ok(UserLoginPostResponse.of(200, "success", "로그인에 성공했습니다.", token));
		
		}catch(Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok(UserLoginPostResponse.of(401, "failure", "id 또는 password를 다시 입력해 주세요.", null));
		}
	}

	/**
	 * [OAuth] Kakao 소셜 로그인
	 * @param authCode
	 * @return
	 */
	@PostMapping("/get-kakao-auth-code")
	public ResponseEntity<?> kakaoLogin(@RequestBody String authCode) {
//		logger.info("#OAuth# Kakao 인가 코드 확인: {}", authCode);

		// 1) Kakao 인가 코드를 통해 인증토큰 발급 (getKakaoAccessToken)
		// 2) 인증토큰을 통해 사용자의 정보 가져오기
		KakaoUserInfo kakaoUserInfo = socialAuthService.getKakaoUserInfo(socialAuthService.getKakaoAccessToken(authCode));
		logger.info("#OAuth# Kakao 사용자 정보 조회: {}", kakaoUserInfo);

		// [검증] 회원 존재여부 확인
//		logger.info("#OAuth# 회원 존재여부: {}", parameterCheck.isValidId(kakaoUserInfo.getKakao_account().getEmail()));
		if (!parameterCheck.isValidId(kakaoUserInfo.getKakao_account().getEmail())) {
//			logger.info("#OAuth# 해당 Kakao email에 해당하는 회원 없음 _회원가입 필요");
			return ResponseEntity.ok(UserLoginPostResponse.of(401, "need_register", "회원가입이 필요합니다.", null, kakaoUserInfo.getKakao_account().getEmail(), kakaoUserInfo.getProperties().getNickname()));
		}

		// 3) 로그인 진행
		UserLoginPostRequest loginInfo = new UserLoginPostRequest();
		loginInfo.setId(kakaoUserInfo.getKakao_account().getEmail());
		loginInfo.setPassword(createSocialPassword(loginInfo.getId(), "KAKAO"));
		loginInfo.setSocialButton(1);
//		logger.info("#OAuth# loginInfo 확인: {}", loginInfo);

		ResponseEntity<UserLoginPostResponse> response = authorize(loginInfo);
		return response;
	}
	
	/** 
	 * 소셜 로그인(Kakao, Google)의 경우 비밀번호 생성
	 * @param id, provider
	 * @return String
	 */
	public String createSocialPassword(String id, String provider) {
		if (provider.equals("KAKAO")) {
			return id.substring(0, 6) + commonVariable.getKakaoSecret().substring(0, 6) + "#1";
		}
		
		return id.substring(0, 6) + commonVariable.getGoogleSecret().substring(0, 6) + "#2";
	}

}
