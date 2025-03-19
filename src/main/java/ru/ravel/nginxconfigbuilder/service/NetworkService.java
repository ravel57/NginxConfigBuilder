package ru.ravel.nginxconfigbuilder.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.InetAddress;

@Service
@RequiredArgsConstructor
public class NetworkService {

	public static boolean pingAddress(String address) {
		 try {
			var inetAddress = InetAddress.getByName(address);
			 return inetAddress.isReachable(3000);
		} catch (Exception e) {
			return false;
		}
	}

}
