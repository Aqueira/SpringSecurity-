package com.spring.security.exception;

import com.spring.security.exception.exceptions.InvalidationFailed;
import com.spring.security.exception.response.ApiError;
import com.spring.security.exception.exceptions.NotFoundException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiError> handleNotFoundException(NotFoundException e) {
		ApiError error = new ApiError(HttpStatus.NOT_FOUND, "Resource not found!", e.getMessage());
		return ResponseEntity.status(error.status()).body(error);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiError> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
		ApiError error = new ApiError(HttpStatus.CONFLICT, "Duplicate entry!", e.getMessage());
		return ResponseEntity.status(error.status()).body(error);
	}

	@ExceptionHandler(InvalidationFailed.class)
	public ResponseEntity<ApiError> handleInvalidationException(InvalidationFailed e) {
		ApiError error = new ApiError(HttpStatus.CONFLICT, "Invalid entry!", e.getMessage());
		return ResponseEntity.status(error.status()).body(error);
	}
}
