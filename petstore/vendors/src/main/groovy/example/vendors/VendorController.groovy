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
package example.vendors

import com.feedzai.commons.tracing.engine.JaegerTracingEngine
import com.feedzai.commons.tracing.engine.TraceUtil
import example.api.v1.Pet
import example.vendors.client.v1.PetClient
import io.micronaut.core.convert.DefaultConversionService
import io.micronaut.http.annotation.Header
import io.micronaut.http.simple.SimpleHttpHeaders
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.validation.Validated
import org.apache.commons.collections.map.DefaultedMap

import javax.inject.Singleton
import javax.validation.constraints.NotBlank
import java.time.Duration
import java.util.function.Supplier

/**
 * @author graemerocher
 * @since 1.0
 */
@Controller('/${vendors.api.version}/vendors')
@Validated
class VendorController {

    final VendorService vendorService
    final PetClient petClient

    VendorController(VendorService vendorService, PetClient petClient) {
        this.vendorService = vendorService
        this.petClient = petClient
        TraceUtil.init(new JaegerTracingEngine.Builder().withCacheDuration(Duration.ofDays(1)).withCacheMaxSize(10000).withSampleRate(1).withProcessName("Vendor").withIp("192.168.0.4").build())

    }

    @Get('/')
    Single<List<Vendor>> list(@Header("uber-trace-id") String traceid) {
        return TraceUtil.instance().newProcess({
            String newTraceId = ((Map<String, String>) TraceUtil.instance().serializeContext()).get("uber-trace-id")
            Single.fromCallable({-> vendorService.list() })
              .subscribeOn(Schedulers.io())
              .toFlowable()
              .flatMap({ List<Vendor> list ->
            Flowable.fromIterable(list)
        })
        .flatMap({ Vendor v ->
            petClient.byVendor(v.name, newTraceId).map({ List<Pet> pets ->
                return v.pets(pets)
            }).toFlowable()
        })
        .toList() } as Supplier, "Vendor List",  TraceUtil.instance().deserializeContext((Serializable) Collections.singletonMap("uber-trace-id", traceid)))

    }

    @Get('/names')
    List<String> names(@Header("uber-trace-id") String traceid) {
        TraceUtil.instance().newProcess({vendorService.listVendorName()} as Supplier, "Vendor Names",  TraceUtil.instance().deserializeContext((Serializable) Collections.singletonMap("uber-trace-id", traceid)))
    }

    @Post('/')
    Vendor save(@NotBlank String name, @Header("uber-trace-id") String traceid) {
        TraceUtil.instance().newProcess({vendorService.findOrCreate(name)} as Supplier, "Vendor Save",  TraceUtil.instance().deserializeContext((Serializable) Collections.singletonMap("uber-trace-id", traceid)))
    }
}
