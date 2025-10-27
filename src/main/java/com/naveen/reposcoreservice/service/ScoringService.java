package com.naveen.reposcoreservice.service;

import com.naveen.reposcoreservice.dto.GithubSearchResponseDto;
import com.naveen.reposcoreservice.dto.ScoredRepoDto;
import com.naveen.reposcoreservice.dto.ScoredRepoItem;
import com.naveen.reposcoreservice.dto.RepoItem;
import com.naveen.reposcoreservice.dto.ScoredRepoConverter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScoringService {

	private final ScoreCalculatorService scoreCalculator;
	private final ScoredRepoConverter scoredRepoConverter;

	public List<ScoredRepoDto> score(final GithubSearchResponseDto githubSearchResponseDto) {
		final List<ScoredRepoItem> scoredItems = githubSearchResponseDto.getItems().stream()
		                                                                .map(this::scoreRepository)
		                                                                .toList();

		final double maxRawScore = scoredItems.stream()
		                                      .mapToDouble(ScoredRepoItem::getRawScore)
		                                      .max()
		                                      .orElse(1.0);

		return scoredItems.stream()
		                  .sorted(Comparator.comparingDouble(ScoredRepoItem::getRawScore).reversed())
		                  .map(item -> scoredRepoConverter.convert(
			                  item.getRepoItem(),
			                  100.0 * item.getRawScore() / maxRawScore))
		                  .collect(Collectors.toList());
	}

	private ScoredRepoItem scoreRepository(RepoItem item) {
		final double rawScore = scoreCalculator.calculateRawScore(item);
		return ScoredRepoItem.builder()
		                     .repoItem(item)
		                     .rawScore(rawScore)
		                     .build();
	}

}
