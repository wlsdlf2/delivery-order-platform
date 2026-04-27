package com.sparta.deliveryorderplatform.store.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StoreVisibilityRequestDTO {
    @NotNull(message = "숨김 여부는 필수입니다.")
    private Boolean isHidden;
}
