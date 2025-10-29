package com.naveen.reposcoreservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.within;

import com.naveen.reposcoreservice.dto.ScoredRepoItem;
import com.naveen.reposcoreservice.dto.SimpleScoredRepoItem;
import com.naveen.reposcoreservice.service.exception.ScoringException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ScoreCalculatorServiceTest {

	private ScoreCalculatorService scoreCalculatorService;

	@BeforeEach
	void setUp() {
		scoreCalculatorService = new ScoreCalculatorService();
		ReflectionTestUtils.setField(scoreCalculatorService, "starsWeight", 0.5);
		ReflectionTestUtils.setField(scoreCalculatorService, "forksWeight", 0.3);
		ReflectionTestUtils.setField(scoreCalculatorService, "recencyWeight", 0.2);
		ReflectionTestUtils.setField(scoreCalculatorService, "recencyHalfLifeDays", 30.0);
	}

	@Test
	void calculateRepositoryRawScore_shouldReturnWeightedScore() {
		final String now = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

		final ScoredRepoItem scoredRepoItem = ScoredRepoItem.builder()
		                                                    .fullName("test-repo")
		                                                    .stargazersCount(100)
		                                                    .forksCount(50)
		                                                    .pushedAt(now)
		                                                    .build();

		final double rawScore = scoreCalculatorService.calculateRepositoryRawScore(scoredRepoItem);

		assertThat(rawScore).isGreaterThan(0);
	}

	@Test
	void normalizeScore_shouldReturn100PercentWhenRawEqualsMax() {
		final SimpleScoredRepoItem item = SimpleScoredRepoItem.builder().rawScore(50.0).build();
		final double normalized = scoreCalculatorService.normalizeScore(item, 50.0);
		assertThat(normalized).isEqualTo(100.0);
	}

	@Test
	void normalizeScore_shouldReturn0WhenMaxRawScoreIsZero() {
		final SimpleScoredRepoItem item = SimpleScoredRepoItem.builder().rawScore(50.0).build();
		final double normalized = scoreCalculatorService.normalizeScore(item, 0.0);
		assertThat(normalized).isEqualTo(0.0);
	}

	@Test
	void calculateRecencyScoreFromPushDate_shouldReturnValueBetween0And1() {
		final String now = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		final double recencyScore =
			ReflectionTestUtils.invokeMethod(scoreCalculatorService,
				"calculateRecencyScoreFromPushDate", now, "repo-name");
		assertThat(recencyScore).isGreaterThan(0).isLessThanOrEqualTo(1.0);
	}

	@Test
	void calculateRecencyScoreFromPushDate_shouldThrowScoringExceptionForInvalidDate() {
		final String invalidDate = "invalid-date-string";
		assertThatThrownBy(() ->
			ReflectionTestUtils.invokeMethod(scoreCalculatorService,
				"calculateRecencyScoreFromPushDate", invalidDate, "repo-name")
		).isInstanceOf(ScoringException.class)
		 .hasMessageContaining("Failed to calculate recency score");
	}

	@Test
	void calculateWeightedScoreFromComponents_shouldReturnCorrectWeightedSum() {
		final double result =
			ReflectionTestUtils.invokeMethod(scoreCalculatorService,
				"calculateWeightedScoreFromComponents", 2.0, 3.0, 1.0);
		assertThat(result).isEqualTo(2.1);
	}

	@Test
	void calculateRecencyScore_shouldReturnCorrectValue() {
		final double recency = ReflectionTestUtils.invokeMethod(scoreCalculatorService, "calculateRecencyScore", 15.0);
		assertThat(recency).isCloseTo(0.666, within(0.01));
	}

	@Test
	void calculateLogarithmicScore_shouldReturnCorrectValue() {
		final double logScore = ReflectionTestUtils.invokeMethod(scoreCalculatorService, "calculateLogarithmicScore", 99);
		assertThat(logScore).isCloseTo(2.0, within(0.001));
	}
}