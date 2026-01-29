package com.galli.tinnova.mapper;

import com.galli.tinnova.dto.request.VeiculoRequest;
import com.galli.tinnova.dto.response.VeiculoResponse;
import com.galli.tinnova.entity.Veiculo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class VeiculoMapperTest {

    @Test
    void deveConverterRequestParaEntity() {
        VeiculoRequest request = new VeiculoRequest(
                "Ford",
                "Fiesta",
                2020,
                "Preto",
                "ABC1234",
                new BigDecimal("10000")
        );

        Veiculo veiculo = VeiculoMapper.toEntity(request);

        assertThat(veiculo.getMarca()).isEqualTo("Ford");
        assertThat(veiculo.getModelo()).isEqualTo("Fiesta");
        assertThat(veiculo.getAno()).isEqualTo(2020);
        assertThat(veiculo.getCor()).isEqualTo("Preto");
        assertThat(veiculo.getPlaca()).isEqualTo("ABC1234");
        assertThat(veiculo.getPrecoUsd()).isEqualByComparingTo("10000");
    }

    @Test
    void deveConverterEntityParaResponseSemPrecoBrl() {
        Veiculo veiculo = Veiculo.builder()
                .id(1L)
                .marca("GM")
                .modelo("Onix")
                .ano(2021)
                .cor("Branco")
                .placa("XYZ9999")
                .precoUsd(new BigDecimal("20000"))
                .build();

        VeiculoResponse response = VeiculoMapper.toResponse(veiculo);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.marca()).isEqualTo("GM");
        assertThat(response.modelo()).isEqualTo("Onix");
        assertThat(response.ano()).isEqualTo(2021);
        assertThat(response.cor()).isEqualTo("Branco");
        assertThat(response.placa()).isEqualTo("XYZ9999");
        assertThat(response.precoUsd()).isEqualByComparingTo("20000");
        assertThat(response.precoBrl()).isNull();
    }

    @Test
    void deveConverterEntityParaResponseComPrecoBrl() {
        Veiculo veiculo = Veiculo.builder()
                .id(2L)
                .marca("Toyota")
                .modelo("Corolla")
                .ano(2022)
                .cor("Prata")
                .placa("DEF5678")
                .precoUsd(new BigDecimal("30000"))
                .build();

        BigDecimal precoBrl = new BigDecimal("150000");

        VeiculoResponse response = VeiculoMapper.toResponse(veiculo, precoBrl);

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.marca()).isEqualTo("Toyota");
        assertThat(response.modelo()).isEqualTo("Corolla");
        assertThat(response.ano()).isEqualTo(2022);
        assertThat(response.cor()).isEqualTo("Prata");
        assertThat(response.placa()).isEqualTo("DEF5678");
        assertThat(response.precoUsd()).isEqualByComparingTo("30000");
        assertThat(response.precoBrl()).isEqualByComparingTo("150000");
    }
}
