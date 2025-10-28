package com.naveen.reposcoreservice.dto;

import org.springframework.stereotype.Component;

@Component
public class ScoredRepoConverter {

	public SimpleScoredRepoDto convertItemToDto(final SimpleScoredRepoItem item, final double score) {
		return SimpleScoredRepoDto.builder()
		                          .fullName(item.getFullName())
		                          .htmlUrl(item.getHtmlUrl())
		                          .description(item.getDescription())
		                          .score(score)
		                          .build();
	}
}