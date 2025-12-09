package vaultWeb.dtos.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

  @NotBlank(message = "Username cannot be blank")
  private String username;

  @NotBlank(message = "Password cannot be blank")
  @Size(min = 8, message = "Password must be at least 8 characters long")
  @Pattern(
          regexp = "^(?=.*[A-Z])(?=.*\\d).*$",
          message = "Password must contain at least one uppercase letter and one digit"
  )
  private String password;
}
