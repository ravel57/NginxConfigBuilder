package ru.ravel.nginxconfigbuilder.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class Location {
	private String location;
	private String proxyPass;
	private List<Pair> proxySetHeaders;
}
