package com.github.theborakompanioni.moneta.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.net.MediaType;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.*;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

@Slf4j
public class RestServerVerticle extends AbstractVerticle {

    static {
        Json.mapper.registerModule(new JavaTimeModule());
        Json.mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        Json.mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        Json.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static final String JSON_TYPE = MediaType.JSON_UTF_8.withoutParameters().toString();

    private final String name = this.getClass().getSimpleName();

    @Override
    public void start(Future<Void> startFuture) {
        log.info("Starting {}...", name);

        startHttpServer()
                .doOnError(error -> log.error("{}", error))
                .subscribe(server -> {
                            log.info("Starten {} on port {}", name, server.actualPort());
                        }, startFuture::fail,
                        startFuture::complete);
    }

    @Override
    public void stop(Future<Void> stopFuture) {
        log.info("stopped {}", name);
        stopFuture.complete();
    }

    private Observable<HttpServer> startHttpServer() {
        int port = port();
        log.info("Starting http server...", name);

        Router router = router();
        return vertx.createHttpServer(new HttpServerOptions())
                .requestHandler(router::accept)
                .listenObservable(port);
    }

    private int port() {
        return config().getInteger("http.rest.port", 8080);
    }

    private Router router() {
        Router router = Router.router(vertx);

        router.route().failureHandler(failureHandler());
        router.route().handler(cookieHandler());
        router.route().handler(timeoutHandler());
        router.route().handler(responseTimeHandler());

        router.route().handler(loggerHandler());
        router.route().handler(bodyHandler());

        router.route("/api")
                .consumes(JSON_TYPE)
                .produces(JSON_TYPE);

        router.route("/api").handler(api());
        router.mountSubRouter("/api/exchange", ExchangeRateRouter.create(vertx));

        router.route().handler(notFoundHandler());

        return router;
    }

    private CookieHandler cookieHandler() {
        return CookieHandler.create();
    }

    private Handler<RoutingContext> notFoundHandler() {
        return ctx -> {
            ctx.fail(NOT_FOUND.code());
        };
    }

    private LoggerHandler loggerHandler() {
        return LoggerHandler.create();
    }

    private ErrorHandler failureHandler() {
        boolean displayExceptionDetails = false;
        return ErrorHandler.create(displayExceptionDetails);
    }

    private ResponseTimeHandler responseTimeHandler() {
        return ResponseTimeHandler.create();
    }

    private TimeoutHandler timeoutHandler() {
        return TimeoutHandler.create(30_000);
    }

    private BodyHandler bodyHandler() {
        return BodyHandler.create();
    }

    private Handler<RoutingContext> api() {
        return routingContext -> routingContext.response()
                .end(new JsonObject().put("msg", "Hello World!").toString());
    }
}

