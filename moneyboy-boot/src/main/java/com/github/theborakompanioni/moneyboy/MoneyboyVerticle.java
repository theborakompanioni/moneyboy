package com.github.theborakompanioni.moneyboy;

import com.github.theborakompanioni.moneyboy.rest.RestServerVerticle;
import com.google.common.collect.ImmutableList;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.rxjava.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

@Slf4j
public class MoneyboyVerticle extends AbstractVerticle {

    private final String name = this.getClass().getName();

    @Override
    public void start(Future<Void> startFuture) {
        log.info("Starting {}...", name);
        ImmutableList.Builder<Observable<String>> builder = ImmutableList.<Observable<String>>builder()
                .add(startRestServer());

        Observable.merge(builder.build())
                .subscribe(name -> {
                    log.info("Deployed {}", name);
                }, error -> {
                    log.error("Deploy main verticle failed", error);
                    startFuture.fail(error);
                }, startFuture::complete);
    }

    @Override
    public void stop(Future<Void> stopFuture) {
        log.info("stopped {}", name);
        stopFuture.complete();
    }

    private Observable<String> startRestServer() {
        return vertx.executeBlockingObservable(future -> {
            vertx.deployVerticleObservable(RestServerVerticle.class.getName(),
                    new DeploymentOptions().setConfig(this.config()))
                    .subscribe(action -> future.complete(RestServerVerticle.class.getName()), future::fail);
        });
    }

}

