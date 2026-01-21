# 🎮 EZ.GG - 롤 듀오 매칭 플랫폼

<p align="center">
  <img width="500" alt="logo" src="https://github.com/user-attachments/assets/1d9a3d77-44ac-43df-af54-5ab5708e1c7a" />
</p>

<p align="center">
  <strong>리그 오브 레전드 듀오 파트너 매칭 서비스</strong><br>
  플레이 스타일과 성향에 맞는 최적의 듀오 파트너를 찾아보세요!
</p>

## 🛠 기술 스택

| Category | Tech Stack |
| :--- | :--- |
| **Language** | <img src="https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=Java&logoColor=white"/> |
| **Backend** | <img src="https://img.shields.io/badge/Spring%20Boot-3.4.4-6DB33F?style=flat-square&logo=Spring%20Boot&logoColor=white"/> <img src="https://img.shields.io/badge/Spring%20Security-Enabled-6DB33F?style=flat-square&logo=Spring%20Security&logoColor=white"/> <img src="https://img.shields.io/badge/OpenAI-API-412991?style=flat-square&logo=OpenAI&logoColor=white"/> <img src="https://img.shields.io/badge/Riot%20Games-API-D32936?style=flat-square&logo=Riot%20Games&logoColor=white"/> |
| **Frontend** | <img src="https://img.shields.io/badge/React-19.1-61DAFB?style=flat-square&logo=React&logoColor=black"/> <img src="https://img.shields.io/badge/Vite-5.1.0-646CFF?style=flat-square&logo=Vite&logoColor=white"/> <img src="https://img.shields.io/badge/Emotion-Styled-DB7093?style=flat-square&logo=Emotion&logoColor=white"/> |
| **DB** | <img src="https://img.shields.io/badge/ElasticSearch-8.9.0-005571?style=flat-square&logo=ElasticSearch&logoColor=white"/> <img src="https://img.shields.io/badge/Redis-Cache-DC382D?style=flat-square&logo=Redis&logoColor=white"/> |
| **Infra** | <img src="https://img.shields.io/badge/Docker-Enabled-2496ED?style=flat-square&logo=Docker&logoColor=white"/> |

<br>

## 🚀 프로젝트 소개

EZ.GG는 리그 오브 레전드를 즐기는 유저들을 위한 **듀오 매칭 플랫폼**입니다.  
혼자가 아닌 **듀오로 랭크를 올리고 싶은 유저들**에게 자신의 플레이 스타일과 성향에 맞는 파트너를 찾을 수 있는 서비스를 제공합니다.


## 🔍 나의 구현한 기능


### Spring Security & JWT
- **로그인**
<p align="center">
<img width="1385" height="753" alt="Image" src="https://github.com/user-attachments/assets/1e40e2a7-dd9d-40bb-bc35-8861cb99c586" />
</p>

1. `UsernamePasswordAuthenticationFilter`를 상속받은 `LoginFilter`를 만들어 로그인 기능 구현
2. `/login`으로 요청이 들어오면 `attemptAuthentication`에서 요청을 캐치하여 `UserDetailsService`를 상속받아 구현한 `CustomUserDetailsService`를 통하여 사용자 조회
3. 로그인 성공 시 `successfulAuthentication`에서 `JWTUtil`을 통하여 token을 생성하고 refreshToken은 redis에 저장하여 관리함
4. 로그인 실패 시 `unsuccessfulAuthentication`에서 401에러를 반환시킴

- **로그아웃**
<p align="center">
<img width="1310" height="491" alt="Image" src="https://github.com/user-attachments/assets/de0d9e15-d77c-4db5-b815-131347e5c707" />
</p>

1. 로그아웃 요청을 하면 Http Header에 포함된 accessToken을 가지고 redis에 토큰의 만료시간을 TTL로 설정하여 BlackList로 저장
2. 기존에 redis에서 관리하던 refreshToken은 삭제
3. 쿠키에 저장하여 관리했던 refreshToken을 만료시키고 200 반환

- **RefreshToken 재발급**
<p align="center">
<img width="962" height="469" alt="Image" src="https://github.com/user-attachments/assets/2b0b183f-c490-4d2b-aa7f-f5f981590ef8" />
</p>

1. refreshToken 재발급 요청이 들어오면 `RefreshService`에서 요청이 들어온 refreshToken의 유효성을 검사
2. 검증이 성공하면 `JWTUtil`을 통하여 새로운 refreshToken을 발급하고 redis에서 관리하던 refreshToken은 삭제하고 새로운 token을 저장

- **인증이 필요한 api**
<p align="center">
<img width="853" height="446" alt="Image" src="https://github.com/user-attachments/assets/06fabde3-dcc0-4d0e-87de-a96bc468c6bb" />
</p>

