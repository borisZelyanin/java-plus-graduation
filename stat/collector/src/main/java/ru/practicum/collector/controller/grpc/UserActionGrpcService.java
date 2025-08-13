package ru.practicum.collector.controller.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.collector.service.UserActionService;
import ru.practicum.grpc.stats.action.UserActionProto;
import ru.practicum.grpc.stats.collector.UserActionControllerGrpc;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserActionGrpcService extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final UserActionService userActionService;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            // Валидация простых полей
            if (request.getUserId() <= 0 || request.getEventId() <= 0) {
                responseObserver.onError(
                        Status.INVALID_ARGUMENT
                                .withDescription("user_id и event_id должны быть > 0")
                                .asRuntimeException()
                );
                return;
            }

            // Передача в сервис
            userActionService.processUserAction(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("💥 Ошибка обработки gRPC CollectUserAction", e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("internal error")
                            .augmentDescription(e.getMessage())
                            .asRuntimeException()
            );
        }
    }
}