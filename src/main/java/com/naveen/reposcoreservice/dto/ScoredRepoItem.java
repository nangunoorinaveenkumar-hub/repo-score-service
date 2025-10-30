package com.naveen.reposcoreservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;

@Data
@Builder
@Generated
public class ScoredRepoItem {
	@JsonProperty("id")
	private long id;

	@JsonProperty("full_name")
	private String fullName;

	@JsonProperty("html_url")
	private String htmlUrl;

	private String description;

	@JsonProperty("stargazers_count")
	private int stargazersCount;

	@JsonProperty("forks_count")
	private int forksCount;

	@JsonProperty("pushed_at")
	private String pushedAt;
}