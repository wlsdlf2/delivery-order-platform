package com.sparta.deliveryorderplatform.global.common;

import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {

	private List<T> content;
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;
	private String sort;

	public static <T> PageResponse<T> of(Page<T> page) {
		return new PageResponse<>(
			page.getContent(),
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages(),
			page.getSort().toString()
		);
	}
}
