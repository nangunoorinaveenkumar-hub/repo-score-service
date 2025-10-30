package com.naveen.reposcoreservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringBootTest
@ActiveProfiles("test")
class WebClientConfigTest {

	@Autowired
	private WebClient githubWebClient;

	@Test
	void githubWebClient_shouldHaveDefaultHeaders() {
		final ExchangeFunction mockExchange = request -> {
			assertThat(request.headers().getFirst("Accept")).isEqualTo("application/vnd.github+json");
			assertThat(request.headers().getFirst("X-GitHub-Api-Version")).isEqualTo("v3");
			return Mono.just(ClientResponse.create(HttpStatusCode.valueOf(200)).build());
		};

		final WebClient testClient = githubWebClient.mutate()
		                                      .exchangeFunction(mockExchange)
		                                      .build();

		testClient.get().uri("/test").exchangeToMono(resp -> Mono.empty()).block();
	}
}