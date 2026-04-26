package com.sparta.deliveryorderplatform.review.dto.request;

import lombok.Getter;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Getter
public class SearchReviewCondition {

    private UUID storeId;

    private Integer rating;
}
