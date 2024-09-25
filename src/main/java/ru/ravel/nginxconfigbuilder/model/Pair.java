package ru.ravel.nginxconfigbuilder.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Pair {
	private String key;
	private String value;
}
