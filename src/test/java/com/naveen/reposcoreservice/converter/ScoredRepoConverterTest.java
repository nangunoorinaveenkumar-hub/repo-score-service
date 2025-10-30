package com.naveen.reposcoreservice.converter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.naveen.reposcoreservice.dto.SimpleScoredRepoDto;
import com.naveen.reposcoreservice.dto.SimpleScoredRepoItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScoredRepoConverterTest {

    @InjectMocks
	private ScoredRepoConverter scoredRepoConverter;

	@Test
	void convertItemToDto_should_return_dto_when_repoItem_is_not_null() {
		final String fullName = "test/repo";
		final String htmlUrl = "https://github.com/test/repo";
		final String description = "Test repo";
		final double score = 99.5;
		final SimpleScoredRepoItem repoItem = SimpleScoredRepoItem.builder()
		                                                          .fullName(fullName)
		                                                          .htmlUrl(htmlUrl)
		                                                          .description(description)
		                                                          .build();

		final SimpleScoredRepoDto scoredRepoDto = scoredRepoConverter.convertItemToDto(repoItem, score);

		assertThat(scoredRepoDto).isNotNull();
		assertThat(scoredRepoDto.getFullName()).isEqualTo(fullName);
		assertThat(scoredRepoDto.getHtmlUrl()).isEqualTo(htmlUrl);
		assertThat(scoredRepoDto.getDescription()).isEqualTo(description);
		assertThat(scoredRepoDto.getScore()).isEqualTo(score);
	}
}