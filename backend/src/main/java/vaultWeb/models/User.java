package vaultWeb.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import vaultWeb.dtos.user.UserDto;

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

    private String phoneNumber;

    @Column(unique = true)
    private String email;
    private String profilePicture;

    public User(UserDto userDto) {
        username = userDto.getUsername();
        password = userDto.getPassword();
        groupMemberships = List.of();
        phoneNumber = userDto.getPhoneNumber();
        email = userDto.getEmail();
        profilePicture = userDto.getProfilePicture();
    }
}
