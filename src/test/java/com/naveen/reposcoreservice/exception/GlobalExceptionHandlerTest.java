package com.naveen.reposcoreservice.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.naveen.reposcoreservice.service.exception.GithubClientException;
import com.naveen.reposcoreservice.service.exception.GlobalExceptionHandler;
import com.naveen.reposcoreservice.service.exception.ScoringException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

	@Test
	void handleAllExceptions_shouldReturnInternalServerErrorResponse() {
		final Exception exception = new Exception("Something went wrong");

		final ResponseEntity<Object> response = globalExceptionHandler.handleAllExceptions(exception);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

		@SuppressWarnings("unchecked")
		final Map<String, Object> body = (Map<String, Object>) response.getBody();

		assertThat(body)
			.containsKeys("timestamp", "message", "status")
			.containsEntry("message", "Something went wrong")
			.containsEntry("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
	}

	@Test
	void handleScoringException_shouldReturnBadRequestResponse() {
		final ScoringException scoringException =
			new ScoringException("Invalid scoring input", new IllegalArgumentException("invalid data"));

		final ResponseEntity<Object> response = globalExceptionHandler.handleScoringException(scoringException);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		@SuppressWarnings("unchecked")
		final Map<String, Object> body = (Map<String, Object>) response.getBody();

		assertThat(body)
			.containsKeys("timestamp", "message", "status")
			.containsEntry("message", "Invalid scoring input")
			.containsEntry("status", HttpStatus.BAD_REQUEST.value());
	}

	@Test
	void handleGithubClientException_shouldReturnBadGatewayResponse() {
		final GithubClientException githubClientException =
			new GithubClientException("GitHub API unreachable", new RuntimeException("timeout"));

		final ResponseEntity<Object> response = globalExceptionHandler.handleGithubClientException(githubClientException);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);

		@SuppressWarnings("unchecked")
		final Map<String, Object> body = (Map<String, Object>) response.getBody();

		assertThat(body)
			.containsKeys("timestamp", "message", "status")
			.containsEntry("message", "GitHub API unreachable")
			.containsEntry("status", HttpStatus.BAD_GATEWAY.value());
	}
}