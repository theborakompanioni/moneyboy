package com.github.theborakompanioni.moneta;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

@JsonDeserialize(as = ExchangeRateResponseImpl.class)
public interface ExchangeRateResponse {
    String getBase();

    List<? extends ExchangeRate> getRates();
}
