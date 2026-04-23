package com.sparta.deliveryorderplatform.order.entity;

public enum OrderStatus {
     CANCLE         // 주문취소 - CUSTOMER
    ,PENDING        // 주문요청 - CUSTOMER
    ,ACCEPTED       // 주문수락 - OWNER
    ,COOKING        // 조리완료 - OWNER
    ,DELIVERING     // 배송수령 - OWNER
    ,DELIVERED      // 배송완료 - OWNER
    ,COMPLETED      // 주문완료 - OWNER
}
