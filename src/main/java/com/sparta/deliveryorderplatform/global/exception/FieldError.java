package com.sparta.deliveryorderplatform.global.exception;

public record FieldError(
	String field,
	String message
) {}
