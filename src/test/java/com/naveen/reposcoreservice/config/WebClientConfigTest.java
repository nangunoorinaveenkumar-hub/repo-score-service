package com.naveen.reposcoreservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringBootTest
@ActiveProfiles("test")
class WebClientConfigTest {

	@Autowired
	private WebClient githubWebClient;

	@Test
	void githubWebClient_shouldHaveDefaultHeaders() {
		final WebClient webClient = githubWebClient.mutate()
		                                     .filter((request, next) -> {
			                                     assertThat(request.headers().getFirst(HttpHeaders.ACCEPT)).isEqualTo("application/vnd.github+json");
			                                     assertThat(request.headers().getFirst("X-GitHub-Api-Version")).isEqualTo("2022-11-28");
			                                     assertThat(request.headers().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer dummy-token");
			                                     return next.exchange(request);
		                                     })
		                                     .build();

		webClient.get().uri("/test").exchangeToMono(response -> Mono.empty()).block();
	}
}