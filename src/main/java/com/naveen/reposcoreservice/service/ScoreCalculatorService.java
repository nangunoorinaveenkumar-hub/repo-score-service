package com.naveen.reposcoreservice.service;

import com.naveen.reposcoreservice.dto.ScoredRepoItem;
import com.naveen.reposcoreservice.dto.SimpleScoredRepoItem;
import com.naveen.reposcoreservice.exception.ScoringException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ScoreCalculatorService {

	@Value("${scoring.stars-weight}")
	private double starsWeight;

	@Value("${scoring.forks-weight}")
	private double forksWeight;

	@Value("${scoring.recency-weight}")
	private double recencyWeight;

	@Value("${scoring.recency-half-life-days}")
	private double recencyHalfLifeDays;

	public double calculateRepositoryRawScore(final ScoredRepoItem repoItem) {
		final double starsScore = calculateLogarithmicScore(repoItem.getStargazersCount());
		final double forksScore = calculateLogarithmicScore(repoItem.getForksCount());
		final double recencyScore = calculateRecencyScoreFromPushDate(repoItem.getPushedAt(), repoItem.getFullName());

		return calculateWeightedScoreFromComponents(starsScore, forksScore, recencyScore);
	}

	public double normalizeScore(final SimpleScoredRepoItem item, final double maxRawScore) {
		if (maxRawScore == 0) return 0;
		return 100.0 * item.getRawScore() / maxRawScore;
	}

	private double calculateLogarithmicScore(final int count) {
		return Math.log10(1.0 + count);
	}

	private double calculateRecencyScoreFromPushDate(final String pushedAtStr, final String repoFullName) {
		try {
			final OffsetDateTime pushedAt = OffsetDateTime.parse(pushedAtStr);
			final double daysSincePush = calculateDaysSincePush(pushedAt);
			return calculateRecencyScore(daysSincePush);
		} catch (Exception ex) {
			throw new ScoringException(
				"Failed to calculate recency score for repo: " + repoFullName, ex
			);
		}
	}

	private double calculateDaysSincePush(final OffsetDateTime pushedAt) {
		return ChronoUnit.DAYS.between(pushedAt, OffsetDateTime.now(ZoneOffset.UTC));
	}

	private double calculateRecencyScore(final double daysSincePush) {
		return 1.0 / (1.0 + (daysSincePush / recencyHalfLifeDays));
	}

	private double calculateWeightedScoreFromComponents(final double starsScore, final double forksScore, final double recencyScore) {
		return (starsWeight * starsScore) +
		       (forksWeight * forksScore) +
		       (recencyWeight * recencyScore);
	}
}