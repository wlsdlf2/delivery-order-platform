package com.sparta.deliveryorderplatform.area.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AreaRequestDTO {

    @NotBlank(message = "운영 지역명은 필수입니다.")
    @Size(max = 100, message = "운영 지역명은 100자를 초과할 수 없습니다.")
    private String name;

    @NotBlank(message = "시/도는 필수입니다.")
    @Size(max = 50, message = "시/도는 50자를 초과할 수 없습니다.")
    private String city;

    @NotBlank(message = "구/군은 필수입니다.")
    @Size(max = 50, message = "구/군은 50자를 초과할 수 없습니다.")
    private String district;

}
