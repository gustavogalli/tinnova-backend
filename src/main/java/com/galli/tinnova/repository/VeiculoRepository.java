package com.galli.tinnova.repository;

import com.galli.tinnova.dto.response.VeiculoPorMarcaResponse;
import com.galli.tinnova.entity.Veiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    boolean existsByPlaca(String placa);

    Optional<Veiculo> findByIdAndAtivoTrue(Long id);

    @Query("""
        SELECT v FROM Veiculo v
        WHERE v.ativo = true
          AND (:marca IS NULL OR v.marca = :marca)
          AND (:ano IS NULL OR v.ano = :ano)
          AND (:cor IS NULL OR v.cor = :cor)
          AND (:minPreco IS NULL OR v.precoUsd >= :minPreco)
          AND (:maxPreco IS NULL OR v.precoUsd <= :maxPreco)
    """)
    Page<Veiculo> filtrar(
            @Param("marca") String marca,
            @Param("ano") Integer ano,
            @Param("cor") String cor,
            @Param("minPreco") BigDecimal minPreco,
            @Param("maxPreco") BigDecimal maxPreco,
            Pageable pageable
    );

    @Query("""
        select new com.galli.tinnova.dto.response.VeiculoPorMarcaResponse(
            v.marca,
            count(v)
        )
        from Veiculo v
        where v.ativo = true
        group by v.marca
    """)
    List<VeiculoPorMarcaResponse> relatorioPorMarca();
}
