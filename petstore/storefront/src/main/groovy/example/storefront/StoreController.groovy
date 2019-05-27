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
package example.storefront

import com.feedzai.commons.tracing.engine.JaegerTracingEngine
import com.feedzai.commons.tracing.engine.SpanTraceContext
import com.feedzai.commons.tracing.engine.TraceUtil
import example.api.v1.Offer
import example.api.v1.Pet
import example.api.v1.Vendor
import example.storefront.client.v1.CommentClient
import example.storefront.client.v1.PetClient
import example.storefront.client.v1.TweetClient
import example.storefront.client.v1.VendorClient
import io.micronaut.context.annotation.Parameter
import io.micronaut.core.convert.DefaultConversionService
import io.micronaut.http.HttpHeaders
import io.micronaut.http.simple.SimpleHttpHeaders
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.sse.Event

import javax.inject.Singleton
import java.time.Duration
import java.util.function.Supplier

/**
 * @author graemerocher* @since 1.0
 */
@Controller("/")
class StoreController {

    private final RxStreamingHttpClient offersClient
    private final VendorClient vendorClient
    private final PetClient petClient
    private final CommentClient commentClient
    private final TweetClient tweetClient

    StoreController(
            @Client(id = 'offers') RxStreamingHttpClient offersClient,
            VendorClient vendorClient,
            PetClient petClient,
            CommentClient commentClient,
            TweetClient tweetClient) {
        this.offersClient = offersClient
        this.vendorClient = vendorClient
        this.petClient = petClient
        this.commentClient = commentClient
        this.tweetClient = tweetClient
        TraceUtil.init(new JaegerTracingEngine.Builder().withCacheDuration(Duration.ofDays(1)).withCacheMaxSize(10000).withSampleRate(1).withProcessName("StoreFront").withIp("192.168.0.4").build())
    }

    @Produces(MediaType.TEXT_HTML)
    @Get(uri = '/')
    HttpResponse index() {
        HttpResponse.redirect(URI.create('/index.html'))
    }

    @Get(uri = "/offers", produces = MediaType.TEXT_EVENT_STREAM)
    Flowable<Event<Offer>> offers(HttpHeaders headers) {
        HttpRequest request = HttpRequest.GET('/v1/offers')
        TraceUtil.instance().newTrace({
            ((Map<String, String>) TraceUtil.instance().serializeContext()).each { k, v -> request.headers.add(k, v) }
            offersClient.jsonStream(request, Offer).map({ offer ->
                Event.of(offer)
            })
        } as Supplier, "Offers")
    }

    @Get('/pets')
    Single<List<Pet>> pets() {
        TraceUtil.instance().newTrace({
            petClient.list(((Map<String, String>) TraceUtil.instance().serializeContext()).get("uber-trace-id"))
                    .onErrorReturnItem(Collections.emptyList())
        } as Supplier, "Pets")
    }

    @Get('/pets/{slug}')
    Maybe<Pet> showPet(@Parameter('slug') String slug) {
        TraceUtil.instance().newTrace({ petClient.find(slug, ((Map<String, String>) TraceUtil.instance().serializeContext()).get("uber-trace-id")) } as Supplier, slug)
    }

    @Get('/pets/random')
    Maybe<Pet> randomPet() {
        TraceUtil.instance().newTrace({
            petClient.random(((Map<String, String>) TraceUtil.instance().serializeContext()).get("uber-trace-id"))
        } as Supplier, "Random Pet", )
    }


    @Get('/pets/vendor/{vendor}')
    Single<List<Pet>> petsForVendor(String vendor) {
        TraceUtil.instance().newTrace({
            Map<String, String> headers = ((Map<String, String>) TraceUtil.instance().serializeContext())
            petClient.byVendor(vendor, headers.get("uber-trace-id"))
                    .onErrorReturnItem(Collections.emptyList())
        } as Supplier, "Pets for Vendor: " + vendor)
    }


    @Get('/vendors')
    Single<List<Vendor>> vendors() {
        TraceUtil.instance().newTrace({
            vendorClient.list(((Map<String, String>) TraceUtil.instance().serializeContext()).get("uber-trace-id"))
                    .onErrorReturnItem(Collections.emptyList())
        } as Supplier, "Vendors", )
    }

}
