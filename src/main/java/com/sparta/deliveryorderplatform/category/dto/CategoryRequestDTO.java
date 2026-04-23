package com.sparta.deliveryorderplatform.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE) // 외부에서 기본 생성자 호출 방지
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더 패턴 사용 유도
@Builder
public class CategoryRequestDTO {

    @NotBlank(message = "카테고리 이름은 필수입니다.")
    @Size(max = 50, message = "카테고리 이름은 50자를 초과할 수 없습니다.")
    private String name;

}
