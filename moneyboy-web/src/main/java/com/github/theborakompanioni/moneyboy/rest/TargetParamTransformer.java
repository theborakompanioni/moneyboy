package com.github.theborakompanioni.moneyboy.rest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import rx.Observable;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by void on 7/25/16.
 */
class TargetParamTransformer implements Observable.Transformer<String, CurrencyUnit> {
    private static final Supplier<TreeSet<CurrencyUnit>> asTreeSet =
            () -> Sets.newTreeSet(Comparator.comparing(CurrencyUnit::getCurrencyCode));

    private static final List<CurrencyUnit> availableCurrencies = ImmutableList.copyOf(
            Arrays.stream(Locale.getAvailableLocales())
                    .map(Monetary::getCurrencies)
                    .filter(currencies -> !currencies.isEmpty())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toCollection(asTreeSet)));

    @Override
    public Observable<CurrencyUnit> call(Observable<String> param) {
        return param
                .flatMap(targetParam -> {
                    try {
                        return Observable.just(Monetary.getCurrency(targetParam));
                    } catch (Exception e) {
                        return Observable.empty();
                    }
                })
                .switchIfEmpty(Observable.from(availableCurrencies));
    }
}
