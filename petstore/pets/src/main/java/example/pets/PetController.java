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
package example.pets;

import com.feedzai.commons.tracing.engine.TraceUtil;
import com.mongodb.client.model.Aggregates;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import example.api.v1.PetOperations;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.annotation.Header;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.micronaut.configuration.hystrix.annotation.HystrixCommand;
import io.micronaut.http.annotation.Controller;
import io.micronaut.validation.Validated;

import javax.inject.Singleton;
import javax.validation.Valid;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * @author graemerocher
 * @since 1.0
 */
@Controller("/${pets.api.version}/pets")
@Validated
public class PetController implements PetOperations<PetEntity> {

    private final PetsConfiguration configuration;
    private MongoClient mongoClient;

    public PetController(
            PetsConfiguration configuration,
            MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
    }

    @Override
    @HystrixCommand
    public Single<List<PetEntity>> list(@Header("uber-trace-id") String traceid) {
        return TraceUtil.instance().newProcess(() -> Flowable.fromPublisher(
                getCollection()
                        .find()
        ).toList(), "List Pets", TraceUtil.instance().deserializeContext((Serializable) Collections.singletonMap("uber-trace-id", traceid)));
    }

    @Override
    @HystrixCommand
    public Maybe<PetEntity> random(@Header("uber-trace-id") String traceid) {
        return TraceUtil.instance().newProcess(() -> Flowable.fromPublisher(
                getCollection()
                        .aggregate(Collections.singletonList(Aggregates.sample(1)), PetEntity.class)
        ).firstElement(), "Random Pet", TraceUtil.instance().deserializeContext((Serializable) Collections.singletonMap("uber-trace-id", traceid)));
    }

    @Override
    public Single<List<PetEntity>> byVendor(String name, @Header("uber-trace-id") String traceid) {
        return TraceUtil.instance().newProcess(() -> Flowable.fromPublisher(
                getCollection()
                        .find(eq("vendor", name))
        ).toList(), "Pet by Vendor: " + name, TraceUtil.instance().deserializeContext((Serializable) Collections.singletonMap("uber-trace-id", traceid)));
    }

    @Override
    public Maybe<PetEntity> find(String slug, @Header("uber-trace-id") String traceid) {
        return TraceUtil.instance().newProcess(() -> Flowable.fromPublisher(
                getCollection()
                        .find(eq("slug", slug))
                        .limit(1)
        ).firstElement(), "Find: " + slug, TraceUtil.instance().deserializeContext((Serializable) Collections.singletonMap("uber-trace-id", traceid)));
    }

    @Override
    public Single<PetEntity> save(@Valid PetEntity pet, @Header("uber-trace-id") String traceid) {
        String slug = FriendlyUrl.sanitizeWithDashes(pet.getName());
        pet.slug(slug);
        return TraceUtil.instance().newProcess(() -> find(slug, traceid)
                .switchIfEmpty(
                        Single.fromPublisher(getCollection().insertOne(pet))
                                .map(success -> pet)
                ), "Save: " + slug, TraceUtil.instance().deserializeContext((Serializable) Collections.singletonMap("uber-trace-id", traceid)));
    }

    private MongoCollection<PetEntity> getCollection() {
        return TraceUtil.instance().addToTrace(() -> mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getCollectionName(), PetEntity.class), "Get Mongo Collection");
    }
}
