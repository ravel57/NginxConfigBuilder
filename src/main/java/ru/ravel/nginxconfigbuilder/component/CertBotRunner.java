package ru.ravel.nginxconfigbuilder.component;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.ravel.nginxconfigbuilder.model.Certificate;
import ru.ravel.nginxconfigbuilder.model.Config;
import ru.ravel.nginxconfigbuilder.service.ConfigsService;
import ru.ravel.nginxconfigbuilder.service.NginxConfigParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


@Component
@RequiredArgsConstructor
public class CertBotRunner implements CommandLineRunner {

	private final ConfigsService configsService;
	private final NginxConfigParser nginxConfigParser;

	@Value("${certificate.email}")
	private String email;

	@Override
	public void run(String... args) throws Exception {
		List<Config> configsWithDomains = nginxConfigParser.getConfigInfo()
				.stream()
				.filter(config -> config.getDomainName() != null)
				.filter(config -> !config.getDomainName().isEmpty())
				.toList();
		for (Config config : configsWithDomains) {
			String[] params = {"certbot", "certonly", "--standalone", "-d", config.getDomainName(), "--non-interactive", "--agree-tos", "--email", email};
			boolean pathExist = new File(config.getCertificates().getPath()).exists();
			if (pathExist) {
				try {
					Certificate certificate = configsService.getCertificate(config.getCertificates().getPath());
					if (certificate.getNotAfter().isBefore(ZonedDateTime.now())) {
						params = new String[]{"certbot", "renew", "--non-interactive", "--quiet"};
						System.out.println(executeProcess(params).stream().map(Arrays::asList).flatMap(Collection::stream).toList());
					}
				} catch (Exception e) {
					System.out.println(executeProcess(params).stream().map(Arrays::asList).flatMap(Collection::stream).toList());
				}
			} else {
				System.out.println(executeProcess(params).stream().map(Arrays::asList).flatMap(Collection::stream).toList());
			}
		}
	}


	private static ArrayList<String[]> executeProcess(String[] processParams) throws IOException, InterruptedException {
		Process process = new ProcessBuilder().command(processParams).start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		ArrayList<String[]> output = (ArrayList<String[]>) reader.lines()
				.map(line -> line.replaceAll("\\s+", " ").split(" "))
				.toList();
		process.waitFor();
		return output;
	}

}