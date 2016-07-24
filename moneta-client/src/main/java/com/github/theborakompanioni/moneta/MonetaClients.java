package com.github.theborakompanioni.moneta;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.Feign;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;

public final class MonetaClients {
    private MonetaClients() {
        throw new UnsupportedOperationException();
    }

    public static MonetaClient create(String baseUrl) {
        ObjectMapper mapper = objectMapper();

        return Feign.builder()
                .retryer(new Retryer.Default())
                .logger(new Slf4jLogger())
                .encoder(new JacksonEncoder(mapper))
                .decoder(new JacksonDecoder(mapper))
                .target(MonetaClient.class, baseUrl);
    }

    private static ObjectMapper objectMapper() {
        return new ObjectMapper()
                //.registerModule(new JavaTimeModule())
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
