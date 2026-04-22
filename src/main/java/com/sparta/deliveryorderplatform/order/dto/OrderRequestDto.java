package com.sparta.deliveryorderplatform.order.dto;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderRequestDto {
    private String customerId;
    private UUID addressId;
}
