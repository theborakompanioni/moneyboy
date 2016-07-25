package com.github.theborakompanioni.moneta;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Headers({
        "Accept: application/json"
})
public interface MonetaClient {

    @RequestLine("GET /api")
    Map<String, Object> api();

    @RequestLine("GET /api/exchangerate/latest")
    ExchangeRateResponse exchangerateLatest();

    @RequestLine("GET /api/exchangerate/latest?base={base}")
    ExchangeRateResponse exchangerateLatest(@Param(value = "base") String base);

    @RequestLine("GET /api/exchangerate/latest?base={base}&target={target}")
    ExchangeRateResponse exchangerateLatest(
            @Param(value = "base") String base,
            @Param(value = "target") String target);

    @RequestLine("GET /api/exchangerate/latest?base={base}&target={targets}")
    ExchangeRateResponse exchangerateLatest(
            @Param(value = "base") String base,
            @Param(value = "targets") List<String> targets);

    @RequestLine("GET /api/exchangerate/{date}")
    ExchangeRateResponse exchangeRateOnDate(@Param(value = "date") LocalDate date);

    @RequestLine("GET /api/exchangerate/{date}?base={base}")
    ExchangeRateResponse exchangeRateOnDate(
            @Param(value = "date") LocalDate date,
            @Param(value = "base") String base);

    @RequestLine("GET /api/exchangerate/{date}?base={base}&target={target}")
    ExchangeRateResponse exchangeRateOnDate(
            @Param(value = "date") LocalDate date,
            @Param(value = "base") String base,
            @Param(value = "target") String target);

    @RequestLine("GET /api/exchangerate/{date}?base={base}&target={targets}")
    ExchangeRateResponse exchangeRateOnDate(
            @Param(value = "date") LocalDate date,
            @Param(value = "base") String base,
            @Param(value = "targets") List<String> targets);

}
