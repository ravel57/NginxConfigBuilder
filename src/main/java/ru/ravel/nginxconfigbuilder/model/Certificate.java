package ru.ravel.nginxconfigbuilder.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Certificate {
	private String principal;
	private ZonedDateTime notBefore;
	private ZonedDateTime notAfter;
	private String path;
}
