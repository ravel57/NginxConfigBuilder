package ru.ravel.nginxconfigbuilder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class NginxConfigBuilderApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(NginxConfigBuilderApplication.class, args);
		Runtime.getRuntime().exec(new String[]{"nginx"});
	}

}
