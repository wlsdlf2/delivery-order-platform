# API 설계 가이드 (API Design Guide)

## RESTful API 설계 원칙

> **RESTful API란?**
> HTTP의 규칙을 잘 지켜서, URL과 HTTP 메서드(GET/POST/PUT/DELETE)만 봐도
> 어떤 동작을 하는지 직관적으로 알 수 있도록 설계된 API 방식입니다.

### 기본 규칙

| 원칙 | 설명 | 예시 |
|------|------|------|
| 명사 사용 | URL에 동사 대신 명사(리소스) 사용 | `/orders` (O), `/getOrder` (X) |
| 복수형 사용 | 리소스는 복수형으로 | `/stores`, `/menus`, `/orders` |
| 계층 구조 표현 | 소속 관계는 경로로 표현 | `/stores/{storeId}/menus` |
| HTTP 메서드로 행위 구분 | URL이 아닌 메서드로 동작 구분 | 아래 표 참고 |

### HTTP 메서드 사용 규칙

| 메서드 | 용도 | 예시 |
|--------|------|------|
| `GET` | 조회 | `GET /orders/{orderId}` |
| `POST` | 생성 | `POST /orders` |
| `PUT` | 전체 수정 | `PUT /menus/{menuId}` |
| `PATCH` | 일부 수정 | `PATCH /orders/{orderId}/status` |
| `DELETE` | 삭제 | `DELETE /stores/{storeId}` |

---

## 공통 응답 형식

모든 API는 아래 형식으로 응답합니다.

**성공 시:**
```json
{
  "status": 200,
  "message": "성공",
  "data": { ... }
}
```

**실패 시:**
```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "주문 취소는 주문 후 5분 이내에만 가능합니다."
}
```

---

## API 문서 작성 기준

> 프론트엔드 개발자가 API 문서만 보고 개발할 수 있어야 합니다.

각 API에 반드시 포함해야 할 항목:

- [ ] **URL** 및 **HTTP 메서드**
- [ ] **요청 헤더** (Authorization 필요 여부)
- [ ] **요청 파라미터** (Path Variable, Query Param, Request Body)
- [ ] **응답 형식** (성공/실패 각각의 예시)
- [ ] **필요 권한** (CUSTOMER / OWNER / MANAGER / MASTER)
- [ ] **에러 코드 및 메시지**

---

## 설계 시 고민 사항

- **확장성**: 현재 모놀리식이지만 도메인별로 패키지를 명확히 분리해두면 추후 MSA 전환이 용이함
- **엔티티 관계 설정**: 연관관계 방향(단방향/양방향)과 fetch 전략(LAZY/EAGER)을 신중히 결정
- **페이지네이션**: 목록 조회 API는 기본적으로 페이징 처리 적용 권장 (`?page=0&size=10`)
