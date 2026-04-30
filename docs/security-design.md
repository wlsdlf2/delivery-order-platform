# 보안 설계 (Security Design)

## 인증 방식: JWT (JSON Web Token)

### JWT란?

> 로그인 성공 시 서버가 발급하는 **암호화된 토큰**입니다.
> 클라이언트는 이후 모든 요청 헤더에 이 토큰을 담아 보내고, 서버는 토큰을 검증해 사용자를 식별합니다.
> 서버가 세션을 저장하지 않아도 되므로 **Stateless(무상태)** 구조를 유지할 수 있습니다.

### 토큰 전달 방식

```
HTTP Header:
Authorization: Bearer {JWT_ACCESS_TOKEN}
```

---

## 비밀번호 암호화

- 알고리즘: **BCrypt 해시**
- 비밀번호는 절대 평문으로 저장하지 않으며, DB에는 해시값만 저장됩니다.
- 로그인 시 입력된 비밀번호를 해시화하여 저장된 값과 비교합니다.

---

## 매 요청 권한 재검증 (중요)

### 왜 필요한가?

JWT는 발급 후 **만료 전까지는 유효**합니다. 그런데 토큰이 살아있는 동안 사용자의 권한이 바뀌면 문제가 생깁니다.

**예시 1 - 권한 다운그레이드:**
```
가게 인수인계로 OWNER → CUSTOMER 로 권한 변경
But, 기존 발급된 토큰에는 아직 OWNER 권한이 담겨있음
→ 만료 전까지 해당 토큰으로 가게를 컨트롤할 수 있는 보안 취약점 발생
```

**예시 2 - 계정 삭제:**
```
MASTER가 MANAGER 계정을 삭제 처리
But, 기존 발급된 토큰은 아직 유효
→ 삭제된 계정이 서비스를 계속 컨트롤할 수 있는 보안 취약점 발생
```

### 해결 방법

요청마다 JWT payload의 `role` 값과 **DB의 현재 role** 을 비교하여 검증합니다.

```
요청 수신
   ↓
JWT 파싱 → payload에서 userId, role 추출
   ↓
DB에서 현재 userId의 role 조회
   ↓
두 값 비교 → 불일치 시 401/403 반환
```

### 성능 고민: DB 접근 최소화

매 요청마다 DB를 조회하면 부하가 커질 수 있습니다.

**해결 방안: Redis 유저 캐싱 (구현 완료)**
> Redis는 메모리 기반 저장소로, DB보다 훨씬 빠르게 데이터를 읽을 수 있습니다.
> `UserCacheService`를 통해 유저 정보를 Redis에 캐싱해두고, 매 요청마다 먼저 Redis를 조회합니다.
> 캐시 미스 시에만 PostgreSQL을 조회하며, 유저 정보 변경(프로필 수정, 권한 변경, 탈퇴) 시 캐시를 즉시 무효화합니다.

---

---

## 토큰 블랙리스트 (로그아웃 보안)

JWT는 Stateless 특성상 서버에서 발급된 토큰을 강제로 무효화할 수 없습니다. 로그아웃 후에도 만료 전까지 해당 토큰으로 API를 호출할 수 있는 보안 문제를 해결하기 위해 Redis 블랙리스트를 도입했습니다.

- 로그아웃 시 해당 Access Token을 Redis 블랙리스트에 등록
- TTL을 토큰의 **남은 유효시간**과 동일하게 설정 → 토큰 만료 시 블랙리스트에서도 자동 삭제
- 모든 요청에서 `JwtAuthenticationFilter`가 블랙리스트 등록 여부를 확인하여 등록된 토큰은 `401` 반환

```
로그아웃 요청
   ↓
Access Token → Redis 블랙리스트 등록 (TTL = 남은 유효시간)
Refresh Token → Redis에서 삭제
   ↓
이후 해당 토큰으로 요청 시 → 401 INVALID_TOKEN 반환
```

---

## 리프레시 토큰

Access Token의 유효시간을 짧게 유지하면서도 사용자가 자주 재로그인하지 않도록 Refresh Token을 도입했습니다.

| 항목 | Access Token | Refresh Token |
|------|-------------|---------------|
| 유효시간 | 짧음 (분 단위) | 김 (일 단위) |
| 저장 위치 | 클라이언트 | 클라이언트 + Redis |
| 용도 | API 인증 | Access Token 재발급 |
| 전달 헤더 | `Authorization: Bearer {token}` | `X-Refresh-Token: {token}` |

- 로그인 시 두 토큰 모두 발급, Refresh Token은 Redis에 저장
- Access Token 만료 시 `/api/v1/auth/signup/refresh`로 Refresh Token을 전달해 새 Access Token 발급
- 로그아웃 시 Redis의 Refresh Token 삭제 → 재발급 불가

---

## 로그인 Rate Limiting

무차별 대입 공격(Brute Force)을 방어하기 위해 IP 기반 로그인 시도 횟수를 제한합니다.

- IP당 **60초 이내 10회 초과** 시 `429 RATE_LIMIT_EXCEEDED` 반환
- Redis에 `ratelimit:login:{IP}` 키로 시도 횟수를 저장하며, 60초 후 자동 초기화

---

## 민감 정보 관리

- API Key, DB 비밀번호 등 민감 정보는 **환경 변수**로 관리
- `.env` 파일 또는 OS 환경 변수를 통해 주입
- `.env` 파일은 소스코드에 커밋하지 않음 (`.gitignore` 등록 필수)