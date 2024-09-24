package ru.ravel.nginxconfigbuilder.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/auth")
class AuthController {

	@GetMapping("/status")
	ResponseEntity<Object> getStatus(HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			if (auth.isAuthenticated() && auth.getPrincipal() != "anonymousUser") {
				response.setStatus(HttpServletResponse.SC_OK);
				return ResponseEntity.ok().body(Map.of("authenticated", auth.isAuthenticated(), "user", auth.getName()));
			} else {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return ResponseEntity.ok().body(Map.of("authenticated", auth.isAuthenticated()));
			}
		} else {
			return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(Map.of("authenticated", false));
		}
	}

}