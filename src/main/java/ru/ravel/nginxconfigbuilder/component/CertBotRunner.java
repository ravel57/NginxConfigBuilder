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
				.filter(config -> config.getDomain() != null && !config.getDomain().isEmpty())
				.filter(config -> Boolean.TRUE.equals(config.getIsSsl()))
				.filter(config -> config.getCertificates() != null
						&& config.getCertificates().getPath() != null
						&& !config.getCertificates().getPath().isBlank())
				.forEach(config -> {
					String path = config.getCertificates().getPath();
					boolean pathExist = new File(path).exists();
					if (pathExist) {
						Certificate certificate = certificateService.getCertificate(path);
						if (certificate != null
								&& certificate.getNotAfter() != null
								&& certificate.getNotAfter().isBefore(ZonedDateTime.now())) {
							certBotService.renewCertificates();
						}
					} else {
						certBotService.issueCertificate(config.getDomain());
					}
				});
	}

}