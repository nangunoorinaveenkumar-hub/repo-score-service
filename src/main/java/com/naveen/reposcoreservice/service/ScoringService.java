package com.naveen.reposcoreservice.service;

import com.naveen.reposcoreservice.dto.GithubSearchRequest;
import com.naveen.reposcoreservice.dto.GithubSearchResponseItem;
import com.naveen.reposcoreservice.converter.ScoredRepoConverter;
import com.naveen.reposcoreservice.dto.ScoredRepoItem;
import com.naveen.reposcoreservice.dto.SimpleScoredRepoDto;
import com.naveen.reposcoreservice.dto.SimpleScoredRepoItem;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ScoringService {

	private final GithubSearchClient githubSearchClient;
	private final ScoreCalculatorService scoreCalculatorService;
	private final ScoredRepoConverter scoredRepoConverter;

	public List<SimpleScoredRepoDto> score(final GithubSearchResponseItem githubSearchResponseItem) {
		final List<SimpleScoredRepoItem> scoredItems = githubSearchResponseItem.getItems().stream()
		                                                                       .map(this::scoreRepository)
		                                                                       .toList();

		final double maxRawScore = scoredItems.stream()
		                                      .mapToDouble(SimpleScoredRepoItem::getRawScore)
		                                      .max()
		                                      .orElse(1.0);

		return scoredItems.stream()
		                  .sorted(Comparator.comparingDouble(SimpleScoredRepoItem::getRawScore).reversed())
		                  .map(item -> scoredRepoConverter.convertItemToDto(
			                  item,
			                  scoreCalculatorService.normalizeScore(item, maxRawScore)))
		                  .collect(Collectors.toList());
	}

	private SimpleScoredRepoItem scoreRepository(final ScoredRepoItem repoItem) {
		final double rawScore = scoreCalculatorService.calculateRepositoryRawScore(repoItem);
		return SimpleScoredRepoItem.builder()
		                           .fullName(repoItem.getFullName())
		                           .htmlUrl(repoItem.getHtmlUrl())
		                           .description(repoItem.getDescription())
		                           .rawScore(rawScore)
		                           .build();
	}

	public Mono<List<SimpleScoredRepoDto>> getScoredRepos(final String language, final String createdAfter, final int page, final int perPage) {
		final GithubSearchRequest request = GithubSearchRequest.builder()
		                                                       .language(language)
		                                                       .createdAfter(createdAfter)
		                                                       .sort("stars")
		                                                       .order("desc")
		                                                       .page(page)
		                                                       .perPage(perPage)
		                                                       .build();

		return githubSearchClient.searchRepositories(request).map(this::score);
	}
}