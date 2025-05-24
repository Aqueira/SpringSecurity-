package com.spring.security.auth.service;

import com.spring.security.jwt.JWTService;
import com.spring.security.role.entity.Role;
import com.spring.security.role.repository.RoleRepository;
import com.spring.security.user.entity.User;
import com.spring.security.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthorizationService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JWTService jwtService;

	@Value("${user.roles.defaultRole}")
	private String defaultRole;

	@Transactional
	public void register(User user) {
		userRepository.save(
				User.builder()
						.username(user.getUsername())
						.password(passwordEncoder.encode(user.getPassword()))
						.roles(Set.of(roleRepository.findByName(defaultRole)
								.orElseGet(() -> roleRepository.save(
										Role.builder()
												.name(defaultRole)
												.build())
										)
								)
						)
						.build());
	}

	@Transactional(readOnly = true)
	public String login(User user) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
		return userRepository.findByUsername(user.getUsername())
				.map(jwtService::generateToken)
				.orElseThrow(RuntimeException::new);
	}

	
	@Transactional(readOnly = true)
	public String refreshAccessToken(String token) {
		String username = jwtService.extractUsername(token);

		if (jwtService.isTokenValid(token, username)) {
			return userRepository.findByUsername(username)
					.map(jwtService::generateRefreshToken)
					.orElseThrow(() -> new RuntimeException("User not found"));
		} else {
			throw new RuntimeException("Invalid refresh token");
		}
	}

}
