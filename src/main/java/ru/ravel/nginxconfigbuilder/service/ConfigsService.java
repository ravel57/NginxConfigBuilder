package ru.ravel.nginxconfigbuilder.service;

import org.springframework.stereotype.Service;
import ru.ravel.nginxconfigbuilder.model.Certificate;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.ZoneId;

@Service
public class ConfigsService {

	public Certificate getCertificate(String certPath) {
		if (certPath == null || certPath.isEmpty()) return null;
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			InputStream inputStream = new FileInputStream(certPath);
			X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(inputStream);
			inputStream.close();
			return Certificate.builder()
					.principal(certificate.getSubjectX500Principal().getName())
					.notBefore(certificate.getNotBefore().toInstant().atZone(ZoneId.systemDefault()))
					.notAfter(certificate.getNotAfter().toInstant().atZone(ZoneId.systemDefault()))
					.path(certPath)
					.build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {

	}
}