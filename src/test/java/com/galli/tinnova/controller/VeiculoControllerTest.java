package com.galli.tinnova.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.galli.tinnova.dto.request.VeiculoRequest;
import com.galli.tinnova.entity.Veiculo;
import com.galli.tinnova.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VeiculoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VeiculoRepository veiculoRepository;

    private Veiculo veiculo;

    @BeforeEach
    void setup() {
        veiculoRepository.deleteAll();

        veiculo = veiculoRepository.save(
                Veiculo.builder()
                        .marca("Toyota")
                        .modelo("Corolla")
                        .ano(2022)
                        .cor("Prata")
                        .placa("ABC1234")
                        .precoUsd(new BigDecimal("20000"))
                        .ativo(true)
                        .build()
        );
    }

    @Test
    @WithMockUser
    void deveListarVeiculos() throws Exception {
        mockMvc.perform(get("/veiculos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].marca").value("Toyota"));
    }

    @Test
    @WithMockUser
    void deveDetalharVeiculo() throws Exception {
        mockMvc.perform(get("/veiculos/{id}", veiculo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(veiculo.getId()))
                .andExpect(jsonPath("$.marca").value("Toyota"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveCriarVeiculo() throws Exception {
        VeiculoRequest request = new VeiculoRequest(
                "Honda",
                "Civic",
                2023,
                "Preto",
                "DEF5678",
                new BigDecimal("25000")
        );

        mockMvc.perform(post("/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.marca").value("Honda"))
                .andExpect(jsonPath("$.modelo").value("Civic"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveAtualizarVeiculo() throws Exception {
        VeiculoRequest request = new VeiculoRequest(
                "Toyota",
                "Corolla XEI",
                2023,
                "Branco",
                "ABC1234",
                new BigDecimal("23000")
        );

        mockMvc.perform(put("/veiculos/{id}", veiculo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Corolla XEI"))
                .andExpect(jsonPath("$.cor").value("Branco"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveAtualizarParcialmente() throws Exception {
        Map<String, Object> campos = Map.of(
                "cor", "Preto"
        );

        mockMvc.perform(patch("/veiculos/{id}", veiculo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(campos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cor").value("Preto"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRemoverVeiculo() throws Exception {
        mockMvc.perform(delete("/veiculos/{id}", veiculo.getId()))
                .andExpect(status().isNoContent());

        assertThat(
                veiculoRepository.findByIdAndAtivoTrue(veiculo.getId())
        ).isEmpty();
    }

    @Test
    @WithMockUser
    void deveGerarRelatorioPorMarca() throws Exception {
        mockMvc.perform(get("/veiculos/relatorios/por-marca"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].marca").value("Toyota"))
                .andExpect(jsonPath("$[0].quantidade").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void patchComCampoInvalidoDeveRetornar400() throws Exception {
        Map<String, Object> campos = Map.of(
                "campoInexistente", "teste"
        );

        mockMvc.perform(patch("/veiculos/{id}", veiculo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(campos)))
                .andExpect(status().isBadRequest());
    }
}
