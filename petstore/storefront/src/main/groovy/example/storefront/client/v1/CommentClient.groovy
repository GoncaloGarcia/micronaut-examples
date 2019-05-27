package example.storefront.client.v1

import example.api.v1.CommentOperations
import example.api.v1.HealthStatusOperation
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

/**
 * @author zacharyklein
 * @since 1.0
 */
@Client(id = "comments")
interface CommentClient extends CommentOperations<Comment>, HealthStatusOperation {

    @Override
    @Get("/v1/topics/{topic}/comments") //TODO: Hard-coding the topics/ paths (instead of using @Client(path)) in order to allow /health endpoint to be accessed
    List<Comment> list(String topic, @Header("uber-trace-id") String traceid)

    @Override
    @Get("/v1/topics/comment/{id}")
    Map<String, Object> expand(Long id, @Header("uber-trace-id") String traceid)

    @Override
    @Post("/v1/topics/{topic}/comments")
    HttpStatus add(String topic, String poster, String content, @Header("uber-trace-id") String traceid)

    @Override
    @Post("/v1/topics/comment/{id}/reply")
    HttpStatus addReply(Long id, String poster, String content, @Header("uber-trace-id") String traceid)
}
