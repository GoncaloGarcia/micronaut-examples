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
package example.storefront.client.v1.fallback

import example.api.v1.Pet
import example.api.v1.PetOperations
import groovy.transform.CompileStatic
import io.micronaut.http.HttpHeaders
import io.micronaut.http.annotation.Header
import io.reactivex.Maybe
import io.reactivex.Single
import io.micronaut.http.annotation.Body
import io.micronaut.retry.annotation.Fallback

import javax.validation.Valid

/**
 * @author graemerocher
 * @since 1.0
 */
@Fallback
@CompileStatic
class PetClientFallback implements PetOperations<Pet> {
    @Override
    Single<List<Pet>> list(@Header("uber-trace-id") String traceid) {
        return Single.just([] as List<Pet>)
    }

    @Override
    Single<List<Pet>> byVendor(String name, @Header("uber-trace-id") String traceid) {
        return list(traceid)
    }

    @Override
    Maybe<Pet> random(@Header("uber-trace-id") String traceid) {
        return Maybe.empty()
    }

    @Override
    Maybe<Pet> find(String slug, @Header("uber-trace-id") String traceid) {
        return Maybe.empty()
    }

    @Override
    Single<Pet> save(@Valid @Body Pet pet, @Header("uber-trace-id") String traceid) {
        return Single.just(pet)
    }
}
