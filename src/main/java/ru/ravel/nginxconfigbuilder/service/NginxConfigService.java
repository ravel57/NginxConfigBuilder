package ru.ravel.nginxconfigbuilder.service;

import com.github.odiszapc.nginxparser.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ravel.nginxconfigbuilder.model.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class NginxConfigService {

	private final CertificateService certificateService;

	@Value("${nginx.config-path}")
	private String configPath;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	public List<Config> getConfigInfo() {
		try {
			NgxConfig conf = NgxConfig.read(configPath);
			List<Upstream> upstreams = conf.findAll(NgxConfig.BLOCK, "http", "upstream").stream()
					.map(entry -> (NgxBlock) entry)
					.map(entry -> Upstream.builder()
							.name(((List<?>) entry.getTokens()).getLast().toString())
							.server(entry.findParam("server").getValue())
							.build())
					.toList();
			List<Config> configs = conf.findAll(NgxConfig.BLOCK, "http", "server").stream()
					.map(entry -> (NgxBlock) entry)
					.map(entry -> {
						var port = entry.findParam("listen").getValue().split(" ");
						var locationBlocks = entry.findAll(NgxConfig.BLOCK, "location");
						var locationPath = locationBlocks.stream()
								.map(it -> (NgxBlock) it)
								.map(it -> (List<?>) it.getTokens())
								.map(it -> it.get(1))
								.map(Object::toString)
								.filter(string -> string.startsWith("/"))
								.toList();
						var proxyPass = locationBlocks.stream()
								.map(subEntry -> (NgxBlock) subEntry)
								.map(subEntry -> subEntry.findParam("proxy_pass"))
								.filter(Objects::nonNull)
								.map(NgxAbstractEntry::getValue)
								.toList();
						var proxySetHeaders = locationBlocks.stream()
								.map(subEntry -> (NgxBlock) subEntry)
								.map(subEntry -> subEntry.findAll(NgxConfig.PARAM, "proxy_set_header"))
								.filter(subEntry -> !subEntry.isEmpty())
								.map(subEntry -> subEntry.stream()
										.map(it -> (NgxParam) it)
										.map(it -> it.getTokens().stream()
												.map(NgxToken::getToken)
												.toList())
										.map(it -> new Pair(it.get(1), it.get(2)))
										.toList())
								.flatMap(Collection::stream)
								.toList();
						var locations = proxyPass.stream()
								.map(subEntry -> Location.builder()
										.location(getLocation(locationPath, subEntry))
										.proxyPass(subEntry)
										.proxySetHeaders(proxySetHeaders)
										.build())
								.toList();
						return Config.builder()
								.domain(Objects.requireNonNullElse(entry.findParam("server_name"), entry).getValue())
								.port(Integer.decode(port[0]))
								.isSsl(port.length > 1 && "ssl".equals(port[1]))
								.location(locations)
								.upstream(null)
								.certificates(getCertificate(entry))
								.certificatesKeyPath(Objects.requireNonNullElse(entry.findParam("ssl_certificate_key"), entry).getValue())
								.build();
					})
					.toList();
			configs.stream()
					.filter(entry -> entry.getLocation() != null)
					.forEach(entry -> {
						Upstream upstream = upstreams.stream()
								.filter(it -> entry.getLocation().stream().anyMatch(el -> el.getProxyPass().endsWith(it.getName())))
								.findFirst()
								.orElse(Upstream.builder().build());
						entry.setUpstream(upstream);
					});
			return configs;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}


	private String getLocation(List<String> locationPath, String subEntry) {
		return locationPath.stream()
				.map(el -> el.replace("/", ""))
				.filter(el -> !el.isEmpty())
				.filter(subEntry::endsWith)
				.map("/%s"::formatted)
				.findFirst()
				.orElse("/");
	}


	private Certificate getCertificate(NgxBlock entry) {
		NgxParam sslCertificate = entry.findParam("ssl_certificate");
		if (sslCertificate != null) {
			if (new File(sslCertificate.getValue()).exists()) {
				return certificateService.getCertificate(sslCertificate.getValue());
			} else {
				return Certificate.builder().path(sslCertificate.getValue()).build();
			}
		}
		return null;
	}
}