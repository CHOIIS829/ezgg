# 🎮 EZ.GG - 롤 듀오 매칭 플랫폼

<p align="center">
  <img width="500" alt="logo" src="https://github.com/user-attachments/assets/1d9a3d77-44ac-43df-af54-5ab5708e1c7a" />
</p>

<p align="center">
  <strong>리그 오브 레전드 듀오 파트너 매칭 서비스</strong><br>
  플레이 스타일과 성향에 맞는 최적의 듀오 파트너를 찾아보세요!
</p>

<br>

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

<br>

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

<br>


### ✨ 주요 기능
---

#### ▼ 🧩 로그인 & 회원가입
<p align="center">
<img src="https://github.com/user-attachments/assets/86436b8a-a911-49a9-97b9-73a37199a816" width="45%" />
<img src="https://github.com/user-attachments/assets/26866e40-3927-4f07-b846-b165c7ba765e" width="45%" />
</p>

- ✅ **JWT + Spring Security 기반 로그인 구현**
- 🔐 **Access/Refresh 토큰 구조 + Redis 저장소 관리**
- 🧹 **로그아웃 시 토큰 블랙리스트 처리 및 쿠키 삭제**
- 🔒 **BCryptPasswordEncoder를 활용한 비밀번호 암호화**
- 🚫 **미로그인 상태로 접근 시 로그인 페이지로 리다이렉트**

<br>

#### ▼ 🏠 메인페이지
<p align="center">
<img src="https://github.com/user-attachments/assets/c433f558-b5cc-4081-aadd-f5eb48af0d0c" width="800" />
</p>

- 🧭 **사용자 정보와 매칭 조건을 UI 섹션으로 분리하여 배치**
- 👀 **꼭 필요한 정보만 표시해 심플하고 직관적인 UI 구성**
- 📊 **최근 경기 기반 요약 정보 제공 (내역이 포함되어 있으면 명시 가능)**

<br>

#### ▼ 🎯 매칭 페이지
<p align="center">
<img src="https://github.com/user-attachments/assets/5a4e23f5-cb00-42c9-81a5-2f232970ec3d" width="45%" />
<img src="https://github.com/user-attachments/assets/0b98d638-a241-4139-a46d-a61a7c966077" width="45%" />
</p>

- 👨‍🦱 **왼쪽: 나의 최근 20경기 정보 및 티어/포지션 정보 표시**
- 🎛️ **오른쪽: 매칭 조건 설정 및 시작 버튼 제공**
- 🔍 **매칭 조건 설정 후, 사용자에게 입력 조건을 다시 보여줘 실수 방지**
- 🧠 **사용자 경험 중심의 매칭 인터페이스 설계**

<br>

#### ▼ 🤝 매칭 완료 페이지
<p align="center">
<img src="https://github.com/user-attachments/assets/9dd54cf4-a78c-4d3e-befe-3005ab4c2163" width="800" />
</p>

- 👫 **매칭된 상대방의 요약 정보를 직관적으로 표시**
- 💬 **실시간 채팅 기능 (WebSocket 기반)**
- 🆔 **매칭 유저의 롤 닉네임 + 태그 복사 버튼 제공**

<br>

#### ▼ 🕒 듀오 타임라인
<p align="center">
<img src="https://github.com/user-attachments/assets/9a0b6a67-93dd-432a-b381-1340952e3083" width="800" />
</p>

- 📜 **지금까지 매칭된 유저 목록 + 실제로 게임을 진행한 기록을 시각적으로 제공**
- 🕹️ **타임라인 기반으로 과거 듀오 활동 이력 파악 가능**

<br>

#### ▼ 📝 리뷰 시스템
<p align="center">
<img src="https://github.com/user-attachments/assets/980e1969-dcc5-45bb-afb2-19a6bd874fe4" width="800" />
</p>

- ✍️ **실제 듀오 플레이가 확인된 유저에 한해 리뷰 작성 가능**
- 🧩 **Riot API를 통해 게임 내 매칭 여부(같은 팀, 같은 게임) 검증**
- 📨 **리뷰가 필요한 유저에게 웹소켓 기반 알림 발송**
- 🗄️ **리뷰 데이터는 MySQL 저장 + Redis의 pending-review로 큐 관리**

<br>

## 📨 Contact
- **Developer**: INSU
- **Email**: cth7097@naver.com
- **GitHub**: [github.com/CHOIIS829](https://github.com/CHOIIS829)
