package com.spring.security.auth.service;

import com.spring.security.exception.exceptions.InvalidationFailed;
import com.spring.security.exception.exceptions.NotFoundException;
import com.spring.security.jwt.JWTService;
import com.spring.security.role.entity.Role;
import com.spring.security.role.repository.RoleRepository;
import com.spring.security.user.entity.User;
import com.spring.security.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorizationService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JWTService jwtService;
	private final EntityManager entityManager;

	private Set<Long> idCachedRoles;

	@EventListener(ApplicationReadyEvent.class)
	@Transactional(readOnly = true)
	public void init() {
		Set<Long> idDefaultRoles = roleRepository.findIdsByDefaultRoleTrue();
		if(idDefaultRoles.isEmpty()) {
			throw new NotFoundException("No default roles found!");
		};
		idCachedRoles = idDefaultRoles;
	} // я далбоеб просто решил протестить как будет работать,
	// запрос теперь один в бд на получение ролей, в памяти данные на пожизненно,
	// и доп запросов  в бд при регистрации на поиск ролей не будет.
	// Хз вроде норм, но тут явно нахуй не надо потому что похуй на RPS в тыщу условный


	@Transactional
	public void register(User user) {
		userRepository.save(
				new User(null,
						user.getUsername(),
						passwordEncoder.encode(user.getPassword()),
						idCachedRoles.stream()
								.map(role_id -> entityManager.getReference(Role.class, role_id)) // делаю прокси для связи между таблицами
								.collect(Collectors.toSet())
				)
		);
	}

	@Transactional(readOnly = true)
	public String login(User user) {
		SecurityContextHolder.getContext()
				.setAuthentication(authenticationManager
						.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()))
				); // добавляет аутентификацию юзера в спрингконтекст

		return userRepository.findByUsername(user.getUsername())
				.map(jwtService::generateToken)
				.orElseThrow(() -> new NotFoundException("User with this username does not exist"));
	}

	
	@Transactional(readOnly = true)
	public String refreshAccessToken(String token) {
		String username = jwtService.extractUsername(token);

		if (jwtService.isTokenValid(token, username)) {
			return userRepository.findByUsername(username)
					.map(jwtService::generateRefreshToken)
					.orElseThrow(() -> new NotFoundException("User with this username does not exist"));
		}
		throw new InvalidationFailed("Token invalidation Failed!");
	}

}
