package meety.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import meety.dtos.UserDto;
import meety.models.User;
import meety.services.UserService;
import meety.services.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "User Controller", description = "Handles registration and login of users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = """
                    Accepts a JSON object containing username and plaintext password.
                    The password is hashed using BCrypt (via Spring Security's PasswordEncoder) before being persisted.
                    The new user is assigned the default role 'User'."""
    )
    public ResponseEntity<String> register(@RequestBody UserDto user) {
        userService.registerUser(new User(user));
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user and return JWT token",
            description = """
                    Accepts a username and plaintext password.
                    If credentials are valid, a JWT (JSON Web Token) is returned in the response body.
                    The token includes the username and user role as claims and is signed using HS256 (HMAC with SHA-256).
                    Token validity is 1 hour.
                    
                    Security process:
                    - Uses Spring Security's AuthenticationManager to validate credentials.
                    - On success, the user details are fetched and a JWT is generated via JwtUtil.
                    - The token can be used in the 'Authorization' header for protected endpoints.
                    """
    )
    public ResponseEntity<?> login(@RequestBody UserDto user) {
        String token = authService.login(user.getUsername(), user.getPassword());
        return ResponseEntity.ok(Map.of("token", token));
    }

}
