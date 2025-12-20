package vaultWeb.services.auth;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vaultWeb.models.RefreshToken;
import vaultWeb.models.User;
import vaultWeb.repositories.RefreshTokenRepository;
import vaultWeb.security.JwtUtil;
import vaultWeb.security.TokenHashUtil;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  /**
   * Creates and issues a new refresh token for the given user.
   *
   * <p>This method is responsible for generating, persisting, and delivering refresh tokens during
   * authentication and refresh token rotation.
   *
   * <p><b>Workflow:</b>
   *
   * <ol>
   *   <li>Revokes all existing refresh tokens associated with the user to ensure only one active
   *       refresh token exists per user/session.
   *   <li>Generates a unique token identifier (<code>jti</code>) for the new refresh token.
   *   <li>Creates a signed refresh token JWT containing the user identifier and the generated
   *       <code>jti</code>.
   *   <li>Computes a one-way SHA-256 hash of the refresh token and stores it in the database
   *       instead of storing the token in plaintext.
   *   <li>Persists the refresh token metadata (jti, hash, user, expiration, and revocation status)
   *       in the database.
   *   <li>Sends the refresh token to the client as a secure, HttpOnly cookie.
   * </ol>
   *
   * <p><b>Security considerations:</b>
   *
   * <ul>
   *   <li>Refresh tokens are JWTs signed with a dedicated refresh signing key.
   *   <li>The refresh token itself is never stored in plaintext; only a SHA-256 hash is persisted.
   *   <li>Revoking existing tokens prevents reuse and enforces refresh token rotation semantics.
   *   <li>The refresh token cookie is marked HttpOnly and Secure to mitigate XSS and
   *       man-in-the-middle attacks.
   * </ul>
   *
   * @param user the authenticated user for whom the refresh token is issued
   * @param response the HTTP response used to attach the refresh token cookie
   */
  public void create(User user, HttpServletResponse response) {

    // revoke old tokens
    refreshTokenRepository.revokeAllByUser(user.getId());

    // generate jti
    String tokenId = UUID.randomUUID().toString();

    // generate JWT refresh token
    String refreshToken = jwtUtil.generateRefreshToken(user, tokenId);

    String hash = TokenHashUtil.sha256(refreshToken);

    // store hashed token
    RefreshToken entity = new RefreshToken();
    entity.setTokenId(tokenId);
    entity.setUser(user);
    entity.setTokenHash(hash);
    entity.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
    entity.setRevoked(false);

    refreshTokenRepository.save(entity);

    // set cookie
    ResponseCookie cookie =
        ResponseCookie.from("refresh_token", refreshToken)
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/api/auth/refresh")
            .maxAge(Duration.ofDays(30))
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
