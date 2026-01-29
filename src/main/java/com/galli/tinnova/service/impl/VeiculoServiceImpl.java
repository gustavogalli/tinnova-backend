package com.galli.tinnova.service.impl;

import com.galli.tinnova.dto.response.VeiculoPorMarcaResponse;
import com.galli.tinnova.dto.response.VeiculoResponse;
import com.galli.tinnova.entity.Veiculo;
import com.galli.tinnova.exception.ConflictException;
import com.galli.tinnova.exception.NotFoundException;
import com.galli.tinnova.mapper.VeiculoMapper;
import com.galli.tinnova.repository.VeiculoRepository;
import com.galli.tinnova.service.DollarService;
import com.galli.tinnova.service.VeiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VeiculoServiceImpl implements VeiculoService {

    private final DollarService dollarService;
    private final VeiculoRepository repository;

    @Override
    public Page<VeiculoResponse> listar(
            String marca,
            Integer ano,
            String cor,
            BigDecimal minPreco,
            BigDecimal maxPreco,
            Pageable pageable
    ) {
        return repository.filtrar(
                        marca,
                        ano,
                        cor,
                        minPreco,
                        maxPreco,
                        pageable
                )
                .map(VeiculoMapper::toResponse);
    }

    @Override
    public Veiculo buscarPorId(Long id) {
        return repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new NotFoundException("Veículo não encontrado"));
    }

    @Override
    public Veiculo criar(Veiculo veiculo) {
        if (repository.existsByPlaca(veiculo.getPlaca())) {
            throw new ConflictException("Já existe veículo com essa placa");
        }

        BigDecimal dolar = dollarService.getUsdToBrl();

        if (dolar == null || dolar.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Não foi possível obter a cotação do dólar");
        }

        BigDecimal precoUsd = veiculo.getPrecoUsd()
                .divide(dolar, 2, RoundingMode.HALF_UP);

        veiculo.setId(null);
        veiculo.setPrecoUsd(precoUsd);
        veiculo.setAtivo(true);

        return repository.save(veiculo);
    }

    @Override
    public Veiculo atualizar(Long id, Veiculo veiculo) {
        Veiculo existente = buscarPorId(id);

        if (!existente.getPlaca().equals(veiculo.getPlaca())
                && repository.existsByPlaca(veiculo.getPlaca())) {
            throw new ConflictException("Já existe veículo com essa placa");
        }

        BigDecimal dolar = dollarService.getUsdToBrl();

        if (dolar == null || dolar.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Não foi possível obter a cotação do dólar");
        }

        BigDecimal precoUsd =
                veiculo.getPrecoUsd().divide(dolar, 2, RoundingMode.HALF_UP);

        existente.setMarca(veiculo.getMarca());
        existente.setModelo(veiculo.getModelo());
        existente.setAno(veiculo.getAno());
        existente.setCor(veiculo.getCor());
        existente.setPlaca(veiculo.getPlaca());
        existente.setPrecoUsd(converterParaUsd(veiculo.getPrecoUsd()));

        return repository.save(existente);
    }

    @Override
    public Veiculo atualizarParcial(Long id, Map<String, Object> campos) {
        Veiculo existente = buscarPorId(id);

        campos.forEach((key, value) -> {
            if (key.equals("id") || key.equals("ativo")) {
                throw new IllegalArgumentException("Campo não pode ser alterado: " + key);
            }

            if (key.equals("precoBrl")) {
                BigDecimal valorBrl = new BigDecimal(value.toString());
                existente.setPrecoUsd(converterParaUsd(valorBrl));
                return;
            }

            if (key.equals("placa")) {
                String novaPlaca = value.toString();
                if (!existente.getPlaca().equals(novaPlaca) && repository.existsByPlaca(novaPlaca)) {
                    throw new ConflictException("Já existe veículo com essa placa");
                }
            }

            Field field = ReflectionUtils.findField(Veiculo.class, key);
            if (field == null) {
                throw new IllegalArgumentException("Campo inválido: " + key);
            }

            field.setAccessible(true);
            ReflectionUtils.setField(field, existente, value);
        });

        return repository.save(existente);
    }


    @Override
    public void remover(Long id) {
        Veiculo veiculo = buscarPorId(id);
        veiculo.setAtivo(false);
        repository.save(veiculo);
    }

    @Override
    public VeiculoResponse detalhar(Long id) {
        Veiculo veiculo = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new NotFoundException("Veículo não encontrado"));

        BigDecimal dolar = dollarService.getUsdToBrl();
        BigDecimal precoBrl = veiculo.getPrecoUsd().multiply(dolar);

        return VeiculoMapper.toResponse(veiculo, precoBrl);
    }

    @Override
    public List<VeiculoPorMarcaResponse> relatorioPorMarca() {
        return repository.relatorioPorMarca();
    }

    private BigDecimal converterParaUsd(BigDecimal valorBrl) {
        BigDecimal dolar = dollarService.getUsdToBrl();
        if (dolar == null || dolar.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Não foi possível obter a cotação do dólar");
        }
        return valorBrl.divide(dolar, 2, RoundingMode.HALF_UP);
    }
}
