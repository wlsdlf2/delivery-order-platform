## 운영지역 삭제

**관련 도메인**: Area, Store  
**권한**: MASTER  
**핵심**: AreaService → StoreRepository 직접 참조 방식으로 순환참조 해결

### 주요 흐름
- 활성 가게가 있으면 삭제 불가 (409 EXIST_LINKED_STORES)
- 수정(비활성화) 시에도 동일한 validateNoActiveStores() 적용
  단, 활성 → 비활성 변경될 때만 조건부 실행

```mermaid
sequenceDiagram
    autonumber
    participant Client
    participant AreaController
    participant AreaService
    participant StoreRepository
    participant AreaRepository

    Note over AreaController: MASTER 권한만 접근 가능

    Client->>AreaController: DELETE /api/v1/areas/{areaId}
    AreaController->>AreaService: deleteArea(areaId, user)

    AreaService->>AreaRepository: findByIdAndDeletedAtIsNull(areaId)
    AreaRepository-->>AreaService: Area
    Note right of AreaRepository: 없으면 404 (AREA_NOT_FOUND)

    AreaService->>StoreRepository: existsByAreaIdAndDeletedAtIsNull(areaId)

    alt 활성 가게 있음
        StoreRepository-->>AreaService: true
        AreaService-->>AreaController: 예외 (EXIST_LINKED_STORES)
        AreaController-->>Client: 409 (EXIST_LINKED_STORES)
    else 활성 가게 없음
        StoreRepository-->>AreaService: false
        Note over AreaService: area.delete() → softDelete + isActive=false
        AreaService-->>AreaController: AreaResponseDTO
        AreaController-->>Client: 200 OK
    end
```