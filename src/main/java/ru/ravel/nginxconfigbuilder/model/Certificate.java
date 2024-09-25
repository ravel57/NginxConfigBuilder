package ru.ravel.nginxconfigbuilder.model;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;


@Data
@Builder
public class Certificate {
	private String principal;
	private ZonedDateTime notBefore;
	private ZonedDateTime notAfter;
	private String path;
}
