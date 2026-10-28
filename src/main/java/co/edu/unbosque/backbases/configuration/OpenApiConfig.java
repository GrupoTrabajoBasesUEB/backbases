package co.edu.unbosque.backbases.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	private static final String ESQUEMA = "bearerAuth";

	@Bean
	public OpenAPI openApi(@Value("${app.version}") String version) {
		return new OpenAPI()
				.info(new Info()
						.title("API Ferretería")
						.version(version)
						.description("API del sistema de la cadena de ferreterías (Bases de Datos 1). "
								+ "Inicie sesión en /api/auth/login y pegue el token en Authorize."))
				.components(new Components().addSecuritySchemes(ESQUEMA,
						new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
				.addSecurityItem(new SecurityRequirement().addList(ESQUEMA));
	}
}
