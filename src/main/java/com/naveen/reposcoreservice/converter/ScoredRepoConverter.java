package com.naveen.reposcoreservice.converter;

import com.naveen.reposcoreservice.dto.SimpleScoredRepoDto;
import com.naveen.reposcoreservice.dto.SimpleScoredRepoItem;
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