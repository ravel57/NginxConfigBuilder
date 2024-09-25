package ru.ravel.nginxconfigbuilder.model;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class Upstream {
	private String name;
	private String server;
}
