package vaultWeb.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private String username;
    private String password;

    @Pattern(regexp = "^[+]?[0-9][10-15]$", message = "Invalid Phone Number Format")
    private String phoneNumber;

    @Email(message = "Invalid Email format")
    private String email;
    private String profilePicture;
}
