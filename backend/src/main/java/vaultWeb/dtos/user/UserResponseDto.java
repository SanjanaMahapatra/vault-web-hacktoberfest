package vaultWeb.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vaultWeb.models.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private String username;
    private String phoneNumber;
    private String email;
    private String profilePicture;

    public UserResponseDto(User user) {
        this.username = user.getUsername();
        this.profilePicture = user.getProfilePicture();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
    }
}