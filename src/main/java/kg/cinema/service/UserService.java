package kg.cinema.service;

import kg.cinema.entity.User;
import kg.cinema.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoyaltyService loyaltyService;

    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public User getUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
            .orElseThrow(() -> new RuntimeException("User not found with phone: " + phone));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Transactional
    public User createUser(User user) {
        // Validate email and phone are unique
        if (existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (existsByPhone(user.getPhone())) {
            throw new RuntimeException("Phone already exists");
        }

        // Hash password
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        // Set default role if not specified
        if (user.getRole() == null) {
            user.setRole(User.UserRole.USER);
        }

        User savedUser = userRepository.save(user);

        // Create loyalty account for new user
        loyaltyService.createLoyaltyAccount(savedUser.getId());

        return savedUser;
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);

        if (userDetails.getFirstName() != null) {
            user.setFirstName(userDetails.getFirstName());
        }
        if (userDetails.getLastName() != null) {
            user.setLastName(userDetails.getLastName());
        }
        if (userDetails.getBirthDate() != null) {
            user.setBirthDate(userDetails.getBirthDate());
        }
        if (userDetails.getAvatarUrl() != null) {
            user.setAvatarUrl(userDetails.getAvatarUrl());
        }

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void updateRole(Long userId, User.UserRole newRole) {
        User user = getUserById(userId);
        user.setRole(newRole);
        userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        User user = getUserById(userId);
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(Long userId) {
        User user = getUserById(userId);
        user.setIsActive(true);
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
