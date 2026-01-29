package com.galli.tinnova.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandlerTest.TestController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/auth/login");
    }

    @Test
    @DisplayName("Deve retornar 404 ao lançar NotFoundException")
    void deveRetornar404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("Deve retornar 409 ao lançar ConflictException")
    void deveRetornar409() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Conflito de dados"))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("Deve retornar 500 ao lançar Exception genérica")
    void deveRetornar500() throws Exception {
        mockMvc.perform(get("/test/generic-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Erro interno inesperado"))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    @DisplayName("Deve retornar 401 para AuthenticationException")
    void testHandleAuthentication() {
        BadCredentialsException ex =
                new BadCredentialsException("Credenciais inválidas");

        ResponseEntity<ApiError> response =
                handler.handleAuthentication(ex, request);

        assertNotNull(response);
        assertEquals(401, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Usuário não autenticado", response.getBody().getMessage());
        assertEquals("/auth/login", response.getBody().getPath());
        assertEquals("Unauthorized", response.getBody().getError());
    }

    @Test
    @DisplayName("Deve retornar 403 para AccessDeniedException")
    void testHandleAccessDenied() {
        AccessDeniedException ex =
                new AccessDeniedException("Acesso negado");

        ResponseEntity<ApiError> response =
                handler.handleAccessDenied(ex, request);

        assertNotNull(response);
        assertEquals(403, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Acesso negado: permissão insuficiente",
                response.getBody().getMessage());
        assertEquals("Forbidden", response.getBody().getError());
    }

    @RestController
    static class TestController {

        @GetMapping("/test/not-found")
        public void notFound() {
            throw new NotFoundException("Recurso não encontrado");
        }

        @GetMapping("/test/conflict")
        public void conflict() {
            throw new ConflictException("Conflito de dados");
        }

        @GetMapping("/test/generic-error")
        public void genericError() {
            throw new RuntimeException("Erro inesperado");
        }
    }
}
