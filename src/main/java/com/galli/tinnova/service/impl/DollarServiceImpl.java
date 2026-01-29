package com.galli.tinnova.service.impl;

import com.galli.tinnova.service.DollarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
public class DollarServiceImpl implements DollarService {

    private final RestClient restClient;

    public DollarServiceImpl(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    @Cacheable(value = "usd-brl", unless = "#result == null")
    public BigDecimal getUsdToBrl() {
        log.info("🔍 Buscando cotação USD → BRL");

        try {
            BigDecimal valor = getFromAwesomeApi();
            log.info("Cotação obtida da AwesomeAPI: {}", valor);

            return valor;
        } catch (Exception e) {
            log.warn("Falha na AwesomeAPI, usando Frankfurter. Motivo: {}", e.getMessage());
            BigDecimal fallback = getFromFrankfurter();
            log.info("Cotação obtida da Frankfurter: {}", fallback);

            return fallback;
        }
    }

    BigDecimal getFromAwesomeApi() {
        Map<String, Object> response =
                restClient.get()
                        .uri("https://economia.awesomeapi.com.br/json/last/USD-BRL")
                        .retrieve()
                        .body(Map.class);

        if (response == null || !response.containsKey("USDBRL")) {
            throw new IllegalStateException("Resposta inválida da AwesomeAPI");
        }

        Map<String, String> usdbrl = (Map<String, String>) response.get("USDBRL");
        return new BigDecimal(usdbrl.get("bid"));
    }

    BigDecimal getFromFrankfurter() {
        Map<String, Object> response =
                restClient.get()
                        .uri("https://api.frankfurter.app/latest?from=USD&to=BRL")
                        .retrieve()
                        .body(Map.class);

        if (response == null || !response.containsKey("rates")) {
            throw new IllegalStateException("Resposta inválida da Frankfurter");
        }

        Map<String, BigDecimal> rates =
                (Map<String, BigDecimal>) response.get("rates");

        return rates.get("BRL");
    }
}
