package com.sparta.deliveryorderplatform.order.entity;

public enum OrderStatus {
    CANCEL, PENDING, ACCEPTED, COOKING, DELIVERING, DELIVERED, COMPLETED;

    // OWNER가 변경 가능한 다음 상태를 반환하는 메서드
    public OrderStatus getNextStatus() {
        return switch (this) {
            case PENDING -> ACCEPTED;    // 주문요청 -> 주문수락
            case ACCEPTED -> COOKING;    // 주문수락 -> 조리중(완료)
            case COOKING -> DELIVERING;  // 조리완료 -> 배송중
            case DELIVERING -> DELIVERED;// 배송중 -> 배송완료
            case DELIVERED -> COMPLETED; // 배송완료 -> 주문완료
            default -> this;             // CANCLE 이나 COMPLETED 상태에선 변경 불가
        };
    }
}
