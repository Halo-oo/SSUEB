import axios from "axios";
import { apiInstance } from "./index.js";

const api = apiInstance();

// #Kakao API#
// Kakao 사용자 정보를 가져오기 & 메세지 전송을 위한 API
const kakao_api_info = axios.create({
  baseURL: "https://kapi.kakao.com",
});
// Kakao 연결끊기를 위한 API
const kakao_api_disconnect = axios.create({
  baseURL: "https://kapi.kakao.com",
  headers: {
    "Content-Type": "application/x-www-form-urlencoded;charset=utf-8",
    Authorization: `Bearer ${localStorage.getItem("kakaoToken")}`,
  },
});

// #Google API#
// Google 연결끊기를 위한 API
const google_api_disconnect = axios.create({
  baseURL: "https://accounts.google.com",
  "Content-Type": "application/x-www-form-urlencoded;charset=utf-8",
  Authorization: `Bearer ${localStorage.getItem("googleToken")}`,
});

// #OAuth# [POST] Kakao 인가 코드 전달하기
async function postReceiveKakaoAuthCode(kakaoAuthCode, success, fail) {
  await api
    .post(
      `${process.env.VUE_APP_API_BASE_URL}/user/login/get-kakao-auth-code`,
      JSON.stringify(kakaoAuthCode)
    )
    .then(success)
    .catch(fail);
}

// [POST] #Kakao# 연결끊기
async function withdrawalKakao(success, fail) {
  // x-www-form-urlencoded 형식으로 파라미터 보내기
  await kakao_api_disconnect.post(`v1/user/unlink`).then(success).catch(fail);
}

// #OAuth# [POST] Google 로그인 진행
async function executeGoogleLogin(googleAccessToken, success, fail) {
  await api
    .post(
      `${process.env.VUE_APP_API_BASE_URL}/user/login/execute-google-login`,
      JSON.stringify(googleAccessToken)
    )
    .then(success)
    .catch(fail);
}

// [POST] #Google# 연결끊기
async function withdrawalGoogle(success, fail) {
  await google_api_disconnect
    .post(`/o/oauth2/revoke?token=${localStorage.getItem("googleToken")}`)
    .then(success)
    .catch(fail);
}

// [POST] #Kakao# 카카오 메세지 보내기
async function sendKakaoMessage(success, fail) {
  // 전송할 메세지
  const data = {
    object_type: "feed",
    content: {
      title: "자격 증명이 확인되었습니다.",
      description: "[SSUEB] 등록하신 자격증이 검증되었습니다.",
      image_url:
        "https://i.pinimg.com/564x/46/a8/3a/46a83ad0d9308c7ee46af3833e898e54.jpg",
      image_width: 640,
      image_height: 640,
      link: {
        web_url: "https://i8a801.p.ssafy.io/",
      },
    },
    buttons: [
      {
        title: "웹으로 이동",
        link: {
          web_url: "https://i8a801.p.ssafy.io/",
        },
      },
    ],
  };

  await kakao_api_info
    .post(
      `/v2/api/talk/memo/default/send`,
      { template_object: JSON.stringify(data) },
      {
        headers: {
          "Content-Type": "application/x-www-form-urlencoded;charset=utf-8",
          Authorization: `Bearer ${localStorage.getItem("kakaoToken")}`,
        },
      }
    )
    .then(success)
    .catch(fail);
}

export {
  postReceiveKakaoAuthCode,
  withdrawalKakao,
  executeGoogleLogin,
  withdrawalGoogle,
  sendKakaoMessage,
};
