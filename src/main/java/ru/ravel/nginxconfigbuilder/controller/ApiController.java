package ru.ravel.nginxconfigbuilder.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ravel.nginxconfigbuilder.service.NginxConfigParser;


@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApiController {

	private final NginxConfigParser nginxConfigParser;

	@GetMapping("/get-configs")
	public ResponseEntity<Object> getConfigs() {
		return ResponseEntity.ok().body(nginxConfigParser.getConfigInfo());
	}

}