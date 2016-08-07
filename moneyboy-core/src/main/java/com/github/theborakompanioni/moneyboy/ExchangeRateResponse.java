package com.github.theborakompanioni.moneyboy;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDate;
import java.util.List;

@JsonDeserialize(as = ExchangeRateResponseImpl.class)
public interface ExchangeRateResponse {
    String getBase();

    LocalDate getDate();

    List<? extends ExchangeRate> getRates();
}
