package co.edu.unbosque.backbases.security;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import co.edu.unbosque.backbases.dto.ErrorDTO;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private static final String[] PUBLICAS = {
			"/api/estado",
			"/", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
	};

	@Bean
	public SecurityFilterChain filtros(HttpSecurity http, ObjectMapper json) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.httpBasic(b -> b.disable())
				.formLogin(f -> f.disable())
				.authorizeHttpRequests(a -> a
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(PUBLICAS).permitAll()
						.anyRequest().authenticated())
				.exceptionHandling(e -> e
						.authenticationEntryPoint((req, res, ex) -> escribir(res, json,
								ErrorDTO.de(401, "NO_AUTENTICADO", "Debe iniciar sesión.")))
						.accessDeniedHandler((req, res, ex) -> escribir(res, json,
								ErrorDTO.de(403, "SIN_PERMISO", "No tiene permiso para esta operación."))));
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return correo -> {
			throw new UsernameNotFoundException("Sin usuarios configurados todavía.");
		};
	}

	private static void escribir(HttpServletResponse res, ObjectMapper json, ErrorDTO error) throws IOException {
		res.setStatus(HttpStatus.valueOf(error.estado()).value());
		res.setContentType(MediaType.APPLICATION_JSON_VALUE);
		res.setCharacterEncoding("UTF-8");
		json.writeValue(res.getOutputStream(), error);
	}
}
