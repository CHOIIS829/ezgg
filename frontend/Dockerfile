# Builder Stage
FROM node:20-alpine AS builder

WORKDIR /app

# 의존성 설치 캐싱을 위해 package.json 먼저 복사
COPY package*.json ./

# npm install 보다 npm ci가 CI/CD 환경 및 재현성에 유리함
RUN npm ci

# 소스코드 복사 및 빌드
COPY . .
RUN npm run build

# Runner Stage
FROM node:20-alpine

WORKDIR /app

# 경량 실행을 위해 serve 패키지 설치
RUN npm install -g serve

# Builder 스테이지에서 빌드된 정적 파일만 복사
COPY --from=builder /app/dist ./dist

# 포트 설정 통일 (3030)
EXPOSE 3030

# 포트 지정하여 실행
CMD ["serve", "-s", "dist", "-l", "3030"]
