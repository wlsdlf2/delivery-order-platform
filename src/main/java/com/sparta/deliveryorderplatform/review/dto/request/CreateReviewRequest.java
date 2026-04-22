package com.sparta.deliveryorderplatform.review.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class CreateReviewRequest {

    @NotNull
    private Integer rating;

    @NotNull
    private UUID storeId;

    private String content;
}
