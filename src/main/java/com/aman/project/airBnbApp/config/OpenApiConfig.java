package com.aman.project.airBnbApp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
			// 1. Link the security requirement to the scheme below
			.addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
			.components(
				new Components()
					// 2. Define what "BearerAuth" looks like
					.addSecuritySchemes(
						"BearerAuth",
						new SecurityScheme()
							.name("BearerAuth")
							.type(SecurityScheme.Type.HTTP)
							.scheme("bearer")
							.bearerFormat("JWT")
					)
			);
	}
}
// from gemini -
/// problem was that after i logged in , there was no option to add the access token
