package com.sparta.deliveryorderplatform.global.exception;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice; // 추가

import com.sparta.deliveryorderplatform.global.common.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException e) {
		ErrorCode errorCode = e.getErrorCode();
		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.fail(errorCode));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException e) {

		List<FieldError> errors = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> new FieldError(
				error.getField(),
				error.getDefaultMessage()
			))
			.toList();

		ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;

		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.fail(errorCode, errors));
	}
}