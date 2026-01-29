package com.galli.tinnova.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void devePermitirAcessoARotaPublica() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void devePermitirAcessoARotaPublicaDeVeiculos() throws Exception {
        mockMvc.perform(get("/veiculos"))
                .andExpect(status().isOk());
    }

    @Test
    void deveCriarUsuarioAdminEmMemoria() {
        UserDetails admin = userDetailsService.loadUserByUsername("admin");

        assertThat(admin).isNotNull();
        assertThat(admin.getUsername()).isEqualTo("admin");
        assertThat(admin.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
                .isTrue();
    }

    @Test
    void deveCriarUsuarioUserEmMemoria() {
        UserDetails user = userDetailsService.loadUserByUsername("user");

        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("user");
        assertThat(user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")))
                .isTrue();
    }

    @Test
    void passwordEncoderDeveValidarSenha() {
        String senha = "admin123";
        String hash = passwordEncoder.encode(senha);

        assertThat(passwordEncoder.matches(senha, hash)).isTrue();
    }
}
