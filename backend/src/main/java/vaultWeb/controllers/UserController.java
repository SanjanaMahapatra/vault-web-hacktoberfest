package vaultWeb.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vaultWeb.dtos.user.UserDto;
import vaultWeb.dtos.user.UserResponseDto;
import vaultWeb.models.User;
import vaultWeb.services.UserService;
import vaultWeb.services.auth.AuthService;
import vaultWeb.services.auth.LoginResult;
import vaultWeb.services.auth.RefreshTokenService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "User Controller", description = "Handles registration and login of users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final AuthService authService;
  private final RefreshTokenService refreshTokenService;

  @PostMapping("/register")
  @Operation(
      summary = "Register a new user",
      description =
          """
                            Accepts a JSON object containing username and plaintext password.
                            The password is hashed using BCrypt (via Spring Security's PasswordEncoder) before being persisted.
                            The new user is assigned the default role 'User'.""")
  public ResponseEntity<String> register(@Valid @RequestBody UserDto user) {
    userService.registerUser(new User(user));
    return ResponseEntity.ok("User registered successfully");
  }

  @Operation(
      summary = "Authenticate user and issue access & refresh tokens",
      description =
          """
                    Authenticates a user using username and password.

                    On successful authentication:
                    - A short-lived access token (JWT) is returned in the response body.
                    - A long-lived refresh token (JWT) is issued and stored in an HttpOnly, secure cookie.

                    Security details:
                    - Credentials are validated using Spring Security's AuthenticationManager.
                    - Access tokens are stateless and short-lived.
                    - Refresh tokens are JWTs containing a unique identifier (jti), stored hashed in the database.
                    - Refresh tokens are rotated on use and can be revoked server-side.

                    The access token should be sent in the Authorization header for protected endpoints.
                    """)
  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody UserDto user, HttpServletResponse response) {
    LoginResult res = authService.login(user.getUsername(), user.getPassword());
    refreshTokenService.create(res.user(), response);
    return ResponseEntity.ok(Map.of("token", res.accessToken()));
  }

  @Operation(
      summary = "Refresh access token using refresh token rotation",
      description =
          """
                    Issues a new access token using a valid refresh token provided via an HttpOnly cookie.

                    Refresh workflow:
                    - The refresh token JWT is validated (signature and expiration).
                    - The token identifier (jti) is extracted from the JWT.
                    - The corresponding refresh token record is looked up in the database.
                    - The stored hash is verified and the token is revoked to prevent reuse.
                    - A new refresh token is generated, stored, and sent as a secure cookie.
                    - A new short-lived access token is returned in the response body.

                    Security guarantees:
                    - Refresh tokens are rotated on every successful refresh.
                    - Revoked or reused refresh tokens are rejected.
                    - Refresh tokens are never stored in plaintext.

                    Returns 401 if the refresh token is missing, invalid, expired, revoked, or reused.
                    """)
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Access token refreshed successfully"),
    @ApiResponse(responseCode = "401", description = "Invalid, expired, or revoked refresh token")
  })
  @PostMapping("/refresh")
  public ResponseEntity<?> refresh(
      @CookieValue(name = "refresh_token", required = false) String refreshToken,
      HttpServletResponse response) {
    if (refreshToken == null) {
      return ResponseEntity.status(401).build();
    }

    return authService.refresh(refreshToken, response);
  }

  @PostMapping("/logout")
  @Operation(
      summary = "Logout user and revoke refresh token",
      description =
          """
                    Logs out the current session by revoking the active refresh token and
                    deleting the refresh token cookie.

                    Logout behavior:
                    - If a refresh token cookie is present, its token identifier (jti) is extracted.
                    - The corresponding refresh token is revoked in the database.
                    - The refresh token cookie is deleted from the client.

                    Security notes:
                    - Revoking the refresh token ensures it cannot be reused, even if previously leaked.
                    - Cookie deletion alone is not relied upon for logout.

                    This operation logs out the current device/session only.
                    """)
  @ApiResponses({@ApiResponse(responseCode = "200", description = "Logged out successfully")})
  public ResponseEntity<Void> logout(
      @CookieValue(name = "refresh_token", required = false) String refreshToken,
      HttpServletResponse response) {
    authService.logout(refreshToken, response);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/check-username")
  @Operation(
      summary = "Check if username already exists",
      description = "Returns true if the username is already taken, false otherwise.")
  public ResponseEntity<Map<String, Boolean>> checkUsernameExists(@RequestParam String username) {
    boolean exists = userService.usernameExists(username);
    return ResponseEntity.ok(Map.of("exists", exists));
  }

  @GetMapping("/users")
  @Operation(
      summary = "Get all users",
      description =
          "Returns a list of all users with basic info (e.g., usernames) for displaying in the chat list.")
  public ResponseEntity<List<UserResponseDto>> getAllUsers() {
    List<UserResponseDto> users =
        userService.getAllUsers().stream().map(UserResponseDto::new).toList();
    return ResponseEntity.ok(users);
  }
}
