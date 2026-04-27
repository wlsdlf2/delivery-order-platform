## 가게 삭제

**관련 도메인**: Store, Order, Menu  
**권한**: OWNER, MASTER

### 주요 흐름

- MASTER는 모든 가게 삭제 가능
- OWNER는 본인 가게만 삭제 가능 (타 가게 접근 시 403)
- 진행중인 주문이 있으면 삭제 불가 (409 EXIST_ACTIVE_ORDERS)
- 가게 삭제 전 해당 가게 메뉴 전체 softDelete 처리
- 가게 softDelete + isHidden=true 처리

```mermaid
sequenceDiagram
    autonumber
    actor User as User (OWNER/MASTER)
    participant Global as Spring Security
    participant Ctrl as StoreController
    participant Svc as StoreService
    participant OrderSvc as OrderService
    participant MenuRepo as MenuRepository
    participant Repo as StoreRepository

    User->>Global: PATCH /api/v1/stores/{storeId} (with JWT)
    Note over Global: @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")<br/>권한 검증 및 UserDetails 생성
    Global->>Ctrl: deleteStore(storeId, userDetails)
    Ctrl->>Svc: deleteStore(storeId, user)

    rect rgb(240, 240, 240)
        Note right of Svc: 유효성 검증 단계
        Svc->>Repo: findByIdAndDeletedAtIsNull(storeId)
        Repo-->>Svc: Store
        Note left of Repo: 없으면 404 (STORE_NOT_FOUND)

        Note over Svc: validateStoreAccess 실행
        alt Role == MASTER
            Svc->>Svc: 권한 통과
        else Role == OWNER
            Svc->>Svc: 본인 가게 여부 확인
            Note over Svc: 실패 시 403 (UNAUTHORIZED_ACCESS)
        end

        Svc->>OrderSvc: hasActiveOrders(storeId)
        OrderSvc-->>Svc: TRUE or FALSE
        Note left of OrderSvc: 진행 중인 주문 있으면 409 (EXIST_ACTIVE_ORDERS)
    end

    rect rgb(219, 234, 254)
        Note right of Svc: 비즈니스 로직 수행
        Svc->>MenuRepo: softDelete(storeId, deletedAt, username)
        MenuRepo-->>Svc: 완료
        Note left of MenuRepo: 해당 가게 메뉴 전체 삭제
        Note over Svc: store.delete() → softDelete + isHidden=true
    end


    Svc-->>Ctrl: StoreResponseDTO
    Ctrl-->>User: 200 OK
```