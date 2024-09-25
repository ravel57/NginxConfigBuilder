package ru.ravel.nginxconfigbuilder.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class CertBotService {

	@Value("${certificate.email}")
	private String email;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	public void issueCertificate(String domain) {
		String[] params = {"certbot", "certonly", "--standalone", "-d", domain, "--non-interactive", "--agree-tos", "--email", email};
		executeProcess(params);
	}


	public void renewCertificate() {
		String[] params = {"certbot", "renew", "--non-interactive", "--quiet"};
		executeProcess(params);
	}


	private void executeProcess(String[] processParams) {
		try {
			Process process = new ProcessBuilder().command(processParams).start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			List<String[]> output = new ArrayList<>();
			while ((line = reader.readLine()) != null) {
				output.add(line.replaceAll("\\s+", " ").split(" "));
			}
			process.waitFor();
			logger.info(output.stream().map(Arrays::asList).flatMap(Collection::stream).collect(Collectors.joining(" ")));
		} catch (IOException | InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
