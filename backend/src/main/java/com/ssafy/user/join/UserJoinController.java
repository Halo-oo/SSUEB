package com.ssafy.user.join;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.common.util.BasicResponse;
import com.ssafy.common.util.ParameterCheck;
import com.ssafy.db.entity.Consultant;
import com.ssafy.db.entity.User;
import com.ssafy.user.admin.UserAdminController;
import com.ssafy.user.join.repository.JoinConsultantRepository;
import com.ssafy.user.join.repository.JoinUserRepository;
import com.ssafy.user.join.request.ConsultantJoinRequest;
import com.ssafy.user.join.request.JoinRequest;
import com.ssafy.user.login.UserLoginController;
import com.ssafy.user.login.request.UserLoginPostRequest;
import com.ssafy.user.login.response.UserLoginPostResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;

@RestController
@RequestMapping("/api/user/join")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Api(tags = { "User/Join" }, description = "사용자 회원가입 API")
public class UserJoinController {
	
	private static final Logger logger = LoggerFactory.getLogger(UserJoinController.class);


	// 유효성 검사
	ParameterCheck parameterCheck = new ParameterCheck();

	@Autowired
	UserJoinService userJoinService;

	@Autowired
	JoinUserRepository userJoinRepository;

	@Autowired
	UserLoginController userLoginController;
	
	@Autowired
	JoinConsultantRepository joinConsultantRepository;
	
	@Autowired
	UserAdminController userAdminController;
	
	@PostMapping("/partner")
	@ApiOperation(value = "반려인 회원가입", notes = "반려인의 정보를 입력받아 회원 정보에 추가하고 로그인한다.")
	@ApiResponse(code = 200, response = BasicResponse.class, message = "반려인 회원가입 진행")
	public ResponseEntity<?> joinPartner(@RequestBody JoinRequest joinRequest) {
		
		System.out.println("#반려인 회원가입 값 확인 001# " + joinRequest);
		
		// # 소셜 로그인(Kakao)인 경우 비밀번호 생성
		if (joinRequest.getUserPassword().equals("socialKakao")) {
			joinRequest.setUserPassword(userLoginController.createSocialPassword(joinRequest.getId(), "KAKAO"));
			
		}
		
		else if (joinRequest.getUserPassword().equals("socialGoogle")) {
			joinRequest.setUserPassword(userLoginController.createSocialPassword(joinRequest.getId(), "GOOGLE"));
		}
		
		// Partner 회원가입
		boolean result = userJoinService.joinPartner(joinRequest);
//		logger.info("#21# 회원가입 result 확인: ", result);
		if (result) {
			
			// 로그인하기
			UserLoginPostRequest loginInfo = new UserLoginPostRequest(joinRequest.getId(), joinRequest.getUserPassword(), 0);
			ResponseEntity<UserLoginPostResponse> resultLogin = userLoginController.authorize(loginInfo);
			
			// 로그인 성공
			if (resultLogin.getBody().getResponse().equals("success")) {
				return resultLogin;
			}
			
			// 로그인 실패
			else {
				return ResponseEntity.status(200).body(new BasicResponse("success", "회원가입에 성공했으나, 로그인에 실패했습니다."));
			}
		}
		
		return ResponseEntity.status(200).body(new BasicResponse("failure", "회원가입에 실패했습니다."));
	}
	
	@Transactional
	@PostMapping(value = "/consultant", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "전문가 회원가입", notes = "전문가의 정보를 입력받아 회원 정보에 추가하고 로그인한다.")
	@ApiResponse(code = 200, response = BasicResponse.class, message = "전문가 회원가입 진행")
	public ResponseEntity<BasicResponse> joinConsultant(JoinRequest joinRequest,
			ConsultantJoinRequest consultantJoinRequest) {
		
		System.out.println("#전문가 회원가입 값 확인 002# " + joinRequest + consultantJoinRequest);
		
		// # 소셜 로그인(Kakao, Google)인 경우 비밀번호 생성
		if (joinRequest.getUserPassword().equals("socialKakao")) {
			joinRequest.setUserPassword(userLoginController.createSocialPassword(joinRequest.getId(), "KAKAO"));
		}
		else if (joinRequest.getUserPassword().equals("socialGoogle")) {
			joinRequest.setUserPassword(userLoginController.createSocialPassword(joinRequest.getId(), "GOOGLE"));
		}
		
		// 자격번호 유효성 검사
		if (parameterCheck.isValidLicenseNumber(consultantJoinRequest.getConsultantLicenseNumber())) {
			
			// 상담 가능한 동물 종류 가져오기
			String petType = consultantJoinRequest.getConsultPetType();
			
			// 상담 가능한 동물 유효성 검사
			if (!parameterCheck.isEmpty(petType) && parameterCheck.isValidPossiblePetType(petType)) {
				
				// 파일 크기 및 확장자 확인
				MultipartFile image = consultantJoinRequest.getConsultantLicenseCopyImage();
				
				if (parameterCheck.isValidFileSize(5000000, image) && parameterCheck.isValidImage(image, true)) {
					
					// User Table에 넣기
					boolean resultUser = userJoinService.joinUser(joinRequest, 1);
					
					// DB에 정보 넣기 성공
					if (resultUser) {
						
						// 아이디 변수 생성
						String userId = joinRequest.getId();
						
						// 권한 설정하기
						userJoinService.grantAuthority(userId, "ROLE_CONSULTANT");
						
						// Consultant Table에 넣기
						boolean resultConsultant = userJoinService.joinConsultant(userId, consultantJoinRequest);
						
						if (resultConsultant) {
							
							return ResponseEntity.status(200).body(new BasicResponse("success", "회원가입에 성공했습니다."));
						}
					}
				}
			}
		}
		
		return ResponseEntity.status(200).body(new BasicResponse("failure", "회원가입에 실패했습니다."));
	}

	@GetMapping("/duplication/id")
	@ApiOperation(value = "사용자 아이디 중복 확인", notes = "사용자가 회원가입하기 전에 아이디 중복을 확인한다.")
	@ApiImplicitParam(name = "id", value = "사용자 아이디", required = true)
	@ApiResponse(code = 200, response = BasicResponse.class, message = "아이디 중복 확인")
	public ResponseEntity<BasicResponse> duplicateId(String id) {
		
		// 아이디 유효성 검사
		if (!parameterCheck.isEmpty(id) && parameterCheck.isValidId(id)) {
			
			// 입력 받은 id가 DB에 있는지 조회
			Optional<User> user = userJoinRepository.findById(id);
			
			// DB에 입력받은 id가 없는 경우
			if (!user.isPresent()) {
				return ResponseEntity.status(200).body(new BasicResponse("success", null));
			}
		}
		
		// DB에 입력받은 id가 있거나 유효하지 않은 id인 경우
		return ResponseEntity.status(200).body(new BasicResponse("failure", null));
	}
	
	@PostMapping("/consultant/accept")
	@ApiOperation(value = "전문가 회원가입 수락", notes = "전문가의 자격 검증이 끝난 뒤, 회원가입을 수락한다.")
	@ApiResponse(code = 200, response = BasicResponse.class, message = "전문가 회원가입 수락")
	@ApiImplicitParam(name = "id", value = "전문가 아이디", required = true)
	public ResponseEntity<BasicResponse> acceptConsultant(String id) {
		
		if (!parameterCheck.isEmpty(id) && parameterCheck.isValidId(id)) {
			
			// 입력 받은 id가 DB에 있는지 조회
			Optional<Consultant> optionalConsultant = joinConsultantRepository.findById(id);
				
			// DB에 입력받은 id가 있는 경우
			if (optionalConsultant.isPresent()) {
				
				Consultant consultant = optionalConsultant.get();
				consultant.setConsultantCertified(1);
				joinConsultantRepository.save(consultant);
				
				Optional<User> optionalUser = userJoinRepository.findById(id);
				
				if (optionalUser.isPresent()) {
					
					User user = optionalUser.get();
					
					int alertFlag = user.getUserAlertFlag();
					if (alertFlag == 0) {
						// 카카오
						
					} else if (alertFlag == 1) {
						// 이메일
						userAdminController.sendCertifiedPassEmail(id);
					} else {
						// 문자
						userAdminController.sendCertifiedPassMessage(user.getUserPhone());
					}
				}
				
				return ResponseEntity.status(200).body(new BasicResponse("success", null));
			}
		}
		
		// DB에 입력받은 id가 있거나 유효하지 않은 id인 경우
		return ResponseEntity.status(200).body(new BasicResponse("failure", null));
		
	}

}
