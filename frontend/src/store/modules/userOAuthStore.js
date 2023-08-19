import { duplicateId } from "@/api/userJoin";
import {
  getKakaoToken,
  postReceiveKakaoAuthCode,
  getKakaoUserInfo,
  withdrawalKakao,
  getGoogleInfo,
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
      // #Kakao# 현재 로그인한 Kakao 사용자 정보 가져오기
      getKakaoUserInfo(kakaoToken);
    },
    // #Google# 발급받은 Google Token 저장
    SET_GOOGLE_TOKEN: (state, googleToken) => {
      state.googleToken = googleToken;
    },
  },
  actions: {
    // [@Method] #Kakao# Kakao Token 발급받기 (Kakao 인가 코드 사용)
    async excuteKakaoToken({ commit }, code) {
      const kakaoInfo = {
        grant_type: "authorization_code",
        client_id: process.env.VUE_APP_OAUTH_KAKAO_CLIENT,
        redirect_uri: process.env.VUE_APP_OAUTH_REDIRECT_URI,
        code: code,
        client_secret: process.env.VUE_APP_OAUTH_KAKAO_CLIENT_SECRET,
      };

      await getKakaoToken(
        kakaoInfo,
        ({ data }) => {
          console.log(
            "#userOAuthStore - getKakaoToken# Kakao Token 발급 성공: ",
            data
          );
          // 발급받은 access-token을 통해 현재 로그인한 사용자 정보 가져오기
          localStorage.setItem("kakaoToken", data.access_token);
          commit("SET_KAKAO_TOKEN", data.access_token);
        },
        (error) => {
          console.log(error);
        }
      );
    },
    // [@Method] Kakao 인가코드 전달
    async receiveKakaoAuthCode({ commit }, code) {
      commit;
      // console.log("#OAuth# Kakao 인가코드 확인: ", code);

      await postReceiveKakaoAuthCode(
        code,
        ({ data }) => {
          // i) 회원가입이 필요한 카카오 사용자일 경우
          if (data.statusCode == 401 && data.response == "need_register") {
            console.log("#OAuth# Kakao 회원가입 필요 _data: ", data);
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
            console.log("#OAuth# Kakao 로그인 성공 _data: ", data);
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
      console.log("#OAuth# Google 로그인 진행 access_token 확인: ", token);

      await executeGoogleLogin(
        token,
        ({ data }) => {
          // i) 회원가입이 필요한 구글 사용자일 경우
          if (data.statusCode == 401 && data.response == "need_register") {
            console.log("#OAuth# Google 회원가입 필요 _data: ", data);
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
            console.log("#OAuth# Google 로그인 성공 _data: ", data);
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
    // [@Method] #Google# Google 사용자 정보 가져오기 > 회원가입 OR 로그인
    async excuteGoogleInfo({ commit }, token) {
      // Google Token 저장
      localStorage.setItem("googleToken", token);
      commit("SET_GOOGLE_TOKEN", token);

      await getGoogleInfo(
        token,
        async ({ data }) => {
          // console.log("#21# Google 사용자 정보: ", data);
          // console.log("#21# Google 사용자 email: ", data.email);
          const info = {
            id: data.email,
            nickname: "",
            provider: "GOOGLE",
          };

          var duplicateResult = false; // 현재 로그인한 사용자 중복 확인 (회원가입 여부 학인)
          await duplicateId(info.id).then((res) => {
            duplicateResult = res;
          });
          // * 만약 이 아이디로 회원가입 한 사용자가 없다면 > 회원가입 페이지로 이동
          if (duplicateResult == true) {
            store.dispatch("setSocialUserInfo", info); // userSocialStore에 id(email) 저장
          }
          // * 있다면 > 로그인
          else {
            // 로그인 JWT 토큰 발행 > (userStore 내 로그인 함수 호출)
            const loginInfo = {
              id: info.id,
              password: `${process.env.VUE_APP_OAUTH_GOOGLE}`,
              socialButton: 1,
            };
            store.dispatch("userStore/excuteLogin", loginInfo, { root: true });
          }
        },
        (error) => {
          console.log(error);
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
