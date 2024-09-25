package ru.ravel.nginxconfigbuilder.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Config {
	private String serverName;
	private String port;
	private Location location;
	private Certificate certificates;
	private String certificatesKeyPath;
}
