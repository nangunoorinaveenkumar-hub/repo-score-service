package com.naveen.reposcoreservice.dto;

import org.springframework.stereotype.Component;

@Component
public class ScoredRepoConverter {

	public ScoredRepoDto convert(final RepoItem item, final double score) {
		return ScoredRepoDto.builder()
							.fullName(item.getFullName())
							.htmlUrl(item.getHtmlUrl())
							.description(item.getDescription())
							.score(score)
			                .build();
	}
}
