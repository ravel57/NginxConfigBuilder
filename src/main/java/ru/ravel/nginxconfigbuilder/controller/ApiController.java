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


	@GetMapping("/configs")
	public ResponseEntity<Object> getConfigs() {
		return ResponseEntity.ok().body(nginxConfigService.getConfigs());
	}


	@PostMapping("/config")
	public ResponseEntity<Object> addNewConfig(@RequestBody Config config) {
		return ResponseEntity.ok().body(nginxConfigService.saveConfig(config));
	}


	@DeleteMapping("/config/{domain}")
	public ResponseEntity<Object> deleteConfig(@PathVariable String domain) {
		return ResponseEntity.ok().body(nginxConfigService.deleteConfig(domain));
	}


	@PostMapping("/renew-certificate")
	public ResponseEntity<Object> renewCertificate(@RequestBody Config config) {
		return ResponseEntity.ok().body(nginxConfigService.renewCertificate());
	}


}