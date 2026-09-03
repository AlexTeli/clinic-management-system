package com.clinic.auth.service;

import com.clinic.auth.dto.UserResponse;
import com.clinic.auth.entity.User;
import com.clinic.auth.exception.InvalidUserOperationException;
import com.clinic.auth.exception.UserNotFoundException;
import com.clinic.auth.repository.UserRepository;
import com.clinic.auth.role.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.clinic.auth.exception.EmailAlreadyExistsException;
import com.clinic.auth.exception.UsernameAlreadyExistsException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String username, String email, String password) {

        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(password);

        User user = new User(
                username,
                email,
                encodedPassword,
                Role.USER
        );

        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole()
                ))
                .toList();
    }

    public UserResponse promoteToDoctor(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + userId
                        )
                );

        if (user.getRole() == Role.DOCTOR) {
            throw new InvalidUserOperationException(
                    "User is already a doctor"
            );
        }

        if (user.getRole() == Role.ADMIN) {
            throw new InvalidUserOperationException (
                    "An admin cannot be promoted to doctor"
            );
        }

        user.setRole(Role.DOCTOR);

        User updatedUser = userRepository.save(user);

        return new UserResponse(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getRole()
        );
    }
}