package ru.ravel.nginxconfigbuilder.service;

import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.ZoneId;

@Service
public class ConfigsService {
	public static void main(String[] args) {
		try {
			String certPath = "C:\\Users\\petr\\Desktop\\fullchain1.pem";
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			InputStream inputStream = new FileInputStream(certPath);
			X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(inputStream);
			inputStream.close();
			System.out.println("Владелец: " + certificate.getSubjectX500Principal().getName());
			System.out.println("Срок действия с: " + certificate.getNotBefore().toInstant().atZone(ZoneId.systemDefault()));
			System.out.println("Срок действия до: " + certificate.getNotAfter().toInstant().atZone(ZoneId.systemDefault()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}