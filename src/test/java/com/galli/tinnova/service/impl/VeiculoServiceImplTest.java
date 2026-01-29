package com.galli.tinnova.service.impl;

import com.galli.tinnova.dto.response.VeiculoPorMarcaResponse;
import com.galli.tinnova.dto.response.VeiculoResponse;
import com.galli.tinnova.entity.Veiculo;
import com.galli.tinnova.exception.ConflictException;
import com.galli.tinnova.exception.NotFoundException;
import com.galli.tinnova.repository.VeiculoRepository;
import com.galli.tinnova.service.DollarService;
import com.galli.tinnova.service.VeiculoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceImplTest {

    @Mock
    private VeiculoRepository repository;

    @Mock
    private DollarService dollarService;

    @InjectMocks
    private VeiculoServiceImpl service;

    private Veiculo veiculo() {
        return Veiculo.builder()
                .id(1L)
                .marca("Ford")
                .modelo("Ka")
                .ano(2020)
                .cor("Preto")
                .placa("ABC1D23")
                .precoUsd(BigDecimal.valueOf(10000))
                .ativo(true)
                .build();
    }

    @Test
    void deveCriarVeiculoComSucesso() {
        when(repository.existsByPlaca(any())).thenReturn(false);
        when(repository.save(any())).thenReturn(veiculo());
        when(dollarService.getUsdToBrl()).thenReturn(new BigDecimal("5.00"));

        Veiculo salvo = service.criar(veiculo());

        assertThat(salvo).isNotNull();
        assertThat(salvo.getAtivo()).isTrue();
    }

    @Test
    void deveListarComFiltros() {
        when(repository.filtrar(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(veiculo())));

        Page<VeiculoResponse> page = service.listar(
                "Ford",
                2020,
                "Preto",
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(15000),
                Pageable.unpaged()
        );

        assertThat(page.getContent()).hasSize(1);
    }


    @Test
    void deveFalharAoAtualizarComPlacaDuplicada() {
        Veiculo existente = veiculo();
        Veiculo novo = veiculo();
        novo.setPlaca("XYZ9Z99");

        when(repository.findByIdAndAtivoTrue(1L))
                .thenReturn(Optional.of(existente));
        when(repository.existsByPlaca("XYZ9Z99"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, novo))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deveAtualizarParcialmente() {
        Veiculo existente = veiculo();

        when(repository.findByIdAndAtivoTrue(1L))
                .thenReturn(Optional.of(existente));
        when(repository.save(any())).thenReturn(existente);

        Veiculo atualizado = service.atualizarParcial(
                1L,
                Map.of("cor", "Azul")
        );

        assertThat(atualizado.getCor()).isEqualTo("Azul");
    }

    @Test
    void deveFazerSoftDelete() {
        Veiculo existente = veiculo();

        when(repository.findByIdAndAtivoTrue(1L))
                .thenReturn(Optional.of(existente));

        service.remover(1L);

        assertThat(existente.getAtivo()).isFalse();
        verify(repository).save(existente);
    }

    @Test
    void deveFalharAoBuscarVeiculoInexistente() {
        when(repository.findByIdAndAtivoTrue(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deveDetalharVeiculoComPrecoConvertidoParaReais() {
        Long id = 1L;

        Veiculo veiculo = Veiculo.builder()
                .id(id)
                .marca("Toyota")
                .modelo("Corolla")
                .precoUsd(new BigDecimal("10000"))
                .ativo(true)
                .build();

        when(repository.findByIdAndAtivoTrue(id))
                .thenReturn(Optional.of(veiculo));

        when(dollarService.getUsdToBrl())
                .thenReturn(new BigDecimal("5.00"));

        VeiculoResponse response = service.detalhar(id);

        assertNotNull(response);
        assertEquals("Toyota", response.marca());
        assertEquals("Corolla", response.modelo());
        assertEquals(new BigDecimal("50000.00"), response.precoBrl());

        verify(repository).findByIdAndAtivoTrue(id);
        verify(dollarService).getUsdToBrl();
    }

    @Test
    void deveLancarExcecaoQuandoVeiculoNaoEncontrado() {
        Long id = 99L;

        when(repository.findByIdAndAtivoTrue(id))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.detalhar(id)
        );

        assertEquals("Veículo não encontrado", exception.getMessage());
        verify(repository).findByIdAndAtivoTrue(id);
        verifyNoInteractions(dollarService);
    }

    @Test
    void deveGerarRelatorioPorMarca() {
        List<VeiculoPorMarcaResponse> relatorio = List.of(
                new VeiculoPorMarcaResponse("Toyota", 3L),
                new VeiculoPorMarcaResponse("Honda", 2L)
        );

        when(repository.relatorioPorMarca()).thenReturn(relatorio);

        List<VeiculoPorMarcaResponse> response = service.relatorioPorMarca();

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("Toyota", response.get(0).marca());
        assertEquals(3L, response.get(0).quantidade());

        verify(repository).relatorioPorMarca();
    }

    @Test
    void deveAtualizarVeiculoComSucesso() {
        Long id = 1L;

        Veiculo existente = Veiculo.builder()
                .id(id)
                .marca("Ford")
                .modelo("Ka")
                .ano(2019)
                .cor("Branco")
                .placa("ABC-1234")
                .precoUsd(new BigDecimal("10000"))
                .build();

        Veiculo atualizado = Veiculo.builder()
                .marca("Toyota")
                .modelo("Corolla")
                .ano(2022)
                .cor("Preto")
                .placa("XYZ-9999")
                .precoUsd(new BigDecimal("20000"))
                .build();

        VeiculoService spyService = spy(service);

        doReturn(existente).when(spyService).buscarPorId(id);
        when(repository.existsByPlaca("XYZ-9999")).thenReturn(false);
        when(repository.save(any(Veiculo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dollarService.getUsdToBrl()).thenReturn(new BigDecimal("5.00"));


        Veiculo resultado = spyService.atualizar(id, atualizado);

        assertNotNull(resultado);
        assertEquals("Toyota", resultado.getMarca());
        assertEquals("Corolla", resultado.getModelo());
        assertEquals(2022, resultado.getAno());
        assertEquals("Preto", resultado.getCor());
        assertEquals("XYZ-9999", resultado.getPlaca());

        verify(repository).save(existente);
    }

    @Test
    void deveFalharAoCriarVeiculoComPlacaDuplicada() {
        String placaDuplicada = "AAA1234";

        Veiculo v2 = Veiculo.builder()
                .marca("Honda")
                .placa(placaDuplicada)
                .build();

        when(repository.existsByPlaca(placaDuplicada)).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> service.criar(v2)
        );

        verify(repository, never()).save(any(Veiculo.class));
    }

    @Test
    void deveFiltrarPorMarcaEAno() {
        Veiculo veiculo = Veiculo.builder().marca("Toyota").ano(2022).build();
        Page<Veiculo> pageMock = new PageImpl<>(List.of(veiculo));

        when(repository.filtrar(any(), any(), any(), any(), any(), any()))
                .thenReturn(pageMock);

        Page<VeiculoResponse> page = service.listar("Toyota", 2022, null, null, null, Pageable.unpaged());

        assertEquals(1, page.getTotalElements());
    }

}
