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


import com.feedzai.commons.tracing.engine.JaegerTracingEngine;
import com.feedzai.commons.tracing.engine.TraceUtil;
import io.micronaut.runtime.Micronaut;

import javax.inject.Singleton;

/**
 * @author graemerocher
 * @since 1.0
 */
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.info.*;
import io.swagger.v3.oas.annotations.tags.*;

import java.time.Duration;

@OpenAPIDefinition(
    info = @Info(
            title = "Pets",
            version = "0.0",
            description = "Exposes information about Pets",
            license = @License(name = "Apache 2.0", url = "http://petstore.notreal")
    ),
    tags = {
            @Tag(name = "Dogs"),
            @Tag(name = "Cats")
    }
)
@Singleton
public class Application {

    public static void main(String[] args) {
        TraceUtil.init(new JaegerTracingEngine.Builder().withCacheDuration(Duration.ofDays(1)).withCacheMaxSize(10000).withSampleRate(1).withProcessName("Pets").withIp("192.168.0.4").build());
        Micronaut.run(Application.class);
    }
}
