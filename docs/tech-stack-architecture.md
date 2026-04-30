# 기술 스택 & 아키텍처 (Tech Stack & Architecture)

## 기술 스택

| 분류 | 기술 |
|------|------|
| 언어 | Java 17 |
| 프레임워크 | Spring Boot 3.3.4 |
| 보안 | Spring Security + JWT |
| ORM | JPA / Hibernate, QueryDSL 5.0 |
| 데이터베이스 | PostgreSQL |
| 캐시 | Redis 7 |
| 빌드 툴 | Gradle |
| 인프라 | Docker / Docker Compose |
| 버전 관리 | Git / GitHub |
| API 문서 | Swagger (springdoc-openapi 2.5.0) |
| AI 연동 | Gemini API |
| 테스트 커버리지 | JaCoCo 0.8.12 |

---

## 아키텍처: Monolithic Application

### Monolithic이란?

> 모든 기능(인증, 주문, 메뉴, 배달 등)이 **하나의 애플리케이션 안에 통합**되어 있는 구조입니다.
> 마이크로서비스(MSA)와 반대 개념으로, 기능별로 서버를 나누지 않고 단일 서버에서 모든 것을 처리합니다.

**장점:**
- 개발 초기 단계에서 구조가 단순하고 빠르게 개발 가능
- 배포가 한 번에 이루어져 관리가 쉬움
- 팀 규모가 작을 때 협업 비용이 낮음

**단점:**
- 서비스가 커질수록 코드가 복잡해짐
- 특정 기능만 스케일 아웃(확장)하기 어려움

---

## 도메인 구조 규칙

- 각 도메인별로 **Entity** 클래스를 독립적으로 작성
- 기능별로 **DTO(Data Transfer Object)** 를 분리하여 관리

| 도메인 | 설명 | 비고 |
|--------|------|------|
| User (사용자) | 회원가입, 로그인, 사용자 관리 | PK: username (유저만 UUID 예외) |
| Area (운영 지역) | 운영 지역 관리 | 초기 광화문, 향후 확장 |
| Category (카테고리) | 음식점 분류 | 한식, 중식, 분식, 치킨, 피자 |
| Store (가게) | 가게 등록/관리 | OWNER 소유, 카테고리+지역 연결 |
| Menu (메뉴/상품) | 가게별 메뉴 관리 | 가격, 설명, 숨김 처리, AI 설명 생성 |
| Order (주문) | 주문 생성 및 상태 관리 | 5분 이내 취소, 상태 흐름 관리 |
| OrderItem (주문 상품) | 주문에 포함된 개별 메뉴 | 수량, 주문 당시 단가 스냅샷 |
| Review (리뷰) | 주문 완료 후 리뷰 및 평점 | 1~5점, 1주문 1리뷰 |
| Payment (결제) | 주문 결제 정보 | 카드만, PG 연동 없이 DB 저장 |
| Address (배송지) | 사용자 배송지 관리 | 주소지 필수 |
| AI Request Log (AI 요청 로그) | AI API 요청/응답 기록 | 질문+답변 모두 저장 |

---

## 레이어드 아키텍처 (Layered Architecture)

```
[Client 요청]
      ↓
[ Controller Layer ]   ← HTTP 요청/응답 처리, DTO 변환
      ↓
[  Service Layer   ]   ← 비즈니스 로직 처리
      ↓
[ Repository Layer ]   ← DB 접근 (JPA)
      ↓
[   Database (PostgreSQL)   ]
```


## 패키지 구조 (Package Structure)

### 전체 구조
```
src/main/java/com/sparta/deliveryorderplatform
│
├── global                          # 전역 설정 및 공통 모듈
│   ├── config
│   │   ├── SecurityConfig.java      # Spring Security 설정
│   │   ├── RedisConfig.java         # Redis 설정
│   │   ├── QueryDslConfig.java      # QueryDSL 설정
│   │   ├── SwaggerConfig.java       # Swagger 설정
│   │   ├── JpaAuditingConfig.java   # JPA Auditing 설정
│   │   └── AppConfig.java           # 기타 애플리케이션 설정
│   ├── exception
│   │   ├── GlobalExceptionHandler.java  # 전역 예외 처리
│   │   ├── CustomException.java         # 커스텀 예외 클래스
│   │   ├── ErrorCode.java               # 에러 코드 열거형
│   │   └── FieldError.java              # 필드 유효성 검사 에러 DTO
│   ├── entity
│   │   └── BaseAuditEntity.java     # 생성·수정·삭제 일시 및 작성자 공통 엔티티 (소프트 딜리트 지원)
│   ├── audit
│   │   └── AuditorHolder.java       # 생성자/수정자 정보를 ThreadLocal로 관리
│   └── common
│       ├── ApiResponse.java         # 공통 API 응답 포맷
│       └── PageResponse.java        # 페이지네이션 공통 응답 래퍼
│
├── auth                            # 인증 관련 기능 (JWT 발급, 로그인, 로그아웃, 토큰 갱신)
├── user                            # 사용자 관련 기능
├── address                         # 배송지 관련 기능
├── store                           # 가게 관련 기능
├── menu                            # 메뉴 관련 기능
├── order                           # 주문 관련 기능
├── payment                         # 결제 관련 기능
├── review                          # 리뷰 관련 기능
├── area                            # 운영 지역 관련 기능
├── category                        # 카테고리 관련 기능
├── ai                              # AI 요청/응답 로그 관련 기능
│
└── DeliveryOrderPlatformApplication.java   # 애플리케이션 진입점
```
>각 도메인(auth, user, address, store, menu, order, payment, review, area, category, ai)은 동일한 구조로 구성됩니다.
```
{domain}/
├── controller/     # HTTP 요청 처리
├── service/        # 비즈니스 로직
├── repository/     # DB 접근
├── entity/         # JPA 엔티티
└── dto/            # 요청/응답 데이터 객체
```
---

