package kg.cinema.service;

import kg.cinema.entity.User;
import kg.cinema.repository.UserRepository;
import kg.cinema.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final LoyaltyService loyaltyService;

    /**
     * Register new user
     */
    @Transactional
    public User register(String email, String phone, String password, String firstName, String lastName) {
        // Validate email and phone are unique
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("Phone already exists");
        }

        // Create user
        User user = new User();
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(User.UserRole.USER);
        user.setIsActive(true);
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);

        // Create loyalty account
        loyaltyService.createLoyaltyAccount(savedUser.getId());

        return savedUser;
    }

    /**
     * Login user and generate JWT tokens
     */
    public Map<String, String> login(String email, String password) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate tokens
        String accessToken = tokenProvider.generateToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        tokens.put("tokenType", "Bearer");

        return tokens;
    }

    /**
     * Refresh access token
     */
    public Map<String, String> refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        Long userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = tokenProvider.generateToken(user);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("tokenType", "Bearer");

        return tokens;
    }

    /**
     * Get current authenticated user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Get current user ID
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Check if current user has role
     */
    public boolean hasRole(User.UserRole role) {
        User user = getCurrentUser();
        return user.getRole() == role;
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return hasRole(User.UserRole.ADMIN);
    }

    /**
     * Check if current user is manager or admin
     */
    public boolean isManagerOrAdmin() {
        User user = getCurrentUser();
        return user.getRole() == User.UserRole.MANAGER || user.getRole() == User.UserRole.ADMIN;
    }
}
