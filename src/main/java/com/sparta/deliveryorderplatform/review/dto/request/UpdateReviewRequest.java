package com.sparta.deliveryorderplatform.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateReviewRequest {

    @NotNull
    @Min(1) @Max(5)
    private Integer rating;

    private String content;
}
