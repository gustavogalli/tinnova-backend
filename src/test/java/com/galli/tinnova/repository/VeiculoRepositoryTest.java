package com.galli.tinnova.repository;

import com.galli.tinnova.dto.response.VeiculoPorMarcaResponse;
import com.galli.tinnova.entity.Veiculo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VeiculoRepositoryTest {

    @Autowired
    private VeiculoRepository repository;

    @Test
    @DisplayName("Deve filtrar veículos por marca, ano e cor")
    void deveFiltrarPorMarcaAnoCor() {
        repository.save(veiculo("Ford", "Preto", 2020, new BigDecimal("10000")));
        repository.save(veiculo("Ford", "Branco", 2020, new BigDecimal("12000")));
        repository.save(veiculo("GM", "Preto", 2021, new BigDecimal("15000")));

        Page<Veiculo> page = repository.filtrar(
                "Ford",
                2020,
                "Preto",
                null,
                null,
                Pageable.unpaged()
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getMarca()).isEqualTo("Ford");
    }

    @Test
    @DisplayName("Deve filtrar veículos por faixa de preço")
    void deveFiltrarPorPreco() {
        repository.save(veiculo("Ford", "Preto", 2020, new BigDecimal("10000")));
        repository.save(veiculo("Ford", "Preto", 2020, new BigDecimal("20000")));
        repository.save(veiculo("Ford", "Preto", 2020, new BigDecimal("30000")));

        Page<Veiculo> page = repository.filtrar(
                null,
                null,
                null,
                new BigDecimal("15000"),
                new BigDecimal("25000"),
                Pageable.unpaged()
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getPrecoUsd())
                .isEqualByComparingTo("20000");
    }

    @Test
    @DisplayName("Deve gerar relatório de veículos agrupados por marca")
    void deveGerarRelatorioPorMarca() {
        repository.save(veiculo("Ford", "Preto", 2020, BigDecimal.TEN));
        repository.save(veiculo("Ford", "Branco", 2021, BigDecimal.TEN));
        repository.save(veiculo("GM", "Preto", 2022, BigDecimal.TEN));

        List<VeiculoPorMarcaResponse> relatorio = repository.relatorioPorMarca();

        assertThat(relatorio).hasSize(2);

        VeiculoPorMarcaResponse ford = relatorio.stream()
                .filter(r -> r.marca().equals("Ford"))
                .findFirst()
                .orElseThrow();

        assertThat(ford.quantidade()).isEqualTo(2);
    }

    private Veiculo veiculo(String marca, String cor, int ano, BigDecimal preco) {
        Veiculo v = new Veiculo();
        v.setMarca(marca);
        v.setModelo("Modelo X");
        v.setAno(ano);
        v.setCor(cor);
        v.setPlaca(marca.substring(0, 2) + ano + Math.random());
        v.setPrecoUsd(preco);
        v.setAtivo(true);
        return v;
    }

    @Test
    @DisplayName("Deve falhar ao salvar veículos com placa duplicada")
    void deveFalharComPlacaDuplicada() {
        Veiculo v1 = veiculo("Ford", "Preto", 2020, BigDecimal.TEN);
        v1.setPlaca("ABC1234");

        Veiculo v2 = veiculo("Ford", "Branco", 2021, BigDecimal.TEN);
        v2.setPlaca("ABC1234");

        repository.saveAndFlush(v1);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                repository.saveAndFlush(v2)
        ).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Deve retornar true quando existir veículo com a placa")
    void deveVerificarExistenciaPorPlaca() {
        Veiculo v = veiculo("Ford", "Preto", 2020, BigDecimal.TEN);
        v.setPlaca("XYZ9999");

        repository.save(v);

        boolean exists = repository.existsByPlaca("XYZ9999");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve buscar veículo apenas se estiver ativo")
    void deveBuscarSomenteVeiculoAtivo() {
        Veiculo ativo = veiculo("Ford", "Preto", 2020, BigDecimal.TEN);
        ativo.setPlaca("AAA1111");

        Veiculo inativo = veiculo("GM", "Branco", 2021, BigDecimal.TEN);
        inativo.setPlaca("BBB2222");
        inativo.setAtivo(false);

        repository.save(ativo);
        repository.save(inativo);

        assertThat(repository.findByIdAndAtivoTrue(ativo.getId()))
                .isPresent();

        assertThat(repository.findByIdAndAtivoTrue(inativo.getId()))
                .isEmpty();
    }

}
