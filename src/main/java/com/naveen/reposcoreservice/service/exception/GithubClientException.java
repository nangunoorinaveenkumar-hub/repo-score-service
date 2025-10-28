package com.naveen.reposcoreservice.service.exception;

public class GithubClientException extends RuntimeException {
	public GithubClientException(String message, Throwable cause) {
		super(message, cause);
	}

}