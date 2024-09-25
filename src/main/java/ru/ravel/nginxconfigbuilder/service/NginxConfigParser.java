package ru.ravel.nginxconfigbuilder.service;

import com.github.odiszapc.nginxparser.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ravel.nginxconfigbuilder.model.Config;
import ru.ravel.nginxconfigbuilder.model.Location;
import ru.ravel.nginxconfigbuilder.model.Pair;
import ru.ravel.nginxconfigbuilder.model.Upstream;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class NginxConfigParser {

	private final ConfigsService configsService;

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
						var locationPath = entry.findAll(NgxConfig.BLOCK, "location").stream()
								.map(it -> (NgxBlock) it)
								.map(it -> (List<?>) it.getTokens())
								.map(it -> it.get(1))
								.map(Object::toString)
								.filter(string -> string.startsWith("/"))
								.toList();
						var proxyPass = entry.findAll(NgxConfig.BLOCK, "location").stream()
								.map(subEntry -> (NgxBlock) subEntry)
								.filter(subEntry -> subEntry.findParam("proxy_pass") != null)
								.map(subEntry -> subEntry.findParam("proxy_pass").getValue())
								.toList();
						var proxySetHeaders = entry.findAll(NgxConfig.BLOCK, "location").stream()
								.map(subEntry -> (NgxBlock) subEntry)
								.filter(subEntry -> subEntry.findParam("proxy_set_header") != null)
								.map(subEntry -> subEntry.findAll(NgxConfig.PARAM, "proxy_set_header")
										.stream()
										.map(it -> (NgxParam) it)
										.map(it -> it.getTokens().stream().map(NgxToken::getToken).toList())
										.map(it -> new Pair(it.get(1), it.get(2)))
										.toList())
								.flatMap(Collection::stream)
								.toList();
						var locations = proxyPass.stream()
								.map(it -> Location.builder()
										.location(locationPath.stream()
												.map(el -> el.replace("/", ""))
												.filter(el -> !el.isEmpty())
												.filter(it::endsWith)
												.map("/%s"::formatted)
												.findFirst()
												.orElse("/"))
										.proxyPass(it)
										.proxySetHeaders(proxySetHeaders)
										.build())
								.toList();
						return Config.builder()
								.domain(Objects.requireNonNullElse(entry.findParam("server_name"), entry).getValue())
								.port(Integer.decode(port[0]))
								.isSsl(port.length > 1 && "ssl".equals(port[1]))
								.location(locations)
								.certificates(entry.findParam("ssl_certificate") != null && new File(entry.findParam("ssl_certificate").getValue()).exists()
										? configsService.getCertificate(entry.findParam("ssl_certificate").getValue())
										: null)
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
						entry.setUpstream(upstream.getServer());
					});
			return configs;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
}