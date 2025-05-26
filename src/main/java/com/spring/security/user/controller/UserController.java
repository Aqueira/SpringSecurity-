package com.spring.security.user.controller;

import com.spring.security.exception.exceptions.NotFoundException;
import com.spring.security.user.entity.User;
import com.spring.security.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@PatchMapping
	@Transactional
	@PreAuthorize("hasRole('ROLE_USER') and #user.id == authentication.principal.id or hasRole('ADMIN')")
	public ResponseEntity<?> update(@RequestBody User user) {
		User existingUser = userRepository
				.findById(user.getId())
				.orElseThrow(() -> new NotFoundException("User with this id does not exist!"));
		existingUser.setUsername(user.getUsername());
		existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
		return ResponseEntity.ok(userRepository.save(existingUser));
	}

	@DeleteMapping
	@Transactional
	@PreAuthorize("hasRole('ROLE_USER') and #id == authentication.principal.id or hasRole('ADMIN')")
	public void delete(@RequestParam Long id) {
		if(!userRepository.existsById(id)) {
			throw new NotFoundException("User with this id does not exist!");
		}
		userRepository.deleteById(id);
	}
}
