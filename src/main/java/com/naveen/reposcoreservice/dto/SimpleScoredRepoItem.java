package com.naveen.reposcoreservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimpleScoredRepoItem {
	private String fullName;
	private String htmlUrl;
	private String description;
	private double rawScore;
}