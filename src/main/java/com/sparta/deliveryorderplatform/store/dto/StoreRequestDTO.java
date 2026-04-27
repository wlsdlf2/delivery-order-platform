package com.sparta.deliveryorderplatform.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreRequestDTO {

    @NotBlank(message = "가게 이름은 필수입니다.")
    @Size(max = 100, message = "가게 이름은 100자를 초과할 수 없습니다.")
    private String name;

    @NotBlank(message = "가게 주소는 필수입니다.")
    @Size(max = 255, message = "가게 주소는 255자를 초과할 수 없습니다.")
    private String address;

    @Size(max = 20, message = "가게 전화번호는 20자를 초과할 수 없습니다.")
    private String phone;

    @NotNull(message = "카테고리 ID는 필수입니다.")
    private UUID categoryId;

    @NotNull(message = "운영지역 ID는 필수입니다.")
    private UUID areaId;

    private Boolean isHidden;
}
