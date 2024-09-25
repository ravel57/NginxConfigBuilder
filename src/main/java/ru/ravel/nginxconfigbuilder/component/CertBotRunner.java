package ru.ravel.nginxconfigbuilder.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


@Component
public class CertBotRunner implements CommandLineRunner {

	@Value("${credentials.email}")
	String email;

	@Value("${credentials.domain}")
	String domain;

	@Override
	public void run(String... args) throws Exception {
		String[] strings = {"certbot", "certonly", "--non-interactive", "--agree-tos", "--email", email, "--webroot", "-w", "/var/www/html", "-d", domain};
		System.out.println(executeProcess(strings).stream().map(Arrays::asList).flatMap(Collection::stream).toList());
	}


	private static ArrayList<String[]> executeProcess(String[] processParams) throws IOException, InterruptedException {
		var process = new ProcessBuilder().command(processParams).start();
		var output = new ArrayList<String[]>();
		var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			output.add(line.replaceAll("\\s+", " ").split(" "));
		}
		process.waitFor();
		return output;
	}

}