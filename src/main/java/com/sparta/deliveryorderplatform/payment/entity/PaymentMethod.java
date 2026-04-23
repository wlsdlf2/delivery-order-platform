package com.sparta.deliveryorderplatform.payment.entity;

public enum PaymentMethod {
    CARD("카드");

    private final String method;

    private PaymentMethod(String method) {
        this.method = method;
    }
}
