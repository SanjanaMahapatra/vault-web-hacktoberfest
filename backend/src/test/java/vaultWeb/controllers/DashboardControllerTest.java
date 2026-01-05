package vaultWeb.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import vaultWeb.dtos.dashboard.UserDashboardDto;
import vaultWeb.models.User;
import vaultWeb.repositories.UserRepository;
import vaultWeb.services.DashboardService;
import vaultWeb.services.auth.AuthService;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

  @Mock private DashboardService dashboardService;

  @Mock private AuthService authService;

  @Mock private UserRepository userRepository;

  @InjectMocks private DashboardController dashboardController;

  @Test
  void shouldReturnDashboardForCurrentUser() {
    // Arrange
    User user = new User();

    UserDashboardDto dashboardDto =
        new UserDashboardDto(null, List.of(), List.of(), List.of(), List.of());

    when(authService.getCurrentUser()).thenReturn(user);
    when(dashboardService.buildDashboard(user)).thenReturn(dashboardDto);

    // Act
    ResponseEntity<UserDashboardDto> response = dashboardController.getCurrentUserDashboard();

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(dashboardDto, response.getBody());
  }

  @Test
  void shouldReturnDashboardForSpecificUser() {
    // Arrange
    String username = "test_user";
    User user = new User();

    UserDashboardDto dashboardDto =
        new UserDashboardDto(null, List.of(), List.of(), List.of(), List.of());

    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
    when(dashboardService.buildDashboard(user)).thenReturn(dashboardDto);

    // Act
    ResponseEntity<UserDashboardDto> response = dashboardController.getDashboardForUser(username);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(dashboardDto, response.getBody());
  }
}
