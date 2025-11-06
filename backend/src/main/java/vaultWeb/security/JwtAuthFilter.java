package vaultWeb.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vaultWeb.services.auth.MyUserDetailsService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT authentication filter that intercepts incoming HTTP requests and validates JWT tokens.
 * <p>
 * This filter extracts the JWT token from the "Authorization" header (Bearer scheme),
 * validates it using {@link JwtUtil}, and sets the authenticated user in the Spring Security context.
 * Requests to "/api/auth/**" are excluded from authentication.
 * </p>
 * <p>
 * This filter extends {@link OncePerRequestFilter}, ensuring it is executed once per request.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final MyUserDetailsService userDetailsService;

    // Define the list of public path that should bypass the JWT authentication
    private static final List<String> PUBLIC_PATH = Arrays.asList(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/users",
            "/api/auth/check-username",
            "/api/auth/check-email"
    );

    /**
     * Constructs a new {@code JwtAuthFilter} with the specified {@link JwtUtil} and {@link MyUserDetailsService}.
     *
     * @param jwtUtil            the utility class for JWT token operations (extracting username, validating token)
     * @param userDetailsService the user details service to load user information by username
     */
    /**
     * Filters each HTTP request, performing JWT validation and setting authentication in the security context.
     * <p>
     * Steps:
     * <ol>
     *     <li>Skip requests starting with "/api/auth/".</li>
     *     <li>Extract JWT from the "Authorization" header if it starts with "Bearer ".</li>
     *     <li>Validate the token and extract the username.</li>
     *     <li>Load user details and set authentication in the {@link SecurityContextHolder}.</li>
     * </ol>
     * If the token is invalid or expired, a 401 Unauthorized response is returned.
     * </p>
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        /**
         *  this line of code is added to make sure that when any user profile is updated
         *  the user must be authenticated before in their profile
         */

        boolean isPublicPath = PUBLIC_PATH.stream().anyMatch(path::startsWith);

        if (isPublicPath) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (JwtException | AuthenticationException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
