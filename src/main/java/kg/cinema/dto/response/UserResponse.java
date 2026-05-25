package kg.cinema.dto.response;

import kg.cinema.entity.User;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String avatarUrl;
    private String role;
    private Boolean emailVerified;

    public static UserResponse fromUser(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setBirthDate(user.getBirthDate());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setRole(user.getRole().name());
        response.setEmailVerified(user.getEmailVerified());
        return response;
    }
}
