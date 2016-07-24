package com.github.theborakompanioni.moneta;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.rxjava.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

@Slf4j
public class Main extends Launcher {

    private static Vertx vertx;

    public static void main(String[] args) {
        log.info("main()");

        vertx = Vertx.vertx(getOptions());
        vertx.deployVerticle(getVerticleClassName(), getDeploymentOptions());

        Runtime.getRuntime().addShutdownHook(getShutdownHook());
    }

    private static JsonObject getConfig() {
        String descriptorFile = "conf/my-conf.json";
        try {
            try (InputStream is = Main.class.getClassLoader().getResourceAsStream(descriptorFile)) {
                if (is == null) {
                    throw new IllegalArgumentException("Cannot find service descriptor file " + descriptorFile + " on classpath");
                }
                try (Scanner scanner = new Scanner(is, Charsets.UTF_8.name()).useDelimiter("\\A")) {
                    String conf = scanner.next();
                    return new JsonObject(conf);
                } catch (NoSuchElementException e) {
                    throw new IllegalArgumentException(descriptorFile + " is empty");
                } catch (DecodeException e) {
                    throw new IllegalArgumentException(descriptorFile + " contains invalid json");
                }
            }
        } catch (IOException e) {
            log.error("Error while reading config file", e);
            throw Throwables.propagate(e);
        }
    }

    private static VertxOptions getOptions() {
        return new VertxOptions(getConfig())
                .setMetricsOptions(getMetricsRegistry());
    }

    private static MetricsOptions getMetricsRegistry() {
        return new DropwizardMetricsOptions()
                .setEnabled(true)
                .setJmxEnabled(true)
                .setRegistryName("registry");
    }

    private static String getVerticleClassName() {
        return MonetaVerticle.class.getName();
    }

    private static DeploymentOptions getDeploymentOptions() {
        return new DeploymentOptions()
                .setConfig(getConfig())
                .setWorker(false);
    }

    private static Thread getShutdownHook() {
        return new Thread() {
            public void run() {
                log.info("shutting down");
                vertx.close(event -> log.info("shutdown complete"));
            }
        };
    }
}
