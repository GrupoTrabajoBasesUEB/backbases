package co.edu.unbosque.backbases.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import co.edu.unbosque.backbases.configuration.CorsConfig;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({ SecurityConfig.class, CorsConfig.class, JwtService.class })
@TestPropertySource(properties = {
		"app.cors.allowed-origins=http://localhost:4200",
		"jwt.secret=c2VjcmV0by1zb2xvLXBhcmEtcHJ1ZWJhcy1kZWwtYmFjay0yNTYtYml0cyEhIQ==",
		"jwt.expiration-ms=3600000" })
public @interface PruebaWebSegura {
}
