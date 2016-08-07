package com.github.theborakompanioni.moneyboy;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Headers({
        "Accept: application/json"
})
public interface MoneyboyClient {

    @RequestLine("GET /api")
    Map<String, Object> api();

    @RequestLine("GET /api/exchange/latest")
    ExchangeRateResponse latest();

    @RequestLine("GET /api/exchange/latest?base={base}")
    ExchangeRateResponse latest(@Param(value = "base") String base);

    @RequestLine("GET /api/exchange/latest?base={base}&target={target}")
    ExchangeRateResponse latest(
            @Param(value = "base") String base,
            @Param(value = "target") String target);

    @RequestLine("GET /api/exchange/latest?base={base}&target={targets}")
    ExchangeRateResponse latest(
            @Param(value = "base") String base,
            @Param(value = "targets") List<String> targets);

    @RequestLine("GET /api/exchange/{date}")
    ExchangeRateResponse historic(@Param(value = "date") LocalDate date);

    @RequestLine("GET /api/exchange/{date}?base={base}")
    ExchangeRateResponse historic(
            @Param(value = "date") LocalDate date,
            @Param(value = "base") String base);

    @RequestLine("GET /api/exchange/{date}?base={base}&target={target}")
    ExchangeRateResponse historic(
            @Param(value = "date") LocalDate date,
            @Param(value = "base") String base,
            @Param(value = "target") String target);

    @RequestLine("GET /api/exchange/{date}?base={base}&target={targets}")
    ExchangeRateResponse historic(
            @Param(value = "date") LocalDate date,
            @Param(value = "base") String base,
            @Param(value = "targets") List<String> targets);

}
