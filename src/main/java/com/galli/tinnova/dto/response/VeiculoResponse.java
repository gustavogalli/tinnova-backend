package com.galli.tinnova.dto.response;

import java.math.BigDecimal;


public record VeiculoResponse(
        Long id,
        String marca,
        String modelo,
        Integer ano,
        String cor,
        String placa,
        BigDecimal precoUsd,
        BigDecimal precoBrl
) {}
