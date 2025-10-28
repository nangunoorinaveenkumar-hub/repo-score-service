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
		@Value("${github.api-url}") final String apiUrl,
		@Value("${github.api-version}") final String apiVersion,
		@Value("${github.token:}") final String token
	) {
		final WebClient.Builder builder = WebClient.builder()
		                                     .baseUrl(apiUrl)
		                                     .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
		                                     .defaultHeader("X-GitHub-Api-Version", apiVersion);

		if (token != null && !token.isBlank()) {
			builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
		}

		return builder.build();
	}
}