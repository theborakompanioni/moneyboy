package com.github.theborakompanioni.moneta;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.theborakompanioni.moneta.ExchangeRateResponseImpl.ExchangeRateImpl;

@JsonDeserialize(as = ExchangeRateImpl.class)
public interface ExchangeRate {
    String getBase();

    boolean isDerived();

    double getFactor();

    String getProvider();

    String getTarget();

    String getType();
}
