package com.galli.tinnova.mapper;

import com.galli.tinnova.dto.request.VeiculoRequest;
import com.galli.tinnova.dto.response.VeiculoResponse;
import com.galli.tinnova.entity.Veiculo;

import java.math.BigDecimal;

public class VeiculoMapper {

    public static Veiculo toEntity(VeiculoRequest request) {
        return Veiculo.builder()
                .marca(request.marca())
                .modelo(request.modelo())
                .ano(request.ano())
                .cor(request.cor())
                .placa(request.placa())
                .precoUsd(request.precoBrl())
                .build();
    }

    public static VeiculoResponse toResponse(Veiculo veiculo) {
        return new VeiculoResponse(
                veiculo.getId(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.getCor(),
                veiculo.getPlaca(),
                veiculo.getPrecoUsd(),
                null
        );
    }

    public static VeiculoResponse toResponse(Veiculo veiculo, BigDecimal precoBrl) {
        return new VeiculoResponse(
                veiculo.getId(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.getCor(),
                veiculo.getPlaca(),
                veiculo.getPrecoUsd(),
                precoBrl
        );
    }
}
