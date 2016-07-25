package com.github.theborakompanioni.moneta.rest;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import javax.money.CurrencyUnit;
import javax.money.UnknownCurrencyException;
import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.MonetaryConversions;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static javax.money.Monetary.getCurrency;

@Slf4j
class ExchangeRateRouter {

    private static final ExchangeRateProvider rateProvider = MonetaryConversions.getExchangeRateProvider();

    private static TargetParamTransformer toCurrencyUnit = new TargetParamTransformer();

    static Router create(Vertx vertx) {
        Router router = Router.router(vertx);

        router.route("/").handler(ctx -> {
            final JsonObject json = new JsonObject()
                    .put("providerNames", MonetaryConversions.getConversionProviderNames())
                    .put("providerChain", MonetaryConversions.getDefaultConversionProviderChain());

            ctx.response().setStatusCode(OK.code());
            ctx.response().end(json.toString());
        });

        router.route("/latest").handler(ctx -> {
            final Optional<CurrencyUnit> baseCurrency = baseParam(ctx);
            if (!baseCurrency.isPresent()) {
                ctx.fail(BAD_REQUEST.code());
                return;
            }

            final List<String> targetParams = ctx.request().params().getAll("target");

            final ExchangeRateResponseTransformer toCurrentExchangeRate = ExchangeRateResponseTransformer
                    .ofTodayWithSlidingWindow(rateProvider, baseCurrency.get());

            Observable.from(targetParams)
                    .compose(toCurrencyUnit)
                    .compose(toCurrentExchangeRate)
                    .map(Json::encode)
                    .subscribe(json -> {
                        ctx.response().setStatusCode(OK.code());
                        ctx.response().end(json);
                    });
        });

        router.route("/:date").handler(ctx -> {
            final Optional<CurrencyUnit> baseCurrency = baseParam(ctx);

            if (!baseCurrency.isPresent()) {
                ctx.fail(BAD_REQUEST.code());
                return;
            }

            final LocalDate now = LocalDate.now();
            final LocalDate date = ofNullable(ctx.request().getParam("date"))
                    .map(val -> LocalDate.parse(val, DateTimeFormatter.ISO_DATE))
                    .orElse(now);

            if (date.isAfter(now)) {
                ctx.fail(BAD_REQUEST.code());
                return;
            }
            final List<String> targetParams = ctx.request().params().getAll("target");

            final ExchangeRateResponseTransformer toHistoryExchangeRate = ExchangeRateResponseTransformer
                    .withSlidingWindow(rateProvider, baseCurrency.get(), date);

            Observable.from(targetParams)
                    .compose(toCurrencyUnit)
                    .compose(toHistoryExchangeRate)
                    .map(Json::encode)
                    .subscribe(json -> {
                        ctx.response().setStatusCode(OK.code());
                        ctx.response().end(json);
                    });
        });

        return router;
    }

    private static Optional<CurrencyUnit> baseParam(RoutingContext ctx) {
        try {
            return of(getCurrency(ofNullable(ctx.request().getParam("base")).orElse("EUR")));
        } catch (UnknownCurrencyException e) {
            log.debug("Unknown currency: {}", e.getCurrencyCode());
            return Optional.empty();
        }
    }

}
