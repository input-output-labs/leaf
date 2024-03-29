package fr.iolabs.leafdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.AuthInterceptor;

@Configuration
@EnableScheduling
public class LeafConfig implements WebMvcConfigurer {

	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(this.authInterceptor()).addPathPatterns("/api/**")
				.excludePathPatterns("/api/account/login").excludePathPatterns("/api/account/register");
	}

	@Bean
	public AuthInterceptor authInterceptor() {
		return new AuthInterceptor();
	}

	@Bean
	@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public LeafContext coreContext() {
		return new LeafContext();
	}
}