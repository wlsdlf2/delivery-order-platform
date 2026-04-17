# 공통 처리 요소 (Common Components)

## 데이터 유효성 검사

- **Spring Validation** 사용 (`@Valid`, `@NotNull`, `@Size` 등)
- 모든 Request DTO에 유효성 검사 어노테이션 적용
- 잘못된 입력값은 Controller 진입 전에 차단

```java
// 예시
public class OrderRequestDto {
    @NotNull(message = "가게 ID는 필수입니다.")
    private UUID storeId;

    @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다.")
    private List<OrderItemDto> items;
}
```
---

## 예외 처리

- **글로벌 예외 처리**: `@RestControllerAdvice` + `@ExceptionHandler` 사용
- 모든 예외는 중앙에서 일관된 형식으로 응답

```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "주문 취소는 5분 이내에만 가능합니다."
}
```