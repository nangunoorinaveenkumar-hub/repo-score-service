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
import java.util.Comparator;
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
			            scoredRepoResponse.getItems().sort(Comparator.comparing(SimpleScoredRepoDto::getFullName, String.CASE_INSENSITIVE_ORDER));
			            mockResponse.getItems().sort(Comparator.comparing(ScoredRepoItem::getFullName, String.CASE_INSENSITIVE_ORDER));

			            assertThat(scoredRepoResponse.getItems()).hasSameSizeAs(mockResponse.getItems());

			            IntStream.range(0, scoredRepoResponse.getItems().size())
			                     .forEach(i -> {
				                     assertThat(scoredRepoResponse.getItems().get(i).getFullName())
					                     .isEqualTo(mockResponse.getItems().get(i).getFullName());
				                     assertThat(scoredRepoResponse.getItems().get(i).getScore())
					                     .isGreaterThanOrEqualTo(0);
			                     });
		            })
		            .expectComplete()
		            .verify();
	}
}
