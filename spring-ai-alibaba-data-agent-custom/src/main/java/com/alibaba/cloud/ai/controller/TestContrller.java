package com.alibaba.cloud.ai.controller;


import com.alibaba.cloud.ai.dataagent.dto.GraphRequest;
import com.alibaba.cloud.ai.dataagent.service.graph.GraphService;
import com.alibaba.cloud.ai.dataagent.vo.GraphNodeResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TestContrller {

    private final GraphService graphService;

    @PostMapping(value = "/stream/search", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<GraphNodeResponse>> streamSearch(
            @RequestParam("agentId") String agentId,
            @RequestParam(value = "threadId", required = false) String threadId,
            @RequestParam("query") String query,
            @RequestParam(value = "humanFeedback", required = false) boolean humanFeedback,
            @RequestParam(value = "humanFeedbackContent", required = false) String humanFeedbackContent,
            @RequestParam(value = "rejectedPlan", required = false) boolean rejectedPlan,
            @RequestParam(value = "nl2sqlOnly", required = false) boolean nl2sqlOnly,
            @RequestParam(value = "plainReport", required = false) boolean plainReport,
            HttpServletResponse response) {

        // 设置 SSE 相关 HTTP 头
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/event-stream");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Cache-Control");

        Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink = Sinks.many().unicast().onBackpressureBuffer();

        GraphRequest request = GraphRequest.builder()
                .agentId(agentId)
                .threadId(threadId)
                .query(query)
                .humanFeedback(humanFeedback)
                .humanFeedbackContent(humanFeedbackContent)
                .rejectedPlan(rejectedPlan)
                .nl2sqlOnly(nl2sqlOnly)
                .plainReport(plainReport)
                .build();

        graphService.graphStreamProcess(sink, request);

        return sink.asFlux()
                .doOnSubscribe(subscription -> log.info("Client subscribed to stream, threadId: {}", request.getThreadId()))
                .doOnCancel(() -> {
                    log.info("Client disconnected from stream, threadId: {}", request.getThreadId());
                    if (request.getThreadId() != null) {
                        graphService.stopStreamProcessing(request.getThreadId());
                    }
                })
                .doOnError(e -> {
                    log.error("Error occurred during streaming, threadId: {}: ", request.getThreadId(), e);
                    if (request.getThreadId() != null) {
                        graphService.stopStreamProcessing(request.getThreadId());
                    }
                })
                .doOnComplete(() -> log.info("Stream completed successfully, threadId: {}", request.getThreadId()));
    }

}

