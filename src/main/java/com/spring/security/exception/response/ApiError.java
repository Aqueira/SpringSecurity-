package com.spring.security.exception.response;




import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ApiError(HttpStatus status, String message, String details, LocalDateTime timestamp) {
	public ApiError(HttpStatus status, String message, String details) {
		this(status, message, details, LocalDateTime.now());
	}
}
