package ru.ravel.nginxconfigbuilder.component;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.ravel.nginxconfigbuilder.model.Certificate;
import ru.ravel.nginxconfigbuilder.model.Config;
import ru.ravel.nginxconfigbuilder.service.CertBotService;
import ru.ravel.nginxconfigbuilder.service.CertificateService;
import ru.ravel.nginxconfigbuilder.service.NginxConfigService;

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

	private final CertificateService certificateService;
	private final NginxConfigService nginxConfigService;
	private final CertBotService certBotService;


	@Override
	public void run(String... args) {
		nginxConfigService.getConfigInfo()
				.stream()
				.filter(config -> config.getDomain() != null)
				.filter(config -> !config.getDomain().isEmpty())
				.forEach(config -> {
					boolean pathExist = new File(config.getCertificates().getPath()).exists();
					if (pathExist) {
						Certificate certificate = certificateService.getCertificate(config.getCertificates().getPath());
						if (certificate.getNotAfter().isBefore(ZonedDateTime.now())) {
							certBotService.renewCertificate();
						}
					} else {
						certBotService.issueCertificate(config.getDomain());
					}
				});
	}

}