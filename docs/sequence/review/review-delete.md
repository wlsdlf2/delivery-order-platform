## 리뷰 삭제 API

**Endpoint**: `DELETE /api/v1/reviews/{reviewId}`  
**권한**: CUSTOMER, MASTER  
**설명**: 리뷰 soft delete 처리

```mermaid
sequenceDiagram
    actor Client
    participant Controller
    participant Service
    participant ReviewRepo

    Client->>Controller: DELETE /reviews/{reviewId}
    Note over Controller: @PreAuthorize CUSTOMER or MASTER only

    Controller->>Service: deleteReview(reviewId, user, role)

    Service->>ReviewRepo: findByIdAndDeletedAtIsNull(reviewId)
    ReviewRepo-->>Service: Review

    alt 리뷰 없음
        Service-->>Client: 404 REVIEW_NOT_FOUND
    end

    alt role = CUSTOMER이고 본인 리뷰 아닌 경우
        Service-->>Client: 403 REVIEW_DELETE_FORBIDDEN
    end

    Note over Service: review.softDelete(username)

    Service->>ReviewRepo: flush()

    Service->>ReviewRepo: findAverageRatingByStoreId(storeId)
    ReviewRepo-->>Service: Double

    Note over Service: store.updateAverageRating(newAverageRating)

    Service-->>Controller: void
    Controller-->>Client: 200 OK (ApiResponse)
```