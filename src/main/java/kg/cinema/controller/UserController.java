package kg.cinema.controller;

import kg.cinema.entity.User;
import kg.cinema.service.AuthService;
import kg.cinema.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile() {
        User user = authService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    /**
     * Update user profile
     */
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody User userDetails) {
        Long userId = authService.getCurrentUserId();
        User updatedUser = userService.updateUser(userId, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody Map<String, String> request) {
        Long userId = authService.getCurrentUserId();
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        userService.changePassword(userId, oldPassword, newPassword);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    /**
     * Get user by ID (for admins)
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        // Only allow if requesting own profile or is admin
        Long currentUserId = authService.getCurrentUserId();
        if (!currentUserId.equals(id) && !authService.isAdmin()) {
            throw new RuntimeException("Access denied");
        }

        return ResponseEntity.ok(userService.getUserById(id));
    }
}
