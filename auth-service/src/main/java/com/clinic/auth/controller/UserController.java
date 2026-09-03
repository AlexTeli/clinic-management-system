package com.clinic.auth.controller;

import com.clinic.auth.dto.UserResponse;
import com.clinic.auth.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public String getCurrentUser(Authentication authentication) {
        return "Logged in as: " + authentication.getName();
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/{id}/promote-to-doctor")
    public UserResponse promoteToDoctor(
            @PathVariable Long id
    ) {
        return userService.promoteToDoctor(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }
}
