package com.naveen.reposcoreservice.service;

import com.naveen.reposcoreservice.dto.ScoredRepoItem;
import com.naveen.reposcoreservice.dto.SimpleScoredRepoItem;
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

	public double calculateRawScore(final ScoredRepoItem repoDto) {
		final double starsScore = calculateScore(repoDto.getStargazersCount());
		final double forksScore = calculateScore(repoDto.getForksCount());
		final double recencyScore = calculateRecencyScore(repoDto);

		return calculateWeightedScore(starsScore, forksScore, recencyScore);
	}

	public double getScore(final SimpleScoredRepoItem item, final double maxRawScore) {
		return 100.0 * item.getRawScore() / maxRawScore;
	}

	private double calculateScore(final int count) {
		return Math.log10(1 + count);
	}

	private double calculateRecencyScore(final ScoredRepoItem repoDto) {
		try {
			final OffsetDateTime pushedAt = OffsetDateTime.parse(repoDto.getPushedAt());
			final double daysSincePush = ChronoUnit.DAYS.between(pushedAt, OffsetDateTime.now(ZoneOffset.UTC));
			return getRecencyScore(daysSincePush);
		} catch (Exception ex) {
			throw new ScoringException("Failed to calculate recency score for repo: " + repoDto.getFullName(), ex);
		}
	}

	private double getRecencyScore(final double daysSincePush) {
		return 1.0 / (1.0 + (daysSincePush / recencyHalfLifeDays));
	}

	private double calculateWeightedScore(final double starsScore, final double forksScore, final double recencyScore) {
		return (starsWeight * starsScore) +
		       (forksWeight * forksScore) +
		       (recencyWeight * recencyScore);
	}

}