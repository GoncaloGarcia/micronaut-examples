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
package example.api.v1;

import io.jaegertracing.internal.utils.Http;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.annotation.Header;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.validation.Validated;

import javax.validation.Valid;
import java.util.List;

/**
 * @author graemerocher
 * @since 1.0
 */
@Validated
public interface PetOperations<T extends Pet> {

    @Get("/")
    Single<List<T>> list(@Header("uber-trace-id") String traceId);

    @Get("/random")
    Maybe<T> random(@Header("uber-trace-id") String traceid);

    @Get("/vendor/{name}")
    Single<List<T>> byVendor(String name, @Header("uber-trace-id") String traceId);

    @Get("/{slug}")
    Maybe<T> find(String slug, @Header("uber-trace-id") String traceId);

    @Post("/")
    Single<T> save(@Valid @Body T pet, @Header("uber-trace-id") String traceId);
}
