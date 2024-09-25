package ru.ravel.nginxconfigbuilder.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class Config {
	private String domain;
	private Integer port;
	private Boolean isSsl;
	private List<Location> location;
	private String upstream;
	private Certificate certificates;
	private String certificatesKeyPath;
}
