package com.naveen.reposcoreservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;

@Data
@Builder
@Generated
public class GithubSearchResponseItem {

	@JsonProperty("total_count")
	private int totalCount;

	@JsonProperty("incomplete_results")
	private boolean incompleteResults;

	private List<ScoredRepoItem> items;
}