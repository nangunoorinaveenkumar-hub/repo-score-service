package com.naveen.reposcoreservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naveen.reposcoreservice.dto.GithubSearchResponseDto;
import com.naveen.reposcoreservice.service.exception.GithubClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.InputStream;

@Service
public class GithubSearchClient {

	private final WebClient webClient;
	private final String token;
	private final ObjectMapper objectMapper;

	public GithubSearchClient(WebClient githubWebClient,
		@Value("${github.token:}") String token,
		ObjectMapper objectMapper) {
		this.webClient = githubWebClient;
		this.token = token;
		this.objectMapper = objectMapper;
	}

	public Mono<GithubSearchResponseDto> searchRepositories(
		final String language,
		final String createdAfter,
		final String sort,
		final String order,
		final int page,
		final int perPage
	) {
		if (token == null || token.isBlank()) {
			// Use mock data
			try {
				final InputStream is = new ClassPathResource("mock/mock_repos.json").getInputStream();
				final GithubSearchResponseDto dto = objectMapper.readValue(is, GithubSearchResponseDto.class);
				return Mono.just(dto);
			} catch (Exception e) {
				throw new GithubClientException("Failed to load mock repository data", e);
			}
		}

		final String query = String.format("language:%s created:>%s", language, createdAfter);

		return webClient.get()
		                .uri(uriBuilder -> uriBuilder
			                .path("/search/repositories")
			                .queryParam("q", query)
			                .queryParam("sort", sort)
			                .queryParam("order", order)
			                .queryParam("page", page)
			                .queryParam("per_page", perPage)
			                .build())
		                .retrieve()
		                .bodyToMono(GithubSearchResponseDto.class)
		                .onErrorMap(e -> new GithubClientException("GitHub API request failed", e));
	}
}
