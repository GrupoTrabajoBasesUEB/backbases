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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import co.edu.unbosque.backbases.dto.ErrorDTO;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filtros(HttpSecurity http, ObjectMapper json, JwtService jwtService) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.httpBasic(b -> b.disable())
				.formLogin(f -> f.disable())
				.addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
				.authorizeHttpRequests(a -> a
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
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

	private static void escribir(HttpServletResponse res, ObjectMapper json, ErrorDTO error) throws IOException {
		res.setStatus(HttpStatus.valueOf(error.estado()).value());
		res.setContentType(MediaType.APPLICATION_JSON_VALUE);
		res.setCharacterEncoding("UTF-8");
		json.writeValue(res.getOutputStream(), error);
	}
}
