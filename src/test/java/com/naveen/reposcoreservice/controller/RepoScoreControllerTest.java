package com.naveen.reposcoreservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.naveen.reposcoreservice.dto.GithubSearchResponseItem;
import com.naveen.reposcoreservice.dto.ScoredRepoItem;
import com.naveen.reposcoreservice.dto.SimpleScoredRepoDto;
import com.naveen.reposcoreservice.service.GithubSearchClient;
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
	private GithubSearchClient githubSearchClient;

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
		final ScoredRepoItem scoredRepoItem = ScoredRepoItem.builder().fullName("repo1").build();
		final ScoredRepoItem scoredRepoItem1 = ScoredRepoItem.builder().fullName("repo2").build();
		final GithubSearchResponseItem githubSearchResponseItem =
			GithubSearchResponseItem.builder().items(List.of(scoredRepoItem, scoredRepoItem1)).build();

		final List<SimpleScoredRepoDto> scoredList = List.of(
			SimpleScoredRepoDto.builder().fullName("repo1").score(100).build(),
			SimpleScoredRepoDto.builder().fullName("repo2").score(200).build()
		);

		when(githubSearchClient.searchRepositories(language, createdAfter, "stars", "desc", page, perPage))
			.thenReturn(Mono.just(githubSearchResponseItem));

		when(scoringService.score(githubSearchResponseItem)).thenReturn(scoredList);


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

		verify(githubSearchClient).searchRepositories(language, createdAfter, "stars", "desc", page, perPage);
		verify(scoringService).score(githubSearchResponseItem);
		verifyNoMoreInteractions(githubSearchClient, scoringService);
	}
}