1. `SecurityConfig`에서 `requestMatchers`를 통해 허용한 요청을 제외한 모든 요청은 `OncePerRequestFilter`를 통해 구현한 `JWTFilter`를 통해 인증 검사를 받음
2. `JWTUtil`을 통해 accessToken의 유효성을 검사하고 redis에 accessToken이 BlackList처리 되어있는지 확인
3. 모든 검증을 통과하면 `SecurityContext`에 인증 객체 설정

### 매칭 시스템
<p align="center">
<img width="1305" height="788" alt="Image" src="https://github.com/user-attachments/assets/82aae7f8-2f53-468d-b19f-5a0489215911" />
</p>

1. `/matching/start` 웹소켓 요청이 들어오면 redis의 stream과 Hash에 매칭요청이 들어온 유저의 정보를 저장
2. 1초 주기로 consumer가 stream을 확인하여 매칭을 시도할 유저가 있는지 확인
3. 매칭시도할 유저가 존재하면 `tryMatching`에서 Elasticsearch에 조건이 맞는 매칭유저를 검색
4. 검색된 유저가 존재하지 않으면 redis의 retryZSet에 10초의 시간을 설정한 후 저장
5. `MatchingRetryScheduler` 3초 간격으로 retryZSet을 확인하여 재시도 가능한 유저를 다시 stream과 Hash에 저장
6. Elasticsearch에서 매칭유저가 검색되면 매칭에 성공한 두 유저에게 /queue/matching 경로로 웹소켓을 통해 매칭된 상대방의 정보를 전송

### 리뷰 알림 시스템
<p align="center">
<img width="973" height="587" alt="Image" src="https://github.com/user-attachments/assets/b86eb9a4-e223-48ee-989e-5057db789cc0" />
</p>

1. 매칭이 성공된 시점에 redis의 match-success에 매칭된 두 유저의 정보와 매칭이 완료된 시점의 시간을 저장
2. Scheduled가 1분 주기로 매칭이 완료된지 10분이 지난 유저를 검색
3. 검색된 유저는 현재시간으로 다시 redis에 저장
4. `findDuoGame`에서 riotApi를 통해 새로운 MatchIds와 매치게임정보를 조회
5. 조회된 MatchIds와 매치게임 정보를 통해 매칭된 두 유저가 실제로 한팀에서 같은게임을 했는지 확인
6. 확인된 매칭유저들은 MySQL에 review데이터를 생성하고 redis의 pending-review에 작성해야될 리뷰데이터를 저장
7. 웹소켓의 연결 여부를 판별하여 웹소켓이 연결되어있는 유저들을 /user/queue/review로 리뷰 알림 발송

### ✨ 주요 기능
- 🔍 **스마트 검색**: 플레이 스타일 기반 듀오 파트너 검색
<p align="center">
<img width="800" alt="스마트 검색" src="https://github.com/user-attachments/assets/9a4931e2-1bbf-453c-aaab-6e6d182788f5" />
</p>

- 🎯 **실시간 매칭**: WebSocket을 활용한 즉시 매칭
<p align="center">
<img width="800" alt="스마트 검색" src="https://github.com/user-attachments/assets/3f0e6ee0-cae7-415c-9d77-44633dfc929f" />
</p>

- 📊 **통계 분석**: 라이엇 API 연동으로 정확한 게임 데이터 제공
<p align="center">
<img width="809" alt="타임라인" src="https://github.com/user-attachments/assets/28b3eaab-f2db-49e2-8aea-bcc06d8fddbf" />
</p>
 
- 💬 **커뮤니케이션**: 매칭 후 원활한 소통 지원
<p align="center">
<img width="800" alt="채팅화면" src="https://github.com/user-attachments/assets/1a449ccf-64a3-4428-8710-8aedf3f3414a" />
</p>

## 🏗 서비스 아키텍처

<p align="center">
  <img width="800" alt="아키텍처" src="https://github.com/user-attachments/assets/129b0b13-3a47-42af-90fd-92587cad1988" />
</p>

<<<<<<< Updated upstream
## 🛠 기술 스택

### Backend
- **Java 21** & **Spring Boot 3.4.4**
- **WebSocket** - 실시간 매칭 시스템
- **ElasticSearch** - 검색 및 매칭 알고리즘
- **Redis** - 캐싱 및 세션 관리
- **Docker** - 컨테이너화

### Frontend
- **React 19.1** + **Vite**
- **Node.js 23.11.0** / **npm 11.3.0**

### External APIs
- **Riot Games API** - 게임 데이터 연동
- **OpenAI API** - 자연어 처리 매칭

=======
>>>>>>> Stashed changes
## 📨 Contact
- **Developer**: INSU
- **Email**: cth7097@naver.com
- **GitHub**: [github.com/CHOIIS829](https://github.com/CHOIIS829)
