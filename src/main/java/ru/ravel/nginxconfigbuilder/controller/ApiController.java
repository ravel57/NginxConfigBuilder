package ru.ravel.nginxconfigbuilder.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ravel.nginxconfigbuilder.model.Config;
import ru.ravel.nginxconfigbuilder.service.NginxConfigService;


@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApiController {

	private final NginxConfigService nginxConfigService;


	@GetMapping("/get-configs")
	public ResponseEntity<Object> getConfigs() {
		return ResponseEntity.ok().body(nginxConfigService.getConfigInfo());
	}


	@PostMapping("/add-new-config")
	public ResponseEntity<Object> addNewConfig(@RequestBody Config config) {
		return ResponseEntity.ok().body(nginxConfigService.addNewConfig(config));
	}


}