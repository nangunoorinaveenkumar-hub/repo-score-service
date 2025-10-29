package com.naveen.reposcoreservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.naveen.reposcoreservice.dto.SimpleScoredRepoDto;
import com.naveen.reposcoreservice.service.ScoringService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class RepoScoreControllerTest {

	@Mock
	private ScoringService scoringService;

	@InjectMocks
	private RepoScoreController repoScoreController;

	@Test
	void getScoredRepos_shouldReturnScoredRepositories() {
		final String language = "java";
		final String createdAfter = "2023-01-01";
		final int page = 1;
		final int perPage = 5;
		final List<SimpleScoredRepoDto> scoredList = List.of(
			SimpleScoredRepoDto.builder().fullName("repo1").score(100).build(),
			SimpleScoredRepoDto.builder().fullName("repo2").score(200).build()
		);

		when(scoringService.getScoredRepos(language, createdAfter, page, perPage))
			.thenReturn(Mono.just(scoredList));


		final List<SimpleScoredRepoDto> result = repoScoreController
			.getScoredRepos(language, createdAfter, page, perPage)
			.block();

		assertThat(result)
			.isNotNull()
			.hasSize(2)
			.extracting(SimpleScoredRepoDto::getFullName)
			.containsExactly("repo1", "repo2");

		assertThat(result)
			.extracting(SimpleScoredRepoDto::getScore)
			.containsExactly(100.00, 200.00);

		verify(scoringService).getScoredRepos(language, createdAfter, page, perPage);
		verifyNoMoreInteractions(scoringService);
	}
}