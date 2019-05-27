package example.mail;


import com.feedzai.commons.tracing.engine.JaegerTracingEngine;
import com.feedzai.commons.tracing.engine.TraceUtil;
import io.micronaut.runtime.Micronaut;

import javax.inject.Singleton;
import java.time.Duration;

/**
 * @author sdelamo
 * @since 1.0
 */
public class Application {

    public static void main(String[] args) {
        TraceUtil.init(new JaegerTracingEngine.Builder().withCacheDuration(Duration.ofDays(1)).withCacheMaxSize(10000).withSampleRate(1).withProcessName("Mail").withIp("192.168.0.4").build());
        Micronaut.run(Application.class);
    }
}
