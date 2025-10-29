package com.naveen.reposcoreservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naveen.reposcoreservice.dto.GithubSearchRequest;
import com.naveen.reposcoreservice.dto.GithubSearchResponseItem;
import com.naveen.reposcoreservice.service.exception.GithubClientException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.InputStream;

@Service
@Slf4j
public class GithubSearchClient {

	private final WebClient webClient;
	private final String token;
	private final ObjectMapper objectMapper;

	private final ConcurrentMap<String, Mono<GithubSearchResponseItem>> cache = new ConcurrentHashMap<>();
	private final Duration cacheTtl = Duration.ofMinutes(5);

	public GithubSearchClient(final WebClient webClient,
		@Value("${github.token:}") final String token,
		final ObjectMapper objectMapper) {
		this.webClient = webClient;
		this.token = token;
		this.objectMapper = objectMapper;
	}

	public Mono<GithubSearchResponseItem> searchRepositories(final GithubSearchRequest githubSearchRequest) {

		final String cacheKey = buildCacheKey(githubSearchRequest);

		return cache.computeIfAbsent(cacheKey, key -> fetchAndCache(githubSearchRequest, cacheKey));
	}

	public Mono<GithubSearchResponseItem> fetchAndCache(
		final GithubSearchRequest githubSearchRequest,
		final  String cacheKey
	) {

		if (token == null || token.isBlank()) {
			return loadMockRepositories();
		}

		final String query = String.format("language:%s created:>%s", githubSearchRequest.getLanguage(), githubSearchRequest.getCreatedAfter());

		final Mono<GithubSearchResponseItem> sourceMono =  webClient.get()
		                .uri(uriBuilder -> uriBuilder
			                .path("/search/repositories")
			                .queryParam("q", query)
			                .queryParam("sort", githubSearchRequest.getSort())
			                .queryParam("order", githubSearchRequest.getOrder())
			                .queryParam("page", githubSearchRequest.getPage())
			                .queryParam("per_page", githubSearchRequest.getPerPage())
			                .build())
		                .retrieve()
		                .bodyToMono(GithubSearchResponseItem.class)
                        .doOnSubscribe(sub -> log.info("Fetching GitHub repos for key={}", cacheKey))
                        .doOnError(err -> log.error("GitHub API call failed: {}", err.getMessage()))
                        .onErrorMap(e -> new GithubClientException("GitHub API request failed", e));

		return sourceMono.cache(cacheTtl)
		                 .doOnNext(dto -> log.info("Caching response for key={} for {} seconds", cacheKey, cacheTtl.getSeconds()));
	}

	private String buildCacheKey(final GithubSearchRequest githubSearchRequest) {
		return String.join(":", githubSearchRequest.getLanguage(), githubSearchRequest.getCreatedAfter(), githubSearchRequest.getSort(),
			githubSearchRequest.getOrder(), String.valueOf(githubSearchRequest.getPage()), String.valueOf(githubSearchRequest.getPerPage()));
	}

	private Mono<GithubSearchResponseItem> loadMockRepositories() {
		try (InputStream is = new ClassPathResource("mock/mock_repos.json").getInputStream()) {
			final GithubSearchResponseItem githubSearchResponseItem = objectMapper.readValue(is, GithubSearchResponseItem.class);
			return Mono.just(githubSearchResponseItem);
		} catch (Exception e) {
			throw new GithubClientException("Failed to load mock repository data", e);
		}
	}
}