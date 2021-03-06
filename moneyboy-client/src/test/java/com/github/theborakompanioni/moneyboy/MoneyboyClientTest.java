package com.github.theborakompanioni.moneyboy;

import com.google.common.collect.ImmutableList;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import rx.Observable;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(VertxUnitRunner.class)
public class MoneyboyClientTest {

    private static Vertx vertx;
    private static MoneyboyClient api;

    @BeforeClass
    public static void setUp(TestContext context) {
        int portRest = TestUtils.getRandomPort().orElse(9000);

        vertx = Vertx.vertx();
        vertx.deployVerticle(MoneyboyVerticle.class.getName(),
                new DeploymentOptions()
                        .setConfig(new JsonObject()
                                .put("http.rest.port", portRest)),
                context.asyncAssertSuccess());

        api = MoneyboyClients.create("http://localhost:" + portRest);
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void itShouldBeAbleToCreateAnInstance() {
        assertThat(api, is(not(nullValue())));
    }

    @Test
    public void itShouldFetchApiEntry(TestContext context) {
        final Async async = context.async();

        Observable.just(MoneyboyClientTest.api.api())
                .subscribe(next -> {
                    assertThat(next, is(notNullValue()));
                    assertThat(next.get("msg"), is(equalTo("Hello World!")));
                }, context::fail, async::complete);
    }

    @Test
    public void itShouldFetchExchangeRate(TestContext context) {
        final Async async = context.async();

        Observable.just(api.latest())
                .subscribe(next -> {
                    assertThat(next.getBase(), is(equalTo("EUR")));
                    assertThat(next.getDate(), is(equalTo(LocalDate.now())));
                    assertThat(next.getRates(), hasSize(greaterThan(30)));
                }, context::fail, async::complete);
    }

    @Test
    public void itShouldFetchExchangeRateWithTarget(TestContext context) {
        final Async async = context.async();

        Observable.just(api.latest("USD", "EUR"))
                .subscribe(next -> {
                    assertThat(next, is(notNullValue()));
                    assertThat(next.getBase(), is(equalTo("USD")));
                    assertThat(next.getDate(), is(equalTo(LocalDate.now())));
                    assertThat(next.getRates(), hasSize(1));
                    assertThat(next.getRates().get(0).getTarget(), is(equalTo("EUR")));

                    log.info("{}", Json.encode(next));

                }, context::fail, async::complete);
    }

    @Test
    public void itShouldFetchExchangeRateWithTargets(TestContext context) {
        final Async async = context.async();

        final ImmutableList<String> targets = ImmutableList.of("GBP", "EUR", "NZD", "RUB", "USD");

        Observable.just(api.latest("EUR", targets))
                .subscribe(next -> {
                    assertThat(next, is(notNullValue()));
                    assertThat(next.getDate(), is(equalTo(LocalDate.now())));
                    assertThat(next.getBase(), is(equalTo("EUR")));
                    assertThat(next.getRates(), hasSize(targets.size()));

                }, context::fail, async::complete);
    }

    @Test
    public void itShouldFetchExchangeOfPastDate(TestContext context) {
        final Async async = context.async();

        final LocalDate date = LocalDate.now()
                .minusDays(ThreadLocalRandom.current().nextLong(30, 120));

        final ImmutableList<String> targets = ImmutableList.of("GBP", "USD");

        Observable.just(api.historic(date, "EUR", targets))
                .subscribe(next -> {
                    assertThat(next, is(notNullValue()));
                    assertThat(next.getBase(), is(equalTo("EUR")));
                    assertThat(next.getDate(), is(equalTo(date)));
                    assertThat(next.getRates(), hasSize(targets.size()));

                    assertThat(next.getRates().get(0).getTarget(), is(equalTo(targets.get(0))));
                    assertThat(next.getRates().get(0).getType(), is("HISTORIC"));

                    assertThat(next.getRates().get(1).getTarget(), is(equalTo(targets.get(1))));
                    assertThat(next.getRates().get(1).getType(), is("HISTORIC"));

                    log.info("{}", Json.encode(next));

                }, context::fail, async::complete);
    }
}
