package app.bankcardmanagementsystem.unit;

import app.bankcardmanagementsystem.controller.dto.cooke.CreateCookeDto;
import app.bankcardmanagementsystem.service.impl.CookeServiceImp;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class CookeServiceImpTest {

    private HttpServletResponse httpServletResponse;
    private CookeServiceImp cookeService;

    @BeforeEach
    void setUp() {
        httpServletResponse = mock(HttpServletResponse.class);
        cookeService = new CookeServiceImp(httpServletResponse);
    }

    @Test
    void createCooke_shouldAddCookieAndSetContentType() {
        // Arrange
        CreateCookeDto createCookeDto = CreateCookeDto.builder()
                .key("refreshToken")
                .data("sample_refresh_token")
                .timeLiveCooke(3600)
                .path("/")
                .contentType("text/plain")
                .build();

        // Act
        cookeService.createCooke(createCookeDto);

        // Assert
        Cookie expectedCookie = new Cookie("refreshToken", "sample_refresh_token");
        expectedCookie.setPath("/");
        expectedCookie.setMaxAge(3600);

        verify(httpServletResponse).addCookie(argThat(cookie ->
                cookie.getName().equals(expectedCookie.getName()) &&
                        cookie.getValue().equals(expectedCookie.getValue()) &&
                        cookie.getPath().equals(expectedCookie.getPath()) &&
                        cookie.getMaxAge() == expectedCookie.getMaxAge()));

        verify(httpServletResponse).setContentType("text/plain");
    }

    @Test
    void createCooke_shouldHandleEmptyPath() {
        // Arrange
        CreateCookeDto createCookeDto = CreateCookeDto.builder()
                .key("sessionToken")
                .data("session_token_value")
                .timeLiveCooke(1800)
                .path("") // Empty path
                .contentType("application/json")
                .build();

        // Act
        cookeService.createCooke(createCookeDto);

        // Assert
        Cookie expectedCookie = new Cookie("sessionToken", "session_token_value");
        expectedCookie.setPath("");
        expectedCookie.setMaxAge(1800);

        verify(httpServletResponse).addCookie(argThat(cookie ->
                cookie.getName().equals(expectedCookie.getName()) &&
                        cookie.getValue().equals(expectedCookie.getValue()) &&
                        cookie.getPath().equals(expectedCookie.getPath()) &&
                        cookie.getMaxAge() == expectedCookie.getMaxAge()));

        verify(httpServletResponse).setContentType("application/json");
    }
}
