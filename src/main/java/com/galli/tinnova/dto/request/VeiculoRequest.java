package com.galli.tinnova.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record VeiculoRequest(

        @NotBlank
        String marca,

        @NotBlank
        String modelo,

        @NotNull
        Integer ano,

        @NotBlank
        String cor,

        @NotBlank
        String placa,

        @NotNull
        @Positive
        BigDecimal precoBrl
) {}
