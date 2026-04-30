
# 🍕 Delivery Order Platform

음식 배달 주문 플랫폼 백엔드 서버입니다.  
사용자 인증, 가게/메뉴 관리, 주문, 결제, 리뷰 기능을 제공합니다.
---

## 문서 구성

| 문서                                            | 한 줄 요약 |
|-----------------------------------------------|-----------|
| [프로젝트 개요](./docs/project-overview.md)         | 주제, 범위, 정책, 핵심 규약 한눈에 |
| [아키텍처 가이드](./docs/tech-stack-architecture.md) | 패키지 구조, 계층별 역할, 코드 배치 규칙 |
| [공통 컴포넌트](./docs/common-components.md)        | 공통 응답 형식, 예외 처리, Base Entity |
| [API 설계 가이드](./docs/api-design-guide.md)      | RESTful 설계 원칙, 도메인별 API 목록 |
| [보안 설계](./docs/security-design.md)            | JWT 인증, 권한 재검증, 민감 정보 관리 |
| [DB 설계](./docs/database-design.md)            | ERD, 테이블 정의, 관계 및 설계 결정 사항 |
| [아키텍처](./docs/architecture.md)                 | 인프라 구성, 배포 파이프라인 |
| [시퀀스 다이어그램](./docs/sequence-diagrams.md)    | 도메인별 주요 기능 처리 흐름 |

---

## 팀원 역할 분담

| 이름  | 담당 도메인              | 주요 역할 |
|-----|---------------------|-----------|
| 김승현 | 리뷰, 결제           | - |
| 김태양 | 메뉴, AI            | - |
| 도경민 | 가게, 운영지역, 카테고리| - |
| 이진일 | Auth, 사용자, 배송지  | - |
| 최준근 | 주문, 주문상품        | - |

---

## 서비스 구성 및 실행 방법
### 시작하기 전에 — 사전 준비

#### 1. Java 17 설치 확인
```bash
java -version
# 출력 예시: openjdk version "17.x.x"
```

#### 2. PostgreSQL 설치 및 DB 생성
PostgreSQL이 설치되어 있어야 합니다.  
설치 후 아래 명령어로 데이터베이스를 생성합니다.

```sql
CREATE DATABASE deliver;
```

---
## 환경 변수 항목 설명
> ⚠️ 실제 값이 들어간 파일은 절대 Git에 커밋하지 마세요!

| 변수명                          | 설명                                                                   |
|------------------------------|----------------------------------------------------------------------|
| `DB_URL`                     | PostgreSQL 접속 URL                                                    |
| `DB_USER`                    | DB 사용자명                                                              |
| `DB_PASSWORD`                | DB 비밀번호                                                              |
| `JWT_SECRET`                 | JWT 서명에 사용하는 비밀 키 (임의의 긴 문자열)                                        |
| `JWT_ACCESS_TOKEN_EXP_MIN`   | 액세스 토큰 유효 시간 (단위: 분)                                                 |
| `JWT_REFRESH_TOKEN_EXP_DAYS` | 리프레시 토큰 유효 시간 (단위: 일)                                                |
| `GEMINI_API_URL`             | Google Gemini API 엔드포인트                                              |
| `GEMINI_API_KEY`             | Gemini API 키 ([Google AI Studio](https://aistudio.google.com/)에서 발급) |
| `AI_SERVICE_URL`             | 내부 AI 서비스 주소                                                         |
| `REDIS_HOST`                 | Redis host 이름                                                        |
| `REDIS_PORT`                 | Redis 포트                                                             |

---
## 실행방법
### 방법 1. IntelliJ IDEA에서 실행 (추천)
1. IntelliJ IDEA에서 프로젝트 열기
2. 우측 상단 실행 버튼(▶) 클릭  
   또는 `DeliveryOrderPlatformApplication.java` 파일을 열고 `main` 메서드 옆 ▶ 클릭
3. 콘솔에 `Started DeliveryOrderPlatformApplication` 메시지가 뜨면 성공!

### 방법 2. 터미널에서 실행
프로젝트 루트 디렉토리에서 아래 명령어를 실행합니다.

**macOS / Linux:**
```bash
# 빌드 + 실행
./gradlew bootRun
```

**Windows:**
```bash
gradlew.bat bootRun
```

### 방법 3. JAR 파일로 빌드 후 실행

```bash
# 빌드 (테스트 제외)
./gradlew build -x test

# 빌드된 JAR 실행
java -jar build/libs/deliveryorderplatform-0.0.1-SNAPSHOT.jar
```

---

## 실행 확인

서버가 정상 실행되면 기본적으로 `http://localhost:8080` 에서 접근할 수 있습니다.

브라우저 또는 API 테스트 툴(Postman, Insomnia 등)에서 아래 주소로 요청해보세요.

```
GET http://localhost:8080
```
