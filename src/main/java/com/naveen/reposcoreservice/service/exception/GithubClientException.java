package com.naveen.reposcoreservice.service.exception;

public class GithubClientException extends RuntimeException {
	public GithubClientException(final String message, final Throwable cause) {
		super(message, cause);
	}

}