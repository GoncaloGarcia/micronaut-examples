/*
 * Copyright 2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example.offers;

import com.feedzai.commons.tracing.api.TraceContext;
import com.feedzai.commons.tracing.engine.JaegerTracingEngine;
import com.feedzai.commons.tracing.engine.TraceUtil;
import example.api.v1.Offer;
import io.jaegertracing.internal.utils.Http;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.validation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.inject.Singleton;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author graemerocher
 * @since 1.0
 */
@Controller("/${offers.api.version}/offers")
@Validated
public class OffersController implements OffersOperations {

    private final OffersRepository offersRepository;
    private final Duration offerDelay;


    public OffersController(OffersRepository offersRepository, @Value("${offers.delay:3s}") Duration offerDelay) {
        this.offersRepository = offersRepository;
        this.offerDelay = offerDelay;
        TraceUtil.init(new JaegerTracingEngine.Builder().withCacheDuration(Duration.ofDays(1)).withCacheMaxSize(10000).withSampleRate(1).withProcessName("Offers").withIp("192.168.0.4").build());
    }

    /**
     * A non-blocking infinite JSON stream of offers that change every 10 seconds
     *
     * @return A {@link Flux} stream of JSON objects
     */
    @Get(uri = "/", produces = MediaType.APPLICATION_JSON_STREAM)
    public Flux<Offer> current(HttpHeaders headers) {
        TraceContext ctx = TraceUtil.instance().deserializeContext((Serializable) headers.asMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0))));
        return TraceUtil.instance().newProcess(() -> offersRepository
                .random()
                .repeat(100)
                .delayElements(offerDelay), "Currents Offer", ctx);
    }

    /**
     * A non-blocking infinite JSON stream of offers that change every 10 seconds
     *
     * @return A {@link Flux} stream of JSON objects
     */
    @Get(uri = "/all")
    public Mono<List<Offer>> all(HttpHeaders headers) {
        return TraceUtil.instance().newProcess(() -> offersRepository.all(), "All Offers", TraceUtil.instance().deserializeContext((Serializable) headers.asMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
    }

    /**
     * Consumes JSON and saves a new offer
     *
     * @param slug        Pet's slug
     * @param price       The price of the offer
     * @param duration    The duration of the offer
     * @param description The description of the offer
     * @return The offer or 404 if no pet exists for the offer
     */
    @Post("/")
    @Override
    public Mono<Offer> save(
            String slug,
            BigDecimal price,
            Duration duration,
            String description
    ) {
        return TraceUtil.instance().addToTrace(() -> offersRepository.save(
                slug, price, duration, description
        ), "Saving offer for: " + slug);
    }
}
