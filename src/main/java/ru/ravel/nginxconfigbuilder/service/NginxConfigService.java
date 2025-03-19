package ru.ravel.nginxconfigbuilder.service;

import com.github.odiszapc.nginxparser.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ravel.nginxconfigbuilder.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@RequiredArgsConstructor
public class NginxConfigService {

	private final CertificateService certificateService;
	private final CertBotService certBotService;


	@Value("${nginx.config-path}")
	private String configPath;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	public List<Config> getConfigs() {
		try {
			NgxConfig conf = NgxConfig.read(configPath);
			List<Upstream> upstreams = conf.findAll(NgxConfig.BLOCK, "http", "upstream").stream()
					.map(entry -> (NgxBlock) entry)
					.map(entry -> {
						String[] split = entry.findParam("server").getValue().split(":");
						return Upstream.builder()
								.name(((List<?>) entry.getTokens()).getLast().toString())
								.host(split[0])
								.port(Integer.valueOf(split[1]))
								.build();
					})
					.toList();
			return conf.findAll(NgxConfig.BLOCK, "http", "server").stream()
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
						var upstream = upstreams.stream()
								.filter(u -> locations.stream().anyMatch(l -> l.getProxyPass().endsWith(u.getName())))
								.findFirst()
								.orElse(Upstream.builder().build());
						return Config.builder()
								.domain(Objects.requireNonNullElse(entry.findParam("server_name"), entry).getValue())
								.port(Integer.decode(port[0]))
								.isSsl(port.length > 1 && "ssl".equals(port[1]))
								.location(locations)
								.upstream(upstream)
								.certificates(getCertificate(entry))
								.certificatesKeyPath(Objects.requireNonNullElse(entry.findParam("ssl_certificate_key"), entry).getValue())
								.build();
					})
					.toList();
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
			return new File(sslCertificate.getValue()).exists()
					? certificateService.getCertificate(sslCertificate.getValue())
					: Certificate.builder().path(sslCertificate.getValue()).build();
		}
		return null;
	}


	public Config saveConfig(Config config) {
		if (!NetworkService.pingAddress(config.getDomain())) {
			return null;
		}
		Pattern pattern = Pattern.compile("^([^/]+?)\\.[a-zA-Z]{2,}(/.*)?$");
		Matcher matcher = pattern.matcher(config.getDomain());
		String proxyPass = matcher.matches() ? matcher.group(1) : "";
		try {
			NgxConfig conf = NgxConfig.read(configPath);
			NgxBlock server = new NgxBlock();
			server.addValue("server");

			NgxBlock ngxBlockLocation = new NgxBlock();
			ngxBlockLocation.addValue("location");
			ngxBlockLocation.addValue("/");

			NgxParam ngxParam = new NgxParam();
			ngxParam.addValue("listen %s%s".formatted(config.getPort(), config.getIsSsl() ? " ssl" : ""));
			server.addEntry(ngxParam);

			ngxParam = new NgxParam();
			ngxParam.addValue("server_name %s".formatted(config.getDomain()));
			server.addEntry(ngxParam);

			if (config.getIsSsl()) {
				certBotService.issueCertificate(config.getDomain());

				ngxParam = new NgxParam();
				ngxParam.addValue("ssl_certificate /etc/letsencrypt/live/%s/fullchain.pem".formatted(config.getDomain()));
				server.addEntry(ngxParam);

				ngxParam = new NgxParam();
				ngxParam.addValue("ssl_certificate_key /etc/letsencrypt/live/%s/privkey.pem".formatted(config.getDomain()));
				server.addEntry(ngxParam);

				ngxParam = new NgxParam();
				ngxParam.addValue("ssl_protocols TLSv1 TLSv1.1 TLSv1.2");
				server.addEntry(ngxParam);
			}

			ngxParam = new NgxParam();
			ngxParam.addValue("add_header Strict-Transport-Security \"max-age=31536000\"");
			server.addEntry(ngxParam);

			NgxBlock ngxBlockUpstream = new NgxBlock();
			ngxBlockUpstream.addValue("upstream");
			ngxBlockUpstream.addValue(proxyPass);
			ngxParam = new NgxParam();
			ngxParam.addValue("server %s:%s".formatted(config.getUpstream().getHost(), config.getUpstream().getPort()));
			ngxBlockUpstream.addEntry(ngxParam);

			ngxParam = new NgxParam();
			ngxParam.addValue("proxy_pass http://%s".formatted(proxyPass));
			ngxBlockLocation.addEntry(ngxParam);

			ngxParam = new NgxParam();
			ngxParam.addValue("proxy_set_header Host $host");
			ngxBlockLocation.addEntry(ngxParam);

			ngxParam = new NgxParam();
			ngxParam.addValue("proxy_http_version 1.1");
			ngxBlockLocation.addEntry(ngxParam);

			ngxParam = new NgxParam();
			ngxParam.addValue("proxy_set_header Upgrade $http_upgrade");
			ngxBlockLocation.addEntry(ngxParam);

			ngxParam = new NgxParam();
			ngxParam.addValue("proxy_set_header Connection \"upgrade\"");
			ngxBlockLocation.addEntry(ngxParam);

			server.addEntry(ngxBlockLocation);
			conf.findBlock("http").addEntry(ngxBlockUpstream);
			conf.findBlock("http").addEntry(server);
			try (FileWriter fileWriter = new FileWriter(configPath)) {
				fileWriter.write(new NgxDumper(conf).dump());
			}
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(new String[]{"pkill", "-f", "nginx"}).waitFor();
			runtime.exec(new String[]{"nginx"});
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		return null;
	}


	public Config deleteConfig(String domain) {
		Pattern pattern = Pattern.compile("^([^/]+?)\\.[a-zA-Z]{2,}(/.*)?$");
		Matcher matcher = pattern.matcher(domain);
		String proxyPass = matcher.matches() ? matcher.group(1) : "";
		try {
			NgxConfig conf = NgxConfig.read(configPath);
			Collection<NgxEntry> entries = conf.getEntries().stream()
					.filter(it -> it instanceof NgxBlock)
					.map(it -> (NgxBlock) it)
					.filter(it -> it.getTokens().stream().anyMatch(token -> token.toString().equals("http")))
					.findFirst()
					.orElseThrow()
					.getEntries();
			NgxBlock upstream = entries.stream()
					.filter(entry -> entry instanceof NgxBlock)
					.map(it -> (NgxBlock) it)
					.filter(entry -> entry.getTokens().stream()
							.anyMatch(it -> it.toString().contains(proxyPass)))
					.findFirst()
					.orElseThrow();
			entries.remove(upstream);
			NgxBlock server = entries.stream()
					.filter(entry -> entry instanceof NgxBlock)
					.map(it -> (NgxBlock) it)
					.filter(entry -> entry.getTokens().stream()
							.anyMatch(it -> it.toString().equals("server")))
					.filter(entry -> entry.getEntries().stream()
							.anyMatch(it -> it.toString().equals("server_name %s;".formatted(domain))))
					.findFirst()
					.orElseThrow();
			entries.remove(server);
			try (FileWriter fileWriter = new FileWriter(configPath)) {
				fileWriter.write(new NgxDumper(conf).dump());
			}
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(new String[]{"pkill", "-f", "nginx"}).waitFor();
			runtime.exec(new String[]{"nginx"});
			System.out.println();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		return null;
	}


	public Config renewCertificate() {
		certBotService.renewCertificates();
		return null;
	}

}