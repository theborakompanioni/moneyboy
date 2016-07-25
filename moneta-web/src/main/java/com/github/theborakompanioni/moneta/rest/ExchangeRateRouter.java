package com.github.theborakompanioni.moneta.rest;

import com.github.theborakompanioni.moneta.ExchangeRateResponseImpl;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.Router;
import org.javamoney.moneta.FastMoney;
import rx.Observable;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.MonetaryConversions;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

class ExchangeRateRouter {

    private static ExchangeRateProvider rateProvider = MonetaryConversions.getExchangeRateProvider();

    static Router create(Vertx vertx) {
        Supplier<TreeSet<CurrencyUnit>> asTreeSet =
                () -> Sets.newTreeSet(Comparator.comparing(CurrencyUnit::getCurrencyCode));

        final List<CurrencyUnit> availableCurrencies = ImmutableList.copyOf(
                Arrays.stream(Locale.getAvailableLocales())
                        .map(Monetary::getCurrencies)
                        .filter(currencies -> !currencies.isEmpty())
                        .flatMap(Collection::stream)
                        .collect(Collectors.toCollection(asTreeSet)));

        Router router = Router.router(vertx);

        router.route("/").handler(ctx -> {
            final JsonObject json = new JsonObject()
                    .put("providerNames", MonetaryConversions.getConversionProviderNames())
                    .put("providerChain", MonetaryConversions.getDefaultConversionProviderChain());

            ctx.response().setStatusCode(OK.code());
            ctx.response().end(json.toString());
        });

        router.route("/latest").handler(ctx -> {
            final String baseParam = Optional.ofNullable(ctx.request().getParam("base"))
                    .orElse("EUR");

            final CurrencyUnit baseCurrency = Monetary.getCurrency(baseParam);

            final FastMoney amount = FastMoney.of(1, baseCurrency);
            Observable.from(ctx.request().params().getAll("target"))
                    .flatMap(targetParam -> {
                        try {
                            return Observable.just(Monetary.getCurrency(targetParam));
                        } catch (Exception e) {
                            return Observable.empty();
                        }
                    })
                    .switchIfEmpty(Observable.from(availableCurrencies))
                    .filter(currency -> rateProvider.isAvailable(amount.getCurrency(), currency))
                    .map(currency -> rateProvider.getCurrencyConversion(currency))
                    .map(currencyConversion -> {
                        try {
                            return currencyConversion.getExchangeRate(amount);
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
                            .build())
                    .map(Json::encode)
                    .subscribe(json -> {
                        ctx.response().setStatusCode(OK.code());
                        ctx.response().end(json);
                    });
        });


        router.route("/:date").handler(ctx -> {
            final LocalDate now = LocalDate.now();
            final LocalDate date = Optional.ofNullable(ctx.request().getParam("date"))
                    .map(val -> LocalDate.parse(val, DateTimeFormatter.ISO_DATE))
                    .orElse(now);

            if (date.isAfter(now)) {
                ctx.fail(BAD_REQUEST.code());
                return;
            }

            ctx.response().setStatusCode(ACCEPTED.code());
            ctx.response().end(now.toString());
        });

        return router;
    }
}
