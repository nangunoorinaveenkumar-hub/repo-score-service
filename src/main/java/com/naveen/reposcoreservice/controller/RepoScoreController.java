package com.naveen.reposcoreservice.controller;

import com.naveen.reposcoreservice.dto.ScoredRepoResponse;
import com.naveen.reposcoreservice.service.ScoringService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/repos")
@RequiredArgsConstructor
public class RepoScoreController {

	private final ScoringService scoringService;

	@GetMapping("/score")
	public Mono<ScoredRepoResponse> getScoredRepos(
		@RequestParam @Parameter(description = "GitHub repo language") String language,
		@RequestParam("created_after") @Parameter(description = "Earliest created date in YYYY-MM-DD format") String createdAfter,
		@RequestParam(defaultValue = "1") @Parameter(description = "Page number") int page,
		@RequestParam(value = "per_page", defaultValue = "5") @Parameter(description = "Items per page") int perPage
	) {
		return scoringService.getScoredRepos(language, createdAfter, page, perPage);
	}
}
