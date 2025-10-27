package com.naveen.reposcoreservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class GithubSearchResponseDto {

	@JsonProperty("total_count")
	private int totalCount;

	@JsonProperty("incomplete_results")
	private boolean incompleteResults;

	private List<RepoItem> items;

}
