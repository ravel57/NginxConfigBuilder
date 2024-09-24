package ru.ravel.nginxconfigbuilder.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApiController {

	@GetMapping("/get-configs")
	public ResponseEntity<Object> getConfigs() {
		return ResponseEntity.ok().body(new ArrayList<>());
	}

}