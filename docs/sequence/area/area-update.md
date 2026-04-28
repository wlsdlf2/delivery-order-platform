## 운영지역 업데이트

**관련 도메인**: Area, Store  
**권한**: MASTER  
**핵심**: AreaService → StoreRepository 직접 참조 방식으로 순환참조 해결

### 주요 흐름

- 지역명 변경 시, 중복 체크 (400 DUPLICATE_AREA_NAME)
- 비활성화 시, 연관 가게가 있으면 불가 (409 EXIST_LINKED_STORES)


```mermaid
sequenceDiagram
  actor Master as User(MASTER)
  participant Controller as AreaController
  participant Service as AreaService
  participant AreaRepo as AreaRepository
  participant StoreRepo as StoreRepository
  autonumber

  Note over Controller: @PreAuthorize("Master") <br/> 권한 검증

  Master->>Controller: PUT /api/v1/areas/{areaId}
  Controller->>Service: updateArea(areaId, requestDTO)


  rect rgb(240, 240, 240)
    Note right of Service: 검증 단계
    Service->>AreaRepo: 1. Area 존재 확인(findActiveAreaById) <br/> 없으면 404 (AREA_NOT_FOUND)
    Note over AreaRepo: findByIdAndIsActiveTrueAndDeletedAtIsNull
    Service->>AreaRepo: 2. 변경시, 지역명 중복 체크(validateDuplicateName) <br/> 있으면 400 (DUPLICATE_AREA_NAME)
    Note over AreaRepo: existsByNameAndDeletedAtIsNull
    Service->>StoreRepo: 3. 비활성화시, 연관 가게 확인(validateNoActiveStores) <br/> 있으면 409 (EXIST_LINKED_STORES)
    Note over StoreRepo: existsByAreaIdAndDeletedAtIsNull
  end

  rect rgb(219, 234, 254)
    Note left of Service: 비즈니스 로직 수행
    Service->>Service: area.update(...)
  end

  Service-->>Controller: AreaResponseDTO
  Controller-->>Master: 200 OK
```