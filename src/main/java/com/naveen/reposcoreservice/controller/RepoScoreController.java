package com.naveen.reposcoreservice.controller;

import com.naveen.reposcoreservice.dto.SimpleScoredRepoDto;
import com.naveen.reposcoreservice.service.ScoringService;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/repos")
@RequiredArgsConstructor
public class RepoScoreController {

	private final ScoringService scoringService;

	@GetMapping("/score")
	public Mono<List<SimpleScoredRepoDto>> getScoredRepos(
		@RequestParam @Parameter(description = "GitHub repo language") String language,
		@RequestParam("created_after") @Parameter(description = "Earliest created date in YYYY-MM-DD format") String createdAfter,
		@RequestParam(defaultValue = "1") @Parameter(description = "Page number") int page,
		@RequestParam(value = "per_page", defaultValue = "5") @Parameter(description = "Items per page") int perPage
	) {
		return scoringService.getScoredRepos(language, createdAfter, page, perPage);
	}
}
