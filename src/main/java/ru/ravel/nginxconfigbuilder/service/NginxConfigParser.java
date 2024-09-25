package ru.ravel.nginxconfigbuilder.service;

import com.github.odiszapc.nginxparser.NgxBlock;
import com.github.odiszapc.nginxparser.NgxConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.ravel.nginxconfigbuilder.model.Config;
import ru.ravel.nginxconfigbuilder.model.Location;
import ru.ravel.nginxconfigbuilder.model.Upstream;

import java.io.IOException;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class NginxConfigParser {

	private final ConfigsService configsService;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	public List<Config> getConfigInfo() {
		try {
			NgxConfig conf = NgxConfig.read("C:\\Users\\petr\\Desktop\\nginx.conf");
			List<Upstream> upstreams = conf.findAll(NgxConfig.BLOCK, "http", "upstream").stream()
					.map(entry -> (NgxBlock) entry)
					.map(entry -> Upstream.builder()
							.name(((List<?>) entry.getTokens()).getLast().toString())
							.server(entry.findParam("server").getValue())
							.build())
					.toList();
			List<Config> configs = conf.findAll(NgxConfig.BLOCK, "http", "server").stream()
					.map(entry -> (NgxBlock) entry)
					.map(entry -> Config.builder()
							.serverName(Objects.requireNonNullElse(entry.findParam("server_name"), entry).getValue())
							.port(entry.findParam("listen").getValue())
							.location(Location.builder()
									.proxyPass(entry.findAll(NgxConfig.BLOCK, "location").stream()
											.map(subEntry -> (NgxBlock) subEntry)
											.filter(subEntry -> subEntry.findParam("proxy_pass") != null)
											.map(subEntry -> subEntry.findParam("proxy_pass").getValue())
											.toList())
									.build())
							.certificates(configsService.getCertificate(Objects.requireNonNullElse(entry.findParam("ssl_certificate"), entry).getValue()))
							.certificatesKeyPath(Objects.requireNonNullElse(entry.findParam("ssl_certificate_key"), entry).getValue())
							.build())
					.toList();
			configs.stream()
					.filter(entry -> !entry.getLocation().getProxyPass().isEmpty())
					.forEach(entry -> {
						Upstream upstream = upstreams.stream()
								.filter(it -> it.getName().equals(entry.getLocation().getProxyPass().getFirst().replace("http://", "")))
								.findFirst()
								.orElse(new Upstream());
						entry.getLocation().setUpstream(upstream.getServer());
					});
			return configs;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
}