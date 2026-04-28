## 가게 숨김 여부 변경

**관련 도메인**: Store, Order, Menu  
**권한**: OWNER, MASTER

### 주요 흐름

- MASTER는 모든 가게 숨김 여부 변경 가능
- OWNER는 본인 가게만 변경 가능 (타 가게 접근 시 403)
- 숨김 처리 시 진행중인 주문이 있으면 변경 불가 (409 EXIST_ACTIVE_ORDERS)
- 숨김 처리 시 해당 가게 메뉴 전체 숨김 처리
- 노출 처리 시 해당 가게 메뉴 전체 노출 처리

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

    User->>Global: PATCH /api/v1/stores/{storeId}/hide (with JWT)
    Note over Global: @PreAuthorize("hasAnyRole('OWNER', 'MASTER')") <br/> 권한 검증 및 UserDetails 생성
    Global->>Ctrl: updateVisibility(storeId, requestDTO, userDetails)
    Ctrl->>Svc: updateVisibility(storeId, isHidden, user)

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
    end

    rect rgb(219, 234, 254)
        Note right of Svc: 비즈니스 로직 수행
        alt 숨김 : isHidden == true
            Svc->>OrderSvc: hasActiveOrders(storeId)
            OrderSvc-->>Svc: TRUE/FALSE
            Note left of OrderSvc: 진행중 주문 있으면 409 (EXIST_ACTIVE_ORDERS)
            Svc->>MenuRepo: updateIsHiddenByStoreId(storeId, true)
            Note left of MenuRepo: 해당 가게 메뉴 전체 숨김 처리
            MenuRepo-->>Svc: 완료
        else 노출 : isHidden == false
            Svc->>MenuRepo: updateIsHiddenByStoreId(storeId, false)
            Note left of MenuRepo: 해당 가게 메뉴 전체 노출 처리
            MenuRepo-->>Svc: 완료
        end
        Note over Svc: store.updateVisibility(isHidden) <br/> 가게 isHidden 상태 변경
    end

    Svc-->>Ctrl: StoreResponseDTO
    Ctrl-->>User: 200 OK

```