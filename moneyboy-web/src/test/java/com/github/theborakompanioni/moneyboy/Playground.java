package com.github.theborakompanioni.moneyboy;


import javax.money.convert.ExchangeRate;
import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.MonetaryConversions;

class Playground {

    public static void main(String args[]) {
        ExchangeRateProvider rateProvider = MonetaryConversions.getExchangeRateProvider();
        ExchangeRate eurToChfRate = rateProvider.getExchangeRate("EUR", "CHF");

        final String providerName = eurToChfRate.getContext().getProviderName();

    }
}
