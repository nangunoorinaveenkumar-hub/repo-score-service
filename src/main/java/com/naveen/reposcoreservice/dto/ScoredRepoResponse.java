package com.naveen.reposcoreservice.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import lombok.Generated;

@Data
@Builder
@Generated
public class ScoredRepoResponse {
	private int totalCount;
	private boolean incompleteResults;
	private List<SimpleScoredRepoDto> items;
}