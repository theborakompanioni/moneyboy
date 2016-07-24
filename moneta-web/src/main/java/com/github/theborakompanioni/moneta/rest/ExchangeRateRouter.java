package com.github.theborakompanioni.moneta.rest;

import com.google.common.base.Strings;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;

import javax.money.convert.ExchangeRate;
import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.MonetaryConversions;
import java.util.Optional;
import java.util.function.Function;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

class ExchangeRateRouter {

    private static ExchangeRateProvider rateProvider = MonetaryConversions.getExchangeRateProvider();

    static Router create(Vertx vertx) {
        String currencyParamName = "currency";
        Function<RoutingContext, Optional<String>> currencyParam = ctx ->
                Optional.ofNullable(ctx.request().getParam(currencyParamName))
                        .map(Strings::emptyToNull);

        Router router = Router.router(vertx);

        router.route("/").handler(ctx -> {
            final JsonObject json = new JsonObject()
                    .put("providerNames", MonetaryConversions.getConversionProviderNames())
                    .put("providerChain", MonetaryConversions.getDefaultConversionProviderChain());

            ctx.response().setStatusCode(OK.code());
            ctx.response().end(json.toString());
        });

        router.route("/latest").handler(ctx -> {
            final String base = Optional.ofNullable(ctx.request().getParam("base"))
                    .orElse("EUR");

            final String target = Optional.ofNullable(ctx.request().getParam("target"))
                    .orElse("USD");

            ExchangeRate eurToChfRate = rateProvider.getExchangeRate(base, target);

            final JsonObject json = new JsonObject()
                    .put("factor", eurToChfRate.getFactor().doubleValueExact())
                    .put("base", eurToChfRate.getBaseCurrency().getCurrencyCode())
                    .put("target", eurToChfRate.getCurrency().getCurrencyCode())
                    .put("provider", eurToChfRate.getContext().getProviderName())
                    .put("type", eurToChfRate.getContext().getRateType().name());

            ctx.response().setStatusCode(OK.code());
            ctx.response().end(json.toString());
        });


        router.route("/:" + currencyParamName + "/rate").handler(ctx -> {
            final Optional<String> currency = currencyParam.apply(ctx);

            if (!currency.isPresent()) {
                ctx.fail(BAD_REQUEST.code());
                return;
            }

            ctx.response().setStatusCode(ACCEPTED.code());
            ctx.response().end(currency.get());
        });

        return router;
    }
}
