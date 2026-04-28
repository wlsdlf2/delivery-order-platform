## 결제 삭제 API

**Endpoint**: `DELETE /api/v1/payments/{paymentId}`  
**권한**: MASTER  
**설명**: 결제 soft delete 처리

```mermaid
sequenceDiagram
    actor Client
    participant Controller
    participant Service
    participant PaymentRepo

    Client->>Controller: DELETE /payments/{paymentId}
    Note over Controller: @PreAuthorize MASTER only

    Controller->>Service: deletePayment(paymentId, user)

    Service->>PaymentRepo: findByIdAndDeletedAtIsNull(paymentId)
    PaymentRepo-->>Service: Payment

    alt 결제 없음
        Service-->>Client: 404 PAYMENT_NOT_FOUND
    end

    Note over Service: payment.softDelete(username)

    Service-->>Controller: void
    Controller-->>Client: 200 OK (ApiResponse)
```