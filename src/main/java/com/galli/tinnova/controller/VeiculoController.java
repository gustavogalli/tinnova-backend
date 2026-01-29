package com.galli.tinnova.controller;

import com.galli.tinnova.dto.request.VeiculoRequest;
import com.galli.tinnova.dto.response.VeiculoPorMarcaResponse;
import com.galli.tinnova.dto.response.VeiculoResponse;
import com.galli.tinnova.entity.Veiculo;
import com.galli.tinnova.mapper.VeiculoMapper;
import com.galli.tinnova.service.VeiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/veiculos")
@RequiredArgsConstructor
public class VeiculoController {

    private final VeiculoService veiculoService;

    @GetMapping
    public ResponseEntity<Page<VeiculoResponse>> listar(
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) String cor,
            @RequestParam(required = false) BigDecimal minPreco,
            @RequestParam(required = false) BigDecimal maxPreco,
            Pageable pageable
    ) {
        Page<VeiculoResponse> page =
                veiculoService.listar(marca, ano, cor, minPreco, maxPreco, pageable);

        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponse> detalhar(@PathVariable Long id) {
        VeiculoResponse response = veiculoService.detalhar(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Veiculo> criar(
            @RequestBody @Valid VeiculoRequest request
    ) {
        Veiculo veiculo = veiculoService.criar(VeiculoMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(veiculo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Veiculo> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid VeiculoRequest request
    ) {
        Veiculo veiculo =
                veiculoService.atualizar(id, VeiculoMapper.toEntity(request));

        return ResponseEntity.ok(veiculo);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Veiculo> atualizarParcial(
            @PathVariable Long id,
            @RequestBody Map<String, Object> campos
    ) {
        Veiculo veiculo = veiculoService.atualizarParcial(id, campos);
        return ResponseEntity.ok(veiculo);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        veiculoService.remover(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/relatorios/por-marca")
    public ResponseEntity<List<VeiculoPorMarcaResponse>> relatorioPorMarca() {
        List<VeiculoPorMarcaResponse> relatorio =
                veiculoService.relatorioPorMarca();

        return ResponseEntity.ok(relatorio);
    }
}
