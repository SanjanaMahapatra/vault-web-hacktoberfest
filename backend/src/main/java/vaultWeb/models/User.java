package vaultWeb.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import vaultWeb.dtos.user.UserDto;
import vaultWeb.dtos.user.UserRegisterDTO;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "vault_user")
@ToString(exclude = "groupMemberships")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<GroupMember> groupMemberships;

    @Column(nullable = true) // adding nullable = true, since these are optional fields
    private String phoneNumber;

    @Column(unique = true, nullable = true) // adding unique and nullable = true
    private String email;

    @Column(nullable = true)
    private String profilePicture;

    public User(UserRegisterDTO userDto) {
        username = userDto.getUsername();
        password = userDto.getPassword();
        groupMemberships = List.of();
        phoneNumber = userDto.getPhoneNumber();
        email = userDto.getEmail();
        profilePicture = userDto.getProfilePicture();
    }
}