## 레이어별 역할

| 폴더 | 역할 |
|------|------|
| `controller` | HTTP 요청/응답 처리, DTO 변환 |
| `service` | 비즈니스 로직 처리 |
| `repository` | DB 접근 (JPA) |
| `entity` | DB 테이블과 매핑되는 클래스 |
| `dto` | 요청/응답 데이터 전달 객체 |

---

## global 폴더 역할

| 폴더 | 파일 | 역할 |
|------|------|------|
| `config` | `SecurityConfig.java` | Spring Security, JWT 필터 설정 |
| `config` | `RedisConfig.java` | Redis 연결 및 직렬화 설정 |
| `config` | `QueryDslConfig.java` | QueryDSL JPAQueryFactory 빈 설정 |
| `config` | `SwaggerConfig.java` | Swagger UI 및 API 문서 설정 |
| `config` | `JpaAuditingConfig.java` | JPA Auditing 활성화 설정 |
| `config` | `AppConfig.java` | 기타 애플리케이션 공통 설정 |
| `exception` | `GlobalExceptionHandler.java` | 전역 예외 처리 (`@RestControllerAdvice`) |
| `exception` | `CustomException.java` | 프로젝트 공통 커스텀 예외 클래스 |
| `exception` | `ErrorCode.java` | 에러 코드 및 HTTP 상태 열거형 |
| `exception` | `FieldError.java` | 필드 유효성 검사 에러 응답 DTO |
| `entity` | `BaseAuditEntity.java` | 생성·수정·삭제 일시 및 작성자 공통 엔티티, 소프트 딜리트 지원 |
| `audit` | `AuditorHolder.java` | 생성자/수정자 정보를 ThreadLocal로 관리 |
| `common` | `ApiResponse.java` | 공통 응답 형식 래퍼 클래스 |
| `common` | `PageResponse.java` | 페이지네이션 공통 응답 래퍼 클래스 |

---

## 네이밍 규칙

| 항목 | 규칙 | 예시 |
|------|------|------|
| Entity 클래스 | `Entity` 접미사 생략 | `Store.java` (O), `StoreEntity.java` (X) |
| Request DTO | `{도메인}RequestDto` | `StoreRequestDto.java` |
| Response DTO | `{도메인}ResponseDto` | `StoreResponseDto.java` |
| Controller | `{도메인}Controller` | `StoreController.java` |
| Service | `{도메인}Service` | `StoreService.java` |
| Repository | `{도메인}Repository` | `StoreRepository.java` |

> **Entity 접미사를 생략하는 이유**
> `@Entity` 어노테이션이 이미 해당 클래스가 JPA 엔티티임을 명시하고 있기 때문에
> 클래스명에 `Entity`를 중복으로 붙이지 않는 것이 일반적인 관례입니다.

### DTO 분리 예시 (Order 도메인 기준)

| DTO 이름 | 용도 | 포함 필드 예시 |
|----------|------|----------------|
| `OrderCreateRequestDto` | 주문 생성 요청 (Client → Server) | storeId, items[ ], addressId |
| `OrderResponseDto` | 주문 조회 응답 (Server → Client) | orderId, storeName, totalPrice, status, createdAt |


> **왜 하나의 DTO를 재사용하지 않나요?**
> - **요청용**은 클라이언트가 보내는 값만 담으면 됩니다. (createdAt, orderId 같은 건 서버가 생성)
> - **응답용**은 클라이언트에게 보여줄 값만 담으면 됩니다. (내부 FK, 민감 정보 제외)

---

## UUID 사용 이유

> 모든 주요 엔티티의 식별자(ID)는 **UUID**를 사용합니다. (유저 엔티티는 예외)

**왜 UUID를 쓰나요?**

일반적인 자동 증가 숫자 ID(1, 2, 3...)는 다음과 같은 문제가 있습니다:

- URL에서 `GET /orders/3` 처럼 노출되면, 타 사용자의 주문을 순서대로 추측해 접근 시도 가능 (보안 취약)
- 서버가 여러 대로 분산될 때 ID 충돌 발생 가능

UUID는 `550e8400-e29b-41d4-a716-446655440000` 같은 형태로, 전 세계에서 거의 중복이 없는 고유값이라 이 문제를 방지합니다.

**유저 엔티티만 예외인 이유:**
유저는 내부 시스템에서 숫자 ID로 관리하는 것이 성능상 유리하거나, 별도의 보안 처리(JWT 등)가 이미 적용되어 있기 때문입니다.

---

