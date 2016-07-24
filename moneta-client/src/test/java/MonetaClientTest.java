import com.github.theborakompanioni.moneta.MonetaClient;
import com.github.theborakompanioni.moneta.MonetaClients;
import com.github.theborakompanioni.moneta.MonetaVerticle;
import io.vertx.core.DeploymentOptions;
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

import javax.money.convert.MonetaryConversions;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(VertxUnitRunner.class)
public class MonetaClientTest {

    private static Vertx vertx;
    private static MonetaClient api;

    @BeforeClass
    public static void setUp(TestContext context) {
        int portRest = TestUtils.getRandomPort().orElse(9000);

        vertx = Vertx.vertx();
        vertx.deployVerticle(MonetaVerticle.class.getName(),
                new DeploymentOptions()
                        .setConfig(new JsonObject()
                                .put("http.rest.port", portRest)),
                context.asyncAssertSuccess());

        api = MonetaClients.create("http://localhost:" + portRest);
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

        Observable.fromCallable(() -> MonetaClientTest.api.api())
                .subscribe(next -> {
                    assertThat(next, is(notNullValue()));
                    assertThat(next.get("msg"), is(equalTo("Hello World!")));
                }, context::fail, async::complete);
    }

    @Test
    public void itShouldFetcExchangeRate(TestContext context) {
        final Async async = context.async();

        Observable.fromCallable(() -> api.exchangerateLatest())
                .subscribe(next -> {
                    assertThat(next, is(notNullValue()));

                }, context::fail, async::complete);
    }

    @Test
    public void itShouldFetcExchangeRateWithParams(TestContext context) {
        final Async async = context.async();

        Observable.fromCallable(() -> api.exchangerateLatest("USD"))
                .subscribe(next -> {
                    assertThat(next, is(notNullValue()));

                }, context::fail, async::complete);
    }

}
