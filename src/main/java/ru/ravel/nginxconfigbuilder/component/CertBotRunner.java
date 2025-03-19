package ru.ravel.nginxconfigbuilder.component;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.ravel.nginxconfigbuilder.model.Certificate;
import ru.ravel.nginxconfigbuilder.service.CertBotService;
import ru.ravel.nginxconfigbuilder.service.CertificateService;
import ru.ravel.nginxconfigbuilder.service.NginxConfigService;

import java.io.File;
import java.time.ZonedDateTime;


@Component
@RequiredArgsConstructor
public class CertBotRunner implements CommandLineRunner {

	private final CertificateService certificateService;
	private final NginxConfigService nginxConfigService;
	private final CertBotService certBotService;


	@Override
	public void run(String... args) {
		nginxConfigService.getConfigs()
				.stream()
				.filter(config -> config.getDomain() != null)
				.filter(config -> !config.getDomain().isEmpty())
				.forEach(config -> {
					boolean pathExist = new File(config.getCertificates().getPath()).exists();
					if (pathExist) {
						Certificate certificate = certificateService.getCertificate(config.getCertificates().getPath());
						if (certificate.getNotAfter().isBefore(ZonedDateTime.now())) {
							certBotService.renewCertificates();
						}
					} else {
						certBotService.issueCertificate(config.getDomain());
					}
				});
	}

}