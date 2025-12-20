package vaultWeb.models;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Data;

@Entity
@Data
public class RefreshToken {

  @Id @GeneratedValue private Long id;

  @Column(nullable = false, unique = true)
  private String tokenId; // jti

  @ManyToOne(optional = false)
  private User user;

  @Column(nullable = false)
  private String tokenHash;

  @Column(nullable = false)
  private Instant expiresAt;

  private boolean revoked;

  private Instant createdAt = Instant.now();
}
