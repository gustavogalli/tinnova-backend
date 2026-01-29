package com.galli.tinnova.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "veiculos", uniqueConstraints = {@UniqueConstraint(columnNames = "placa")})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String marca;

    private String modelo;

    private Integer ano;

    private String cor;

    @Column(nullable = false, unique = true)
    private String placa;

    private BigDecimal precoUsd;

    private Boolean ativo;
}
