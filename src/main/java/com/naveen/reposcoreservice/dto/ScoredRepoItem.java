package com.naveen.reposcoreservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScoredRepoItem {
	private RepoItem repoItem;
	private double rawScore;
}
