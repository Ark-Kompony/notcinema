package kg.cinema.controller;

import kg.cinema.dto.request.LoginRequest;
import kg.cinema.dto.request.RegisterRequest;
import kg.cinema.dto.response.AuthResponse;
import kg.cinema.dto.response.UserResponse;
import kg.cinema.entity.User;
import kg.cinema.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register new user
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(
            request.getEmail(),
            request.getPhone(),
            request.getPassword(),
            request.getFirstName(),
            request.getLastName()
        );

        return ResponseEntity.ok(UserResponse.fromUser(user));
    }

    /**
     * Login user
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Map<String, String> tokens = authService.login(request.getEmail(), request.getPassword());
        User user = authService.getCurrentUser();

        AuthResponse response = new AuthResponse(
            tokens.get("accessToken"),
            tokens.get("refreshToken"),
            tokens.get("tokenType"),
            UserResponse.fromUser(user)
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        Map<String, String> tokens = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(tokens);
    }

    /**
     * Get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        User user = authService.getCurrentUser();
        return ResponseEntity.ok(UserResponse.fromUser(user));
    }
}
