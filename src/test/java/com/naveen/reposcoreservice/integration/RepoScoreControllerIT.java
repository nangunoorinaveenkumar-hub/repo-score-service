package com.naveen.reposcoreservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naveen.reposcoreservice.controller.RepoScoreController;
import com.naveen.reposcoreservice.dto.GithubSearchResponseItem;
import com.naveen.reposcoreservice.dto.ScoredRepoItem;
import com.naveen.reposcoreservice.dto.ScoredRepoResponse;
import com.naveen.reposcoreservice.dto.SimpleScoredRepoDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RepoScoreControllerIT {

	@Autowired
	private RepoScoreController repoScoreController;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void getScoredRepos_shouldReturnSortedScoredList() throws Exception {
		final InputStream is = getClass().getClassLoader().getResourceAsStream("mock/mock_repos.json");
		final GithubSearchResponseItem mockResponse = objectMapper.readValue(is, GithubSearchResponseItem.class);

		final Mono<ScoredRepoResponse> resultMono = repoScoreController
			.getScoredRepos("java", "2024-01-01", 1, 5);

		StepVerifier.create(resultMono)
		            .assertNext(scoredRepoResponse -> {
			            final List<SimpleScoredRepoDto> scoredItems = new ArrayList<>(scoredRepoResponse.getItems());
			            final List<ScoredRepoItem> mockItems = new ArrayList<>(mockResponse.getItems());

			            scoredItems.sort(Comparator.comparing(SimpleScoredRepoDto::getFullName, String.CASE_INSENSITIVE_ORDER));
			            mockItems.sort(Comparator.comparing(ScoredRepoItem::getFullName, String.CASE_INSENSITIVE_ORDER));

			            assertThat(scoredItems).hasSameSizeAs(mockItems);
			            IntStream.range(0, scoredItems.size())
			                     .forEach(i -> {
				                     assertThat(scoredItems.get(i).getFullName())
					                     .isEqualTo(mockItems.get(i).getFullName());
				                     assertThat(scoredItems.get(i).getScore())
					                     .isGreaterThanOrEqualTo(0);
			                     });
		            })
		            .expectComplete()
		            .verify();
	}
}
