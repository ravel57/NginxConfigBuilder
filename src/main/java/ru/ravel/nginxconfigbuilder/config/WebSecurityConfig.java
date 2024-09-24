package ru.ravel.nginxconfigbuilder.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/js/**", "/css/**", "/logo.png", "/favicon.png", "/auth/**").permitAll()
						.requestMatchers("/login").permitAll()
						.anyRequest().authenticated()
				)
				.formLogin(form -> form
						.loginPage("/login")
						.loginProcessingUrl("/auth/login")
						.defaultSuccessUrl("/", true)
						.permitAll()
				)
				.logout(logout -> logout
						.logoutUrl("/auth/logout")
						.logoutSuccessUrl("/login")
						.invalidateHttpSession(true)
						.deleteCookies("JSESSIONID")
						.permitAll()
				)
				.build();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	static PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}