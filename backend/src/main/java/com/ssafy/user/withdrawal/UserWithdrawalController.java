package com.ssafy.user.withdrawal;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.common.util.ParameterCheck;
import com.ssafy.db.entity.User;
import com.ssafy.user.join.repository.JoinUserRepository;
import com.ssafy.user.login.response.UserLoginPostResponse;
import com.ssafy.user.withdrawal.repository.UserWithdrawalRepository;
import com.ssafy.user.withdrawal.request.UserWithdrawalPostRequest;
import com.ssafy.user.withdrawal.response.UserWithdrawalPostResponse;
import com.ssafy.user.withdrawal.service.UserWithdrawalService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = {"User/Withdrawal"}, description = "회원탈퇴  API")
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/user/withdrawal")
public class UserWithdrawalController {
	
	private static final Logger logger = LoggerFactory.getLogger(UserWithdrawalController.class);
	
	ParameterCheck parametterCheck = new ParameterCheck();
	
	@Autowired
	JoinUserRepository joinUserRepository;
	
	@Autowired
	UserWithdrawalRepository userWithdrawalRepository;
	
	@Autowired
	UserWithdrawalService userWithdrawalService;

	/** 
	 * 해당 id의 사용자 탈퇴 처리 (DB 삭제가 아닌 탈퇴 처리) 
	 * @param String
	 * @return 
	 */
	@PostMapping("/")
	@ApiOperation(value = "회원탈퇴")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = UserLoginPostResponse.class, message = "탈퇴 처리 되었습니다."),
			@ApiResponse(code = 401, response = UserLoginPostResponse.class, message = "죄송합니다. 다시 시도해 주세요.")
	})
	public ResponseEntity<UserWithdrawalPostResponse> withdrawal(@RequestBody @ApiParam(value = "회원탈퇴 id(email)", required = true) UserWithdrawalPostRequest info) {
		
		try {
			logger.info("## [Controller]: withdrawal - 회원탈퇴 실행 {}", info);
			// # 유효성 검사
			if (parametterCheck.isEmpty(info.getId()) || !parametterCheck.isValidId(info.getId())) {
				return ResponseEntity.ok(UserWithdrawalPostResponse.of(401, "failure", "죄송합니다. 다시 시도해 주세요."));
			}
			
			// # 회원탈퇴
			Optional<User> user = joinUserRepository.findById(info.getId());
			if (user.isPresent()) {
				User tempUser = user.get(); 
				tempUser.setUserDeleteFlag(1);	// 1 = 탈퇴
				
				userWithdrawalRepository.save(tempUser);
				return ResponseEntity.ok(UserWithdrawalPostResponse.of(200, "success", "탈퇴 처리 되었습니다."));
			}
			return ResponseEntity.ok(UserWithdrawalPostResponse.of(401, "failure", "죄송합니다. 다시 시도해 주세요."));
			
		}catch(Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok(UserWithdrawalPostResponse.of(401, "failure", "죄송합니다. 다시 시도해 주세요."));
		}
	}
	
	/** 
	 * 탈퇴 회원 여부 확인 (false = 탈퇴 X, true = 탈퇴) 
	 * @param id
	 * @return boolean
	 */
	public boolean checkWithdrawalUser(String id) {
		// 0 = 탈퇴 X, 1 = 탈퇴
		if (userWithdrawalService.getUserWithdrawal(id).getUserDeleteFlag() == 0) {
			return false; 
		}
		
		return true; 
	}
}
