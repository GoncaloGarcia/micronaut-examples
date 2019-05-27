package example.storefront

import com.feedzai.commons.tracing.engine.TraceUtil
import example.api.v1.HealthStatus
import example.storefront.client.v1.Comment
import example.storefront.client.v1.CommentClient
import groovy.transform.CompileStatic
import io.reactivex.Single
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post

import javax.inject.Inject
import javax.inject.Singleton
import java.util.function.Supplier

/**
 * @author zacharyklein* @since 1.0
 */
@Singleton
@Controller("/comment")
@CompileStatic
class CommentController {

    final CommentClient commentClient

    CommentController(CommentClient commentClient) {
        this.commentClient = commentClient
    }

    @Get('/health')
    Single<HealthStatus> health() {
        commentClient.health().onErrorReturn({ new HealthStatus('DOWN') })
    }

    @Get('/{topic}')
    List<Comment> topics(String topic) {
        TraceUtil.instance().newTrace({
            commentClient.list topic, ((Map<String, String>) TraceUtil.instance().serializeContext()).get("uber-trace-id")
        } as Supplier, "Topics")
    }

    @Get('/{topic}/{id}')
    Map<String, Object> thread(String topic, Long id) {
        TraceUtil.instance().newTrace({commentClient.expand id, ((Map<String, String>) TraceUtil.instance().serializeContext()).get("uber-trace-id")} as Supplier, "Thread: "+ topic)
    }

    @Post('/{topic}')
    HttpStatus addTopic(String topic, @Body Comment comment) {
        TraceUtil.instance().newTrace({ commentClient.add topic, comment.poster, comment.content, ((Map<String, String>) TraceUtil.instance().serializeContext()).get("uber-trace-id")} as Supplier, "Add Topic: "+ topic)
    }

    @Post('/{topic}/{id}')
    HttpStatus addReply(String topic, Long id, @Body Comment comment) {
        TraceUtil.instance().newTrace({ commentClient.addReply id, comment.poster, comment.content, ((Map<String, String>) TraceUtil.instance().serializeContext()).get("uber-trace-id")} as Supplier, "Add Reply: "+ topic)
    }
}
