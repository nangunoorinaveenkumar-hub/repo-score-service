package com.naveen.reposcoreservice.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GithubSearchRequest {
	String language;
	String createdAfter;
	String sort;
	String order;
	int page;
	int perPage;
}