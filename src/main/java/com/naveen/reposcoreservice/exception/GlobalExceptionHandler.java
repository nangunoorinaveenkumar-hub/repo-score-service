package com.naveen.reposcoreservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

	public static final String TIMESTAMP = "timestamp";
	public static final String MESSAGE = "message";
	public static final String STATUS = "status";

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleAllExceptions(Exception ex) {
		final Map<String, Object> body = new HashMap<>();
		body.put(TIMESTAMP, LocalDateTime.now());
		body.put(MESSAGE, ex.getMessage());
		body.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());

		return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(ScoringException.class)
	public ResponseEntity<Object> handleScoringException(ScoringException ex) {
		final Map<String, Object> body = new HashMap<>();
		body.put(TIMESTAMP, LocalDateTime.now());
		body.put(MESSAGE, ex.getMessage());
		body.put(STATUS, HttpStatus.BAD_REQUEST.value());

		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(GithubClientException.class)
	public ResponseEntity<Object> handleGithubClientException(GithubClientException ex) {
		final Map<String, Object> body = new HashMap<>();
		body.put(TIMESTAMP, LocalDateTime.now());
		body.put(MESSAGE, ex.getMessage());
		body.put(STATUS, HttpStatus.BAD_GATEWAY.value());

		return new ResponseEntity<>(body, HttpStatus.BAD_GATEWAY);
	}

}