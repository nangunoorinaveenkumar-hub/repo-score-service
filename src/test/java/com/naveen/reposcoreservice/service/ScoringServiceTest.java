package com.naveen.reposcoreservice.service;

import com.naveen.reposcoreservice.converter.ScoredRepoConverter;
import com.naveen.reposcoreservice.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoringServiceTest {

	@Mock
	private GithubSearchClient githubSearchClient;

	@Mock
	private ScoreCalculatorService scoreCalculatorService;

	@Mock
	private ScoredRepoConverter scoredRepoConverter;

	@InjectMocks
	private ScoringService scoringService;

	private ScoredRepoItem scoredRepoItem;
	private ScoredRepoItem scoredRepoItem1;
	private GithubSearchResponseItem githubSearchResponseItem;

	@BeforeEach
	void setUp() {
		scoredRepoItem = ScoredRepoItem.builder()
		                               .fullName("naveen/repo1")
		                               .htmlUrl("http://github.com/naveen/repo1")
		                               .description("Test repo 1")
		                               .build();

		scoredRepoItem1 = ScoredRepoItem.builder()
		                                .fullName("naveen/repo2")
		                                .htmlUrl("http://github.com/naveen/repo2")
		                                .description("Test repo 2")
		                                .build();

		githubSearchResponseItem = GithubSearchResponseItem.builder().items(List.of(scoredRepoItem, scoredRepoItem1)).build();
	}

	@Test
	void score_shouldReturnSortedScoredRepos() {
		when(scoreCalculatorService.calculateRepositoryRawScore(scoredRepoItem)).thenReturn(10.0);
		when(scoreCalculatorService.calculateRepositoryRawScore(scoredRepoItem1)).thenReturn(5.0);

		when(scoreCalculatorService.normalizeScore(any(SimpleScoredRepoItem.class), any(Double.class)))
			.thenAnswer(invocation -> {
				SimpleScoredRepoItem item = invocation.getArgument(0);
				double max = invocation.getArgument(1);
				return item.getRawScore() / max;
			});

		when(scoredRepoConverter.convertItemToDto(any(SimpleScoredRepoItem.class), any(Double.class)))
			.thenAnswer(invocation -> {
				SimpleScoredRepoItem item = invocation.getArgument(0);
				double normalized = invocation.getArgument(1);
				return SimpleScoredRepoDto.builder()
				                          .fullName(item.getFullName())
				                          .score(normalized)
				                          .build();
			});

		final ScoredRepoResponse result = scoringService.score(githubSearchResponseItem);

		assertThat(result.getItems())
			.isNotNull()
			.hasSize(2);

		assertThat(result.getItems().get(0).getFullName()).isEqualTo("naveen/repo1");
		assertThat(result.getItems().get(0).getScore()).isEqualTo(1.0);
		assertThat(result.getItems().get(1).getFullName()).isEqualTo("naveen/repo2");
		assertThat(result.getItems().get(1).getScore()).isEqualTo(0.5);
	}

	@Test
	void score_shouldReturnEmptyResponse_whenItemsAreNull() {
		final GithubSearchResponseItem ghSearchResponseItem = GithubSearchResponseItem.builder().items(null).build();

		final ScoredRepoResponse result = scoringService.score(ghSearchResponseItem);

		assertThat(result.getItems())
			.isNotNull()
			.isEmpty();

		assertThat(result.getTotalCount()).isZero();
		assertThat(result.isIncompleteResults()).isFalse();
	}

	@Test
	void score_shouldReturnEmptyResponse_whenItemsAreEmpty() {
		final GithubSearchResponseItem ghSearchResponseItem = GithubSearchResponseItem.builder().items(List.of()).build();

		final ScoredRepoResponse result = scoringService.score(ghSearchResponseItem);

		assertThat(result.getItems())
			.isNotNull()
			.isEmpty();

		assertThat(result.getTotalCount()).isZero();
		assertThat(result.isIncompleteResults()).isFalse();
	}

	@Test
	void getScoredRepos_shouldInvokeGithubClientAndReturnMonoOfResponse() {
		when(githubSearchClient.searchRepositories(any(GithubSearchRequest.class)))
			.thenReturn(Mono.just(githubSearchResponseItem));

		when(scoreCalculatorService.calculateRepositoryRawScore(any())).thenReturn(10.0);
		when(scoreCalculatorService.normalizeScore(any(SimpleScoredRepoItem.class), any(Double.class))).thenReturn(1.0);

		when(scoredRepoConverter.convertItemToDto(any(SimpleScoredRepoItem.class), any(Double.class)))
			.thenReturn(SimpleScoredRepoDto.builder()
			                               .fullName("naveen/repo1")
			                               .score(1.0)
			                               .build());

		final ScoredRepoResponse result = scoringService
			.getScoredRepos("java", "2024-01-01", 1, 10)
			.block();

		assertThat(result).isNotNull();
		assertThat(result.getItems()).isNotNull().isNotEmpty();
		assertThat(result.getItems().get(0).getFullName()).isEqualTo("naveen/repo1");
	}
}
