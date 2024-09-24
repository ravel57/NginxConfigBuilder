package ru.ravel.nginxconfigbuilder.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	@Value("${credentials.username}")
	String username;

	@Value("${credentials.password}")
	String password;


	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();
		if (Objects.equals(username, this.username) && Objects.equals(password, this.password)) {
			List<GrantedAuthority> authorities = new ArrayList<>();
			UserDetails userDetails = new User(username, password, authorities);
			return new UsernamePasswordAuthenticationToken(userDetails, password, authorities);
		} else {
			return null;
		}
	}


	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

}