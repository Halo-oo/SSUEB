import { apiInstance } from "./index.js";

const api = apiInstance();

// [POST] 로그인
async function login(loginInfo, success, fail) {
  console.log("#user - api# 로그인 params: ", loginInfo);
  await api
    .post(`/user/login/`, JSON.stringify(loginInfo))
    .then(success)
    .catch(fail);
}

// [POST] Kakao 소셜 로그인
async function kakaoLogin(code, success, fail) {
  console.log("#user - kakaoLogin# 카카오 로그인 인가 code: ", code);
  await api
    .post(`/user/login/kakao`, JSON.stringify(code))
    .then(success)
    .catch(fail);
}

// [GET] 모든 권한 허용 + header에 token 넣어야 함
async function anyPermit(token, success, fail) {
  console.log("#user - anyPermit# 모든 권한 허용 params - token: ", token);

  await api
    .get(`/user/auth/permit`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    .then(success)
    .catch(fail);
}

// [GET] 전문가, 관리자 권한만 허용 + header에 token 넣어야 함
async function partPermit(userId, token, success, fail) {
  console.log("#user - api# 일부 권한 허용 params - userId: ", userId);
  console.log("#user - api# 일부 권한 허용 params - token: ", token);

  await api
    .get(`/user/auth/permit/${userId}`, {
      // header에 authorization bearer token 넣기
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    .then(success)
    .catch(fail);
}

export { login, kakaoLogin, anyPermit, partPermit };
