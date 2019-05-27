package example.storefront

import io.micronaut.core.convert.ArgumentConversionContext
import io.micronaut.http.HttpHeaders

class   TracingHeaders implements HttpHeaders {

    Map<String, String> contextMap

    TracingHeaders(Serializable context){
        contextMap = context;
    }

    @Override
    List<String> getAll(CharSequence name) {
        return Collections.singletonList(contextMap.get(name))
    }

    @Override
    String get(CharSequence name) {
        return contextMap.get(name)
    }

    @Override
    Set<String> names() {
        return contextMap.keySet()
    }

    @Override
    Collection<List<String>> values() {
        return contextMap.values()
    }

    @Override
    def <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
        return null;
    }
}
