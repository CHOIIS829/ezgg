#!/bin/bash
# EZ.GG 개발 환경 실행 스크립트
# 사용법: ./start.sh [up|down|build|logs|restart]

COMMAND=${1:-up}

# Frontend Build Step (up/build/restart 시 실행)
if [ "$COMMAND" = "up" ] || [ "$COMMAND" = "build" ] || [ "$COMMAND" = "restart" ]; then
  echo "🎨 Building Frontend..."
  cd frontend
  
  # node_modules가 없으면 설치
  if [ ! -d "node_modules" ]; then
      echo "� Installing Dependencies..."
      npm install
  fi

  npm run build
  if [ $? -ne 0 ]; then
      echo "❌ Frontend build failed! Aborting."
      exit 1
  fi
  cd ..
  echo "✅ Frontend build complete."
fi

echo "� Starting EZ.GG in mode: $COMMAND"

if [ "$COMMAND" = "up" ]; then
    # up 명령: 백그라운드 실행, 재생성, 빌드 포함
    docker compose -p ezgg --env-file .env up -d --build --force-recreate
elif [ "$COMMAND" = "down" ]; then
    # down 명령: 컨테이너 종료 및 정리
    docker compose -p ezgg --env-file .env down
else
    # 그 외 명령 (logs, ps, restart 등) 그대로 전달
    docker compose -p ezgg --env-file .env $COMMAND
fi
