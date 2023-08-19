<template>
  <NowLoading v-if="!loaded"></NowLoading>
  <div v-else class="main-page">
    <div class="main-center">
      <div class="main-center-item first">
        <img class="image" :src="require('@/assets/main/wind.gif')" />
        <div class="explain one">
          <h2>SSUEB 소개</h2>
          <p>
            반려동물에 대한 고민을 해결하기 위해 만들어진 플랫폼입니다.<br />
            화상 미팅을 통해 반려동물행동지도사를 만나 반려동물에 대한 여러 가지
            궁금증을 해결해 보세요. 반려인이 상담을 신청하면, 전문가가 이에 상담
            제안을 보내고, 이를 수락하면 상담이 진행됩니다.
          </p>
        </div>
      </div>
      <div class="main-center-item second">
        <div class="explain two">
          <h2>반려인 이용방법</h2>
          <p>
            개, 고양이, 토끼, 패럿, 기니피그, 햄스터에 대해 상담받고 싶은 누구나
          </p>
          <p>
            (1) 신규 상담 등록에서 반려동물에 대한 상담을 신청합니다.<br />
            (2) 이후 전문가의 상담 제안을 확인하고, 원하는 전문가를
            선택합니다.<br />
            (3) 예약 시간에 화상 상담 입장에서 화상 상담을 진행합니다.
          </p>
        </div>
        <img class="image" :src="require('@/assets/main/startle.gif')" />
      </div>
      <div class="main-center-item third">
        <img class="image" :src="require('@/assets/main/walk.gif')" />
        <div class="explain three">
          <h2>전문가 이용방법</h2>
          <p>
            반려동물행동지도사 자격을 보유하고, 반려동물 상담 자격을 갖춘 사람
          </p>
          <p>
            (1) 신규 상담 제안에서 상담을 원하는 반려인에게 상담을
            제안합니다.<br />
            (2) 이후 반려인이 상담 제안을 수락하면 상담이 확정됩니다.<br />
            (3) 예약 시간에 화상 상담 입장에서 화상 상담을 진행합니다.
          </p>
        </div>
      </div>
    </div>
    <div class="main-right">
      <!-- isLogin : [false - 로그인 X / true - 로그인 O]-->
      <div v-if="userAuth == null" class="main-right-item">
        <UserLogin class="card"></UserLogin>
        <!-- <UserAlert class="card" v-show="false"></UserAlert> -->
      </div>
      <div v-else class="main-right-item">
        <UserMainAlert class="mb-5"></UserMainAlert>
        <BoardTopFive></BoardTopFive>
      </div>
      <div class="main-right-item bottom">
        <MainPageChatBot></MainPageChatBot>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from "vuex";
import UserLogin from "../UserLogin/UserLogin.vue";
// import UserAlert from "../UserLogin/UserAlert.vue";
import MainPageChatBot from "@/components/MainPage/MainPageChatBot.vue";
import BoardTopFive from "@/components/MainPage/BoardTopFive.vue";
import UserMainAlert from "@/components/MainPage/UserMainAlert.vue";
import ChatbotService from "@/api/chatbotService.js";

import NowLoading from "@/views/NowLoading.vue";

const userStore = "userStore";
const userOAuthStore = "userOAuthStore";

export default {
  name: "mainPage",
  data() {
    return {
      kakaoCode: null,
      googleToken: null,
      loaded: false,
    };
  },
  created() {
    this.loaded = false;
    // #OAuth - 인가 code 받기 (Google, Kakao)
    // i) Kakao 인가 code
    this.kakaoCode = this.$route.query.code;
    if (this.kakaoCode != null) {
      // console.log("#21# kakao 인가 code 확인: ", this.kakaoCode);
      this.kakao();
    }
    // ii) Google access_token
    else {
      const url = new URL(window.location.href);
      const hash = url.hash;
      if (hash) {
        this.googleToken = hash.split("=")[1].split("&")[0];
        // console.log("#21# google access_token 확인: ", this.googleToken);
        this.google();
      }
    }
    ChatbotService.boot({
      pluginKey: "3f36e5e1-896a-4ff5-8493-6695d1b76b19", //please fill with your plugin key
      hideChannelButtonOnBoot: true,
    });

    this.loaded = true;
  },
  components: {
    UserLogin,
    // UserAlert,
    MainPageChatBot,
    BoardTopFive,
    UserMainAlert,
    NowLoading,
  },
  computed: {
    ...mapState(userStore, ["userAuth"]),
  },
  methods: {
    ...mapActions(userOAuthStore, [
      "excuteKakaoToken",
      "receiveKakaoAuthCode",
      "excuteGoogleInfo",
    ]),
    // #OAuth - Kakao# 받은 인가 코드를 사용하여 Kakao Token 발급요청
    // async kakao() {
    //   await this.excuteKakaoToken(this.kakaoCode);
    //   this.kakaoCode = null; // 받은 인가 code 초기화
    // },
    // #OAuth# Kakao 인가 코드 전달
    async kakao() {
      await this.receiveKakaoAuthCode(this.kakaoCode);
      this.kakaoCode = null; // 받은 인가 code 초기화
    },
    // #OAuth - Google# 받은 access_token을 사용하여 사용자 정보 요청
    async google() {
      await this.excuteGoogleInfo(this.googleToken);
      this.googleToken = null; // 받은 access_token 초기화
    },
  },
};
</script>

<style scoped>
.main-page {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
}
.main-right {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;

  width: 290px;
  height: 654.4px;
}
.main-right .main-right-item {
  align-self: start;
}
.main-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;

  width: 730px;
  height: 654.4px;
  margin-right: 48px;
}
.main-center .main-center-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;

  width: 730px;
  height: 200px;
}
.main-center .main-center-item img {
  width: 200px;
  height: 200px;

  border-radius: 10px;
  object-fit: cover;
}
.main-center .main-center-item .explain {
  width: 500px;
  height: 200px;

  border-radius: 10px;
  padding: 30px;
}
.main-center .main-center-item .explain h2 {
  font-size: 27px;
  font-family: "yg-jalnan";
  font-weight: lighter;
}
.main-center .main-center-item .explain.one {
  background-color: #e8ebf6;
  color: #1f3967;
}
.main-center .main-center-item .explain.two {
  background-color: #3d6ec5;
  color: white;
}
.main-center .main-center-item .explain.three {
  background-color: #e3e3e3;
  color: #353535;
}
</style>
