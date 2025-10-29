package com.naveen.reposcoreservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	@Bean
	public WebClient githubWebClient(
		@Value("${github.api.base-url}") final String apiUrl,
		@Value("${github.client.max-in-memory-size}") final int maxInMemorySize,
		@Value("${github.api-version}") final String apiVersion,
		@Value("${github.token:}") final String token
	) {
		final WebClient.Builder builder = WebClient.builder()
		                                     .baseUrl(apiUrl)
                                             .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxInMemorySize))
		                                     .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
		                                     .defaultHeader("X-GitHub-Api-Version", apiVersion);

		if (token != null && !token.isBlank()) {
			builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
		}

		return builder.build();
	}
}