package vaultWeb.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vaultWeb.dtos.user.UserDto;
import vaultWeb.dtos.user.UserRegisterDTO;
import vaultWeb.dtos.user.UserResponseDto;
import vaultWeb.models.User;
import vaultWeb.services.UserService;
import vaultWeb.services.auth.AuthService;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "User Controller", description = "Handles registration and login of users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = """
                    Accepts a JSON object containing username and plaintext password.
                    The password is hashed using BCrypt (via Spring Security's PasswordEncoder) before being persisted.
                    The new user is assigned the default role 'User'."""
    )
    public ResponseEntity<String> register(@Valid @RequestBody UserRegisterDTO user) {
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

    @GetMapping("/check-username")
    @Operation(
            summary = "Check if username already exists",
            description = "Returns true if the username is already taken, false otherwise."
    )
    public ResponseEntity<Map<String, Boolean>> checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.usernameExists(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/users")
    @Operation(
            summary = "Get all users",
            description = "Returns a list of all users with basic info (e.g., usernames) for displaying in the chat list."
    )
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers()
                .stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/current-user-details")
    @Operation(
            summary = "Get current logged-in user details",
            description = "Returns the details of the currently authenticated user."
    )
    public ResponseEntity<UserResponseDto> getCurrentUserDetails() {
        System.out.println("current user details -> " + authService.getCurrentUser().getUsername());
        UserResponseDto currentUserDetails = userService.getUserDetailsByUsername(authService.getCurrentUser().getUsername());
        return ResponseEntity.ok(currentUserDetails);
    }

    @GetMapping("/check-email")
    @Operation(
            summary = "Check if email already exists",
            description = "Returns true if the email is already taken, false otherwise."
    )
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.userEmailExists(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PutMapping("/update-user-profile")
    @Operation(
            summary = "Update the user profile",
            description = """
                    Returns Updated UserResponseDTO with the updated profile details of user, it updates
                    "only the optional details such as email, phone, profile photo.
                    """
    )
    public ResponseEntity<UserResponseDto> updateProfileDetails(@RequestParam String username, @Valid @RequestBody UserRegisterDTO userDto) throws AccessDeniedException {

        String currentUserName = authService.getCurrentUser().getUsername();
        System.out.println("current username : " + currentUserName);
        System.out.println("requested username : " + username);
        if(!currentUserName.equalsIgnoreCase(username)) {
            throw new AccessDeniedException("You are not allowed to update other user's profile");
        }

        System.out.println("coming here to update profile");
        UserResponseDto updatedProfileDetails = userService.updateUserProfile(userDto, currentUserName);
        System.out.println("Profile updated successfully");

        return ResponseEntity.ok(updatedProfileDetails);
    }
}
