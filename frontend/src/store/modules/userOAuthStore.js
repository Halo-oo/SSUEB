import {
  postReceiveKakaoAuthCode,
  withdrawalKakao,
  executeGoogleLogin,
  withdrawalGoogle,
  sendKakaoMessage,
} from "@/api/userOAuth";
import store from "..";

const userOAuthStore = {
  namespaced: true,
  state: {
    kakaoToken: null,
    googleToken: null,
  },
  getters: {
    getKakaoToken: (state) => {
      return state.kakaoToken;
    },
    getGoogleToken: (state) => {
      return state.googleToken;
    },
  },
  mutations: {
    // #Kakao# 발급받은 Kakao Token 저장
    SET_KAKAO_TOKEN: (state, kakaoToken) => {
      state.kakaoToken = kakaoToken;
    },
    // #Google# 발급받은 Google Token 저장
    SET_GOOGLE_TOKEN: (state, googleToken) => {
      state.googleToken = googleToken;
    },
  },
  actions: {
    // [@Method] Kakao 인가코드 전달
    async receiveKakaoAuthCode({ commit }, code) {
      commit;
      // console.log("#OAuth# Kakao 인가코드 확인: ", code);

      await postReceiveKakaoAuthCode(
        code,
        ({ data }) => {
          // i) 회원가입이 필요한 카카오 사용자일 경우
          if (data.statusCode == 401 && data.response == "need_register") {
            // console.log("#OAuth# Kakao 회원가입 필요 _data: ", data);
            const info = {
              id: data.socialUserEmail,
              nickname: data.socialUserNickname,
              provider: "KAKAO",
            };

            // 소셜 로그인 유저 정보(userSocialStore) store에 id, nickname 저장
            store.dispatch("setSocialUserInfo", info);
          }
          // ii) 로그인 성공
          else if (data.statusCode == 200) {
            // console.log("#OAuth# Kakao 로그인 성공 _data: ", data);
            store.dispatch("userStore/setAutoLogin", data, { root: true });
          }
          // iii) 로그인 실패
          else {
            store.dispatch("userStore/resetUserInfo", data, { root: true });
          }
        },
        (error) => {
          console.log("#OAuth# Kakao 로그인 에러 _error: ", error);
        }
      );
    },
    // [@Method] Google 로그인 진행
    async googleLogin({ commit }, token) {
      commit;
      // console.log("#OAuth# Google 로그인 진행 access_token 확인: ", token);

      await executeGoogleLogin(
        token,
        ({ data }) => {
          // i) 회원가입이 필요한 구글 사용자일 경우
          if (data.statusCode == 401 && data.response == "need_register") {
            // console.log("#OAuth# Google 회원가입 필요 _data: ", data);
            const info = {
              id: data.socialUserEmail,
              nickname: "",
              provider: "GOOGLE",
            };

            // 소셜 로그인 유저 정보(userSocialStore) store에 id, nickname 저장
            store.dispatch("setSocialUserInfo", info);
          }
          // ii) 로그인 성공
          else if (data.statusCode == 200) {
            // console.log("#OAuth# Google 로그인 성공 _data: ", data);
            store.dispatch("userStore/setAutoLogin", data, { root: true });
          }
          // iii) 로그인 실패
          else {
            store.dispatch("userStore/resetUserInfo", data, { root: true });
          }
        },
        (error) => {
          console.log("#OAuth# Google 로그인 에러 _error: ", error);
        }
      );
    },
    // [@Method] #Kakao# 회원탈퇴
    async excuteWithdrawalKakao(context) {
      //if (context.state.kakaoToken == null) return;
      context;
      if (localStorage.getItem("kakaoToken") == null) return;
      // console.log("#userOAuthStore# KAKAO 회원탈퇴 동작");

      await withdrawalKakao(
        //context.state.kakaoToken,
        // localStorage.getItem("kakaoToken"),
        // ({ data }) => {
        //   console.log(
        //     "#userOAuthStore - withdrawalKakao# Kakao 연결끊기 성공: ",
        //     data
        //   );
        // },
        (error) => {
          console.log(error);
        }
      );
    },
    // [@Method] #Google# 회원탈퇴
    async excuteWithdrawalGoogle(context) {
      if (context.state.googleToken == null) return;
      // console.log("#userOAuthStore# GOOGLE 회원탈퇴 동작");

      await withdrawalGoogle(
        // ({ data }) => {
        //   console.log(
        //     "#userOAuthStore - withdrawalGoogle# Google 연결끊기 성공: ",
        //     data
        //   );
        // },
        (error) => {
          console.log(error);
        }
      );
    },
    // [@Method] 카카오 메세지 보내기 (나에게)
    async excutedSendKakaoMessage() {
      // console.log("#21# 카카오톡 전송 동작");
      await sendKakaoMessage(
        // ({ data }) => {
        //   console.log("#21# Kakao 톡 전송 성공: ", data);
        // },
        (error) => {
          console.log("#21# Kakao 톡 전송 실패: ", error);
        }
      );
    },
  },
};

export default userOAuthStore;
