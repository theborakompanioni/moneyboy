package com.github.theborakompanioni.moneta;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.time.LocalDateTime;
import java.util.Map;

@Headers({
        "Accept: application/json"
})
public interface MonetaClient {

    @RequestLine("GET /api")
    Map<String, Object> api();

    @RequestLine("GET /api/exchangerate/latest")
    Map<String, Object> exchangerateLatest();

    @RequestLine("GET /api/exchangerate/latest?base={base}")
    Map<String, Object> exchangerateLatest(@Param(value = "base") String base);

    @RequestLine("GET /api/exchangerate/{date}")
    Map<String, Object> exchangeRateOnDate(@Param(value = "date") LocalDateTime date);

    @RequestLine("GET /api/exchangerate/{date}?base={base}")
    Map<String, Object> exchangeRateOnDate(@Param(value = "date") LocalDateTime date, @Param(value = "base") String base);

}
