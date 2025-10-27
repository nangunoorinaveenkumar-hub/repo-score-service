package com.naveen.reposcoreservice.service;

import com.naveen.reposcoreservice.dto.RepoItem;
import com.naveen.reposcoreservice.service.exception.ScoringException;
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

	public double calculateRawScore(final RepoItem item) {
		final double starsScore = calculateStarsScore(item.getStargazersCount());
		final double forksScore = calculateForksScore(item.getForksCount());
		final double recencyScore = calculateRecencyScore(item);

		return calculateWeightedScore(starsScore, forksScore, recencyScore);
	}

	private double calculateStarsScore(final int stargazersCount) {
		return Math.log10(1 + stargazersCount);
	}

	private double calculateForksScore(final int forksCount) {
		return Math.log10(1 + forksCount);
	}

	private double calculateRecencyScore(final RepoItem repoItem) {
		try {
			final OffsetDateTime pushedAt = OffsetDateTime.parse(repoItem.getPushedAt());
			final double daysSincePush = ChronoUnit.DAYS.between(pushedAt, OffsetDateTime.now(ZoneOffset.UTC));
			return 1.0 / (1.0 + (daysSincePush / recencyHalfLifeDays));
		} catch (Exception ex) {
			throw new ScoringException("Failed to calculate recency score for repo: " + repoItem.getFullName(), ex);
		}
	}

	private double calculateWeightedScore(final double starsScore, final double forksScore, final double recencyScore) {
		return (starsWeight * starsScore) +
		       (forksWeight * forksScore) +
		       (recencyWeight * recencyScore);
	}
}
