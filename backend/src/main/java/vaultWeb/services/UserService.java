package vaultWeb.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vaultWeb.dtos.user.UserDto;
import vaultWeb.dtos.user.UserRegisterDTO;
import vaultWeb.dtos.user.UserResponseDto;
import vaultWeb.exceptions.DuplicateEmailException;
import vaultWeb.exceptions.DuplicateUsernameException;
import vaultWeb.exceptions.notfound.UserNotFoundException;
import vaultWeb.models.User;
import vaultWeb.repositories.UserRepository;

import java.util.List;

/**
 * Service class for managing users.
 * <p>
 * Provides functionality for user registration, checking for existing usernames,
 * and retrieving all users. Passwords are securely encoded before storing.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user by encoding their password and assigning the default role.
     * <p>
     * Steps performed by this method:
     * <ol>
     *     <li>Check if the username already exists in the database. If so, throw {@link DuplicateUsernameException}.</li>
     *     <li>Encode the plaintext password using the injected {@link PasswordEncoder}.</li>
     *     <li>Save the user entity with the hashed password to the database via {@link UserRepository}.</li>
     * </ol>
     * <p>
     * Important:
     * - The PasswordEncoder bean must match the encoder used during authentication to correctly verify passwords.
     *
     * @param user The {@link User} entity containing username and plaintext password.
     * @throws DuplicateUsernameException if a user with the same username already exists.
     */
    public void registerUser(User user) {
        if (usernameExists(user.getUsername())) {
            throw new DuplicateUsernameException("Username '" + user.getUsername() + "' is already taken");
        }
        if(userEmailExists(user.getEmail())) {
            throw new DuplicateEmailException("Email '" + user.getEmail() + "' is already registered! Use another email!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    /**
     * Checks if a username already exists in the database.
     *
     * @param username The username to check.
     * @return {@code true} if the username exists, {@code false} otherwise.
     */
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Retrieves a list of all registered users.
     *
     * @return A {@link List} of {@link User} entities.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Checks if a email already exists in the database.
     *
     * @param email The email to check.
     * @return {@code true} if the email exists, {@code false} otherwise.
     */
    public boolean userEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }



    public UserResponseDto getUserDetailsByUsername(String username) {
        if (!usernameExists(username)) {
            throw new DuplicateUsernameException("Username '" + username + "' does not exists in database");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with given username"));

        System.out.println("user returned -> " + user.getUsername());
        return new UserResponseDto(user);
    }

    /**
     * This method updates the user profile in the database
     *
     * @param userDto Takes UserDto as a param.
     * @param username Takes username as a param
     * @return {@code UserResponseDto} with the updated user profile details
     * @throws DuplicateEmailException if a user with the same email already exists.
     */
    public UserResponseDto updateUserProfile(UserRegisterDTO userDto, String username) {
        User user = userRepository.findByUsername(username).
                orElseThrow(() -> new UserNotFoundException("User not found with given username"));

        // checking if the email id already exists

        if(userDto.getEmail() != null &&
            !userDto.getEmail().equalsIgnoreCase(user.getEmail()) &&
                userRepository.existsByEmail(userDto.getEmail())
        ) {
            throw new DuplicateEmailException("Email Address already exists with other user id");
        }

        // checking the condition that username / password should not be updated

        // setting of the optional fields i.e phone, email and profile picture
        if( user.getPhoneNumber() != null) {
            user.setPhoneNumber(userDto.getPhoneNumber());
        }

        if(user.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }

        if(user.getProfilePicture() != null) {
            user.setProfilePicture(userDto.getProfilePicture());
        }

        User updatedUser = userRepository.save(user);
        return new UserResponseDto(updatedUser);
    }
}