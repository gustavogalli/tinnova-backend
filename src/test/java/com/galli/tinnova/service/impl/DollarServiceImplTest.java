package com.galli.tinnova.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DollarServiceImplTest {

    @Test
    void deveBuscarCotacaoNaAwesomeApi() {
        RestClient restClient = mock(RestClient.class);
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        Map<String, Object> response = Map.of(
                "USDBRL", Map.of("bid", "5.12")
        );

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Map.class)).thenReturn(response);

        DollarServiceImpl service = new DollarServiceImpl(builderQueRetorna(restClient));

        BigDecimal result = service.getFromAwesomeApi();

        assertEquals(new BigDecimal("5.12"), result);
    }

    @Test
    void deveBuscarCotacaoNaFrankfurter() {
        RestClient restClient = mock(RestClient.class);
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        Map<String, Object> response = Map.of(
                "rates", Map.of("BRL", new BigDecimal("5.25"))
        );

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Map.class)).thenReturn(response);

        DollarServiceImpl service = new DollarServiceImpl(builderQueRetorna(restClient));

        BigDecimal result = service.getFromFrankfurter();

        assertEquals(new BigDecimal("5.25"), result);
    }

    private RestClient.Builder builderQueRetorna(RestClient restClient) {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.build()).thenReturn(restClient);
        return builder;
    }
}
