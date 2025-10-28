package com.naveen.reposcoreservice.controller;

import com.naveen.reposcoreservice.service.GithubSearchClient;
import com.naveen.reposcoreservice.dto.SimpleScoredRepoDto;
import com.naveen.reposcoreservice.service.ScoringService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/repos")
@RequiredArgsConstructor
public class RepoScoreController {

	private final GithubSearchClient githubSearchClient;
	private final ScoringService scoringService;

	/**
	 * Endpoint to fetch repositories and return scored list.
	 *
	 * @param language GitHub repo language
	 * @param createdAfter earliest created date in YYYY-MM-DD format
	 * @param page page number (optional)
	 * @param perPage items per page (optional)
	 * @return Mono<List<SimpleScoredRepoDto>> scored repositories
	 */
	@GetMapping("/score")
	public Mono<List<SimpleScoredRepoDto>> getScoredRepos(
		@RequestParam final String language,
		@RequestParam("created_after") final String createdAfter,
		@RequestParam(defaultValue = "1") final int page,
		@RequestParam(value = "per_page", defaultValue = "5")final int perPage
	) {
		return githubSearchClient.searchRepositories(
			                         language,
			                         createdAfter,
			                         "stars",
			                         "desc",
			                         page,
			                         perPage
		                         )
		                         .map(scoringService::score);
	}
}