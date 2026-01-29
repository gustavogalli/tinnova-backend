package com.galli.tinnova.service;

import com.galli.tinnova.dto.response.VeiculoPorMarcaResponse;
import com.galli.tinnova.dto.response.VeiculoResponse;
import com.galli.tinnova.entity.Veiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface VeiculoService {

    Page<VeiculoResponse> listar(
            String marca,
            Integer ano,
            String cor,
            BigDecimal minPreco,
            BigDecimal maxPreco,
            Pageable pageable
    );

    Veiculo buscarPorId(Long id);

    Veiculo criar(Veiculo veiculo);

    Veiculo atualizar(Long id, Veiculo veiculo);

    Veiculo atualizarParcial(Long id, Map<String, Object> campos);

    void remover(Long id);

    VeiculoResponse detalhar(Long id);

    List<VeiculoPorMarcaResponse> relatorioPorMarca();
}
