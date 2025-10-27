package com.naveen.reposcoreservice.dto;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

public class ScoredRepoConverterTest {

	private final ScoredRepoConverter converter = new ScoredRepoConverter();

	@Test
	void convert_should_return_dto_when_repoItem_is_not_null() {
		final String fullName = "test/repo";
		final String htmlUrl = "https://github.com/test/repo";
		final String description = "Test repo";
		final double score = 99.5;
		final RepoItem repoItem = RepoItem.builder()
                                          .fullName(fullName)
                                          .htmlUrl(htmlUrl)
                                          .description(description)
                                          .build();

		final ScoredRepoDto scoredRepoDto = converter.convert(repoItem, score);

		assertThat(scoredRepoDto).isNotNull();
		assertThat(scoredRepoDto.getFullName()).isEqualTo(fullName);
		assertThat(scoredRepoDto.getHtmlUrl()).isEqualTo(htmlUrl);
		assertThat(scoredRepoDto.getDescription()).isEqualTo(description);
		assertThat(scoredRepoDto.getScore()).isEqualTo(score);
	}
}
