package com.github.theborakompanioni.moneta.rest;

import com.github.theborakompanioni.moneta.ExchangeRateResponse;
import com.github.theborakompanioni.moneta.ExchangeRateResponseImpl;
import lombok.Builder;
import rx.Observable;

import javax.money.CurrencyUnit;
import javax.money.convert.ConversionQueryBuilder;
import javax.money.convert.ExchangeRateProvider;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.IntStream;

class ExchangeRateResponseTransformer implements Observable.Transformer<CurrencyUnit, ExchangeRateResponse> {
    static ExchangeRateResponseTransformer ofTodayWithSlidingWindow(ExchangeRateProvider provider, CurrencyUnit baseCurrency) {
        return withSlidingWindow(provider, baseCurrency, LocalDate.now());
    }

    static ExchangeRateResponseTransformer withSlidingWindow(ExchangeRateProvider provider, CurrencyUnit baseCurrency, LocalDate localDate) {
        return withSlidingWindow(provider, baseCurrency, localDate, 4);
    }

    static ExchangeRateResponseTransformer withSlidingWindow(ExchangeRateProvider provider, CurrencyUnit baseCurrency, LocalDate localDate, int days) {
        final LocalDate[] localDates = IntStream.range(0, days)
                .boxed()
                .map(localDate::minusDays)
                .sorted(Comparator.<LocalDate>naturalOrder().reversed())
                .toArray(LocalDate[]::new);

        return ExchangeRateResponseTransformer.builder()
                .provider(provider)
                .baseCurrency(baseCurrency)
                .localDates(localDates)
                .build();
    }

    private final ExchangeRateProvider provider;
    private final CurrencyUnit baseCurrency;
    private final LocalDate[] localDates;

    @Builder
    public ExchangeRateResponseTransformer(ExchangeRateProvider provider, CurrencyUnit baseCurrency, LocalDate... localDates) {
        this.provider = provider;
        this.baseCurrency = baseCurrency;
        this.localDates = localDates;
    }

    @Override
    public Observable<ExchangeRateResponse> call(Observable<CurrencyUnit> currencyUnits) {
        return currencyUnits.filter(currency -> provider.isAvailable(baseCurrency, currency))
                .map(currency -> ConversionQueryBuilder.of()
                        .setBaseCurrency(baseCurrency)
                        .setTermCurrency(currency)
                        .set(localDates)
                        .build())
                .map(conversionQuery -> {
                    try {
                        return provider.getExchangeRate(conversionQuery);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(exchangeRate -> ExchangeRateResponseImpl.ExchangeRateImpl.builder()
                        .factor(exchangeRate.getFactor().doubleValueExact())
                        .base(exchangeRate.getBaseCurrency().getCurrencyCode())
                        .target(exchangeRate.getCurrency().getCurrencyCode())
                        .provider(exchangeRate.getContext().getProviderName())
                        .derived(exchangeRate.isDerived())
                        .type(exchangeRate.getContext().getRateType().name())
                        .build())
                .toList()
                .map(rates -> ExchangeRateResponseImpl.builder()
                        .base(baseCurrency.getCurrencyCode())
                        .rates(rates)
                        .build());
    }
}
