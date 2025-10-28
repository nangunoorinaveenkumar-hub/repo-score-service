package com.naveen.reposcoreservice.service;

import com.naveen.reposcoreservice.dto.GithubSearchResponseItem;
import com.naveen.reposcoreservice.dto.SimpleScoredRepoDto;
import com.naveen.reposcoreservice.dto.SimpleScoredRepoItem;
import com.naveen.reposcoreservice.dto.ScoredRepoItem;
import com.naveen.reposcoreservice.dto.ScoredRepoConverter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScoringService {

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
			                  scoreCalculatorService.getScore(item, maxRawScore)))
		                  .collect(Collectors.toList());
	}

	private SimpleScoredRepoItem scoreRepository(ScoredRepoItem repoItem) {
		final double rawScore = scoreCalculatorService.calculateRawScore(repoItem);
		return SimpleScoredRepoItem.builder()
                                   .fullName(repoItem.getFullName())
                                   .htmlUrl(repoItem.getHtmlUrl())
                                   .description(repoItem.getDescription())
                                   .rawScore(rawScore)
                                   .build();
	}

}