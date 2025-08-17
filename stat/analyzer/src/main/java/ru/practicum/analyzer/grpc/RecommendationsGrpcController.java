// src/main/java/ru/practicum/analyzer/grpc/RecommendationsGrpcController.java
package ru.practicum.analyzer.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.analyzer.service.RecommendationService;
import ru.practicum.grpc.stats.analyzer.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendation.UserPredictionsRequestProto;

import java.util.List;
import java.util.Map;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationsGrpcController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationService service;

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        long eventId = request.getEventId();
        long userId = request.getUserId();
        int max = request.getMaxResults();

        List<Map.Entry<Long, Double>> res = service.similarEventsForUser(eventId, userId, max);
        for (var e : res) {
            responseObserver.onNext(RecommendedEventProto.newBuilder()
                    .setEventId(e.getKey())
                    .setScore(e.getValue())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        List<Map.Entry<Long, Double>> res =
                service.recommendationsForUser(request.getUserId(), request.getMaxResults());
        for (var e : res) {
            responseObserver.onNext(RecommendedEventProto.newBuilder()
                    .setEventId(e.getKey())
                    .setScore(e.getValue())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        var ids = request.getEventIdList();
        Map<Long, Double> sums = service.interactionsCount(ids.stream().map(Long::valueOf).toList());
        for (Long id : ids) {
            responseObserver.onNext(RecommendedEventProto.newBuilder()
                    .setEventId(id)
                    .setScore(sums.getOrDefault(id, 0.0))
                    .build());
        }
        responseObserver.onCompleted();
    }
}