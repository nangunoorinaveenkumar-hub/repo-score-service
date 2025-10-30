package com.naveen.reposcoreservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naveen.reposcoreservice.dto.GithubSearchRequest;
import com.naveen.reposcoreservice.dto.GithubSearchResponseItem;
import com.naveen.reposcoreservice.dto.ScoredRepoItem;
import com.naveen.reposcoreservice.exception.GithubClientException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class GithubSearchClientTest {

	@Mock
	private WebClient webClient;

	@Mock
	private ObjectMapper objectMapper;

	private GithubSearchClient githubSearchClientWithToken;

	private GithubSearchClient githubSearchClientNoToken;

	private GithubSearchRequest githubSearchRequest;

	private GithubSearchResponseItem githubSearchResponseItem;

	@BeforeEach
	void setUp() {
		githubSearchRequest = GithubSearchRequest.builder()
		                                         .language("java")
		                                         .createdAfter("2024-01-01")
		                                         .sort("stars")
		                                         .order("desc")
		                                         .page(1)
		                                         .perPage(5)
		                                         .build();

		githubSearchClientNoToken = new GithubSearchClient(webClient, "", objectMapper, 5);
		githubSearchClientWithToken = new GithubSearchClient(webClient, "dummy-token", objectMapper,5);

		final ScoredRepoItem scoredRepoItem = ScoredRepoItem.builder()
		                                                    .fullName("naveen/repo1")
		                                                    .htmlUrl("http://github.com/naveen/repo1")
		                                                    .description("Test repo 1")
		                                                    .build();

		final ScoredRepoItem scoredRepoItem1 = ScoredRepoItem.builder()
		                                                     .fullName("naveen/repo2")
		                                                     .htmlUrl("http://github.com/naveen/repo2")
		                                                     .description("Test repo 2")
		                                                     .build();

		githubSearchResponseItem = GithubSearchResponseItem.builder()
		                                                   .totalCount(2)
		                                                   .incompleteResults(false)
		                                                   .items(List.of(scoredRepoItem, scoredRepoItem1))
		                                                   .build();
	}

	@Test
	void fetchAndCache_shouldLoadMockDataWhenTokenBlank() throws Exception {

		try (InputStream is = new ClassPathResource("mock/mock_repos.json").getInputStream()) {
			when(objectMapper.readValue(any(InputStream.class), eq(GithubSearchResponseItem.class)))
				.thenReturn(githubSearchResponseItem);
		}

		final Mono<GithubSearchResponseItem> resultMono =
			githubSearchClientNoToken.fetchAndCache(githubSearchRequest, "cache-key");

		final GithubSearchResponseItem result = resultMono.block();
		assertThat(result).isNotNull();

		verify(objectMapper, times(1))
			.readValue(any(InputStream.class), eq(GithubSearchResponseItem.class));
	}

	@Test
	void fetchAndCache_shouldCallWebClientWhenTokenPresent() {
		final WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
		final WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

		when(webClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
		when(requestHeadersUriSpec.uri(any(java.util.function.Function.class)))
			.thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.bodyToMono(GithubSearchResponseItem.class)).thenReturn(Mono.just(githubSearchResponseItem));

		final GithubSearchClient client = new GithubSearchClient(webClient, "dummy-token", new ObjectMapper(),5);
		final GithubSearchResponseItem result =
			client.fetchAndCache(GithubSearchRequest.builder()
			                                        .language("java")
			                                        .createdAfter("2024-01-01")
			                                        .sort("stars")
			                                        .order("desc")
			                                        .page(1)
			                                        .perPage(5)
			                                        .build(),
				      "cache-key")
			      .block();

		assertThat(result).isNotNull();
		verify(webClient).get();
		verify(requestHeadersUriSpec).retrieve();
		verify(responseSpec).bodyToMono(GithubSearchResponseItem.class);
	}

	@Test
	void fetchAndCache_shouldWrapErrorsInGithubClientException() {
		final WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
		final WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

		when(webClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
		when(requestHeadersUriSpec.uri(any(java.util.function.Function.class)))
			.thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.bodyToMono(GithubSearchResponseItem.class))
			.thenReturn(Mono.error(new RuntimeException("GitHub API down")));

		final Mono<GithubSearchResponseItem> resultMono =
			githubSearchClientWithToken.fetchAndCache(githubSearchRequest, "cache-key");

		assertThatThrownBy(resultMono::block)
			.isInstanceOf(GithubClientException.class)
			.hasMessageContaining("GitHub API request failed");
	}

	@Test
	void searchRepositories_shouldReturnCachedMono() {
		final GithubSearchClient client = new GithubSearchClient(webClient, "dummy-token", objectMapper,5);
		final WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
		final WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

		when(webClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
		when(requestHeadersUriSpec.uri(any(java.util.function.Function.class)))
			.thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.bodyToMono(GithubSearchResponseItem.class)).thenReturn(Mono.just(githubSearchResponseItem));

		final Mono<GithubSearchResponseItem> firstCall = client.searchRepositories(githubSearchRequest);
		final Mono<GithubSearchResponseItem> secondCall = client.searchRepositories(githubSearchRequest);

		assertThat(firstCall).isSameAs(secondCall);
	}

	@Test
	@SuppressWarnings("unchecked")
	void fetchAndCache_shouldBuildCorrectUri() {
		final WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
		final WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

		when(webClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
		when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.bodyToMono(GithubSearchResponseItem.class)).thenReturn(Mono.just(githubSearchResponseItem));

		final GithubSearchClient client = new GithubSearchClient(webClient, "dummy-token", new ObjectMapper(), 5);
		client.fetchAndCache(githubSearchRequest, "cache-key").block();

		final ArgumentCaptor<Function> uriFunctionCaptor = ArgumentCaptor.forClass(Function.class);
		verify(requestHeadersUriSpec).uri(uriFunctionCaptor.capture());

		final Function uriFunction = uriFunctionCaptor.getValue();

		final DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
		final URI uri = (URI) uriFunction.apply(factory.builder());

		assertThat(uri.getPath()).isEqualTo("/search/repositories");
		assertThat(uri.getQuery())
			.contains("q=language:java created:>2024-01-01")
			.contains("sort=stars")
			.contains("order=desc");
	}

	@Test
	void buildCacheKey_shouldReturnExpectedFormat() throws Exception {
		final Method method = GithubSearchClient.class.getDeclaredMethod("buildCacheKey", GithubSearchRequest.class);
		method.setAccessible(true);

		final String key = (String) method.invoke(githubSearchClientWithToken, githubSearchRequest);

		assertThat(key)
			.isEqualTo("java:2024-01-01:stars:desc:1:5")
			.contains("java")
			.contains("stars")
			.contains("1");
	}
}