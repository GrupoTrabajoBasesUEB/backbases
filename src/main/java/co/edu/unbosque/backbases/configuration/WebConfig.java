package co.edu.unbosque.backbases.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import co.edu.unbosque.backbases.security.ClaveSeguridadInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final ClaveSeguridadInterceptor claveSeguridadInterceptor;

	public WebConfig(ClaveSeguridadInterceptor claveSeguridadInterceptor) {
		this.claveSeguridadInterceptor = claveSeguridadInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(claveSeguridadInterceptor).addPathPatterns("/api/**");
	}
}
