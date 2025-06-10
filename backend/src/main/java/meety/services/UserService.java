package meety.services;

import meety.exceptions.notfound.UserNotFoundException;
import meety.models.User;
import meety.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registers a new user by encoding their password and assigning the default role.
     *
     * @param user The User entity with plaintext password and username set.
     *             <p>
     *             1. The plaintext password from the User object is hashed using the injected PasswordEncoder.
     *             This ensures that the password is securely stored in the database (never plaintext).
     *             The hashing algorithm used depends on the PasswordEncoder bean configuration (commonly BCrypt).
     *             2. The user's role is set to the default Role.User.
     *             3. The user entity with hashed password and role is then saved to the database via UserRepository.
     *             <p>
     *             Important:
     *             - The PasswordEncoder bean must be consistent with the encoder used during authentication to correctly verify passwords later on.
     */
    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    /**
     * Finds a user by their username.
     *
     * @param username The username to search for.
     * @return The User entity if found, or null if no user exists with this username.
     * <p>
     * Used internally to fetch full user details for login or other user-related operations.
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }
}
