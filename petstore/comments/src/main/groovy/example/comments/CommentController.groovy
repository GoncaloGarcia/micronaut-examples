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
package example.comments

import com.feedzai.commons.tracing.engine.JaegerTracingEngine
import com.feedzai.commons.tracing.engine.TraceUtil
import example.api.v1.CommentOperations
import groovy.transform.CompileStatic
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Header

import javax.validation.constraints.NotBlank
import java.time.Duration
import java.util.function.Supplier

/**
 * @author graemerocher
 * @since 1.0
 */
@Controller('/${comments.api.version}/topics')
@CompileStatic
class CommentController implements CommentOperations<Comment> {

    final CommentRepository commentRepository

    CommentController(CommentRepository commentRepository) {
        this.commentRepository = commentRepository
        TraceUtil.init(new JaegerTracingEngine.Builder().withCacheDuration(Duration.ofDays(1)).withCacheMaxSize(10000).withSampleRate(1).withProcessName("StoreFront").withIp("192.168.0.4").build())
    }

    @Override
    HttpStatus add(
            @NotBlank String topic,
            @NotBlank String poster,
            @NotBlank String content,
            @Header("uber-trace-id") String traceid) {
        Comment c = TraceUtil.instance().newProcess({commentRepository.saveComment(
                topic, poster, content
        )} as Supplier, "Add Comment", TraceUtil.instance().deserializeContext((Serializable) Collections.singletonMap("uber-trace-id", traceid)))
        if(c != null) {
            return HttpStatus.CREATED
        }
        return HttpStatus.NOT_FOUND
    }

    @Override
    HttpStatus addReply(
            @NotBlank Long id,
            @NotBlank String poster,
            @NotBlank String content,
            @Header("uber-trace-id") String traceid) {
        Comment c = TraceUtil.instance().newProcess({commentRepository.saveReply(
                id, poster, content
        )} as Supplier, "Add Reply", TraceUtil.instance().deserializeContext((Serializable) Collections.singletonMap("uber-trace-id", traceid)))
        if(c != null) {
            return HttpStatus.CREATED
        }
        return HttpStatus.NOT_FOUND
    }

    @Override
    List<Comment> list(String topic,  @Header("uber-trace-id") String traceid) {
        return TraceUtil.instance().newProcess({commentRepository.findComments(topic)} as Supplier, "List comments: " + topic, TraceUtil.instance().deserializeContext((Serializable) Collections.singletonMap("uber-trace-id", traceid)))
    }

    @Override
    Map<String, Object> expand(Long id,  @Header("uber-trace-id") String traceid) {
        return TraceUtil.instance().newProcess({commentRepository.findCommentReplies(id)} as Supplier, "Expand Comment: " + id, TraceUtil.instance().deserializeContext((Serializable) Collections.singletonMap("uber-trace-id", traceid)))
    }


}
