package com.naveen.reposcoreservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI repoScoreOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("Repo Score Service API")
				.description("API documentation for scoring GitHub repositories by popularity and activity.")
				.version("1.0.0")
				.contact(new Contact()
					.name("Naveen Kumar")
					.email("nanugnoori.naveenkumar@gmail.com")));
	}
}
