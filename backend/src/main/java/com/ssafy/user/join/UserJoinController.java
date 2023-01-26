package com.ssafy.user.join;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.common.util.ParameterCheck;
import com.ssafy.db.entity.User;
import com.ssafy.user.join.request.ConsultantJoinRequest;
import com.ssafy.user.join.request.JoinRequest;
import com.ssafy.user.join.response.BasicResponse;
import com.ssafy.user.join.response.JoinResponse;
import com.ssafy.user.login.UserLoginController;
import com.ssafy.user.login.request.UserLoginPostRequest;
import com.ssafy.user.login.response.UserLoginPostResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/user/join")
@Api(tags = { "User Join" }, description = "사용자 회원가입 API")
public class UserJoinController {
	
	
	private static final Logger logger = LoggerFactory.getLogger(UserJoinController.class);


	// 유효성 검사
	ParameterCheck parameterCheck = new ParameterCheck();

	@Autowired
	UserJoinService userJoinService;

	@Autowired
	UserJoinRepository userJoinRepository;

	@Autowired
	UserLoginController userLoginController;

	@PostMapping("/partner")
	@ApiOperation(value = "반려인 회원가입", notes = "반려인의 정보를 입력받아 회원 정보에 추가하고 로그인한다.")
	@ApiResponses(value = { @ApiResponse(code = 200, response = JoinResponse.class, message = "회원가입 성공") })
	public ResponseEntity<JoinResponse> joinPartner(JoinRequest joinRequest) {

		// 역할을 반려인으로 설정
		joinRequest.setUserRole(0);

		// User Table에 넣기
		boolean result = userJoinService.joinUser(joinRequest);

		// 아이디 변수 생성
		String userId = joinRequest.getId();

		// 권한 설정하기
		boolean resultAuthority = userJoinService.grantAuthority(userId, "ROLE_USER");

		// DB에 넣었다면
		if (result && resultAuthority) {

			// 로그인하기
			UserLoginPostRequest loginInfo = new UserLoginPostRequest(userId, joinRequest.getUserPassword());
			// ResponseEntity<UserLoginPostResponse> resultLogin =
			// userLoginController.authorize(new UserLoginPostRequest(userId,
			// joinRequest.getUserPassword()));
			ResponseEntity<UserLoginPostResponse> resultLogin = userLoginController.authorize(loginInfo);
			logger.info("#21# resultLogin 값 확인: {}", resultLogin.getBody());

			// 로그인 성공
			if (resultLogin.getBody().getResponse().equals("success")) {
				return ResponseEntity.status(200).body(new JoinResponse("success", "회원가입에 성공했습니다."));
			}

			// 로그인 실패
			else {
				return ResponseEntity.status(200).body(new JoinResponse("success", "회원가입에 성공했으나, 로그인에 실패했습니다."));
			}

		}
		return ResponseEntity.status(200).body(new JoinResponse("failure", "회원가입에 실패했습니다."));
	}

	@PostMapping(value = "/consultant", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "전문가 회원가입", notes = "전문가의 정보를 입력받아 회원 정보에 추가하고 로그인한다.")
	@ApiResponses(value = { @ApiResponse(code = 200, response = JoinResponse.class, message = "회원가입 성공") })
	public ResponseEntity<JoinResponse> joinConsultant(JoinRequest joinRequest,
			ConsultantJoinRequest consultantJoinRequest) {

		// 상담 가능한 동물 종류 가져오기
		String petType = consultantJoinRequest.getConsultPetType();

		// 상담 가능한 동물 유효성 검사
		if (!parameterCheck.isEmpty(petType) && parameterCheck.isValidPetType(petType)) {

			// 역할을 전문가로 설정
			joinRequest.setUserRole(1);

			// User Table에 넣기
			boolean resultUser = userJoinService.joinUser(joinRequest);

			// 아이디 변수 생성
			String userId = joinRequest.getId();

			// 권한 설정하기
			userJoinService.grantAuthority(userId, "ROLE_CONSULTANT");

			// DB에 정보 넣기 성공
			if (resultUser) {

				// Consultant Table에 넣기
				boolean resultConsultant = userJoinService.joinConsultant(joinRequest.getId(), consultantJoinRequest);

				if (resultConsultant) {

					// 로그인하기
					ResponseEntity<UserLoginPostResponse> resultLogin = userLoginController
							.authorize(new UserLoginPostRequest(userId, joinRequest.getUserPassword()));

					// 로그인 성공
					if (resultLogin.getBody().getResponse().equals("success")) {
						return ResponseEntity.status(200).body(new JoinResponse("success", "회원가입에 성공했습니다."));
					}

					// 로그인 실패
					else {
						return ResponseEntity.status(200)
								.body(new JoinResponse("success", "회원가입에 성공했으나, 로그인에 실패했습니다."));
					}
				}

			}
		}
		return ResponseEntity.status(200).body(new JoinResponse("failure", "회원가입에 실패했습니다."));
	}

	@GetMapping("/duplication/id")
	@ApiOperation(value = "사용자 아이디 중복 확인", notes = "사용자가 회원가입하기 전에 아이디 중복을 확인한다.")
	@ApiImplicitParam(name = "id", value = "사용자 아이디", required = true)
	public ResponseEntity<BasicResponse> duplicateId(String id) {

		// 입력 받은 id가 DB에 있는지 조회
		User user = userJoinRepository.findById(id);

		// 아이디 유효성 검사
		if (!parameterCheck.isEmpty(id) && parameterCheck.isValidId(id)) {

			// DB에 입력받은 id가 없는 경우
			if (user == null) {
				return ResponseEntity.status(200).body(new BasicResponse("success"));
			}
		}

		// DB에 입력받은 id가 있거나 유효하지 않은 id인 경우
		return ResponseEntity.status(200).body(new BasicResponse("failure"));
	}

}
