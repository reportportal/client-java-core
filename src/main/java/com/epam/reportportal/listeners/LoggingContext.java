package com.epam.reportportal.listeners;

import com.epam.reportportal.service.ReportPortalClient;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import reactor.core.publisher.BlockingSink;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Function;

/**
 * Created by avarabyeu on 12/7/16.
 */
class LoggingContext {

    final Mono<Void> loggingSubscription;
    final BlockingSink<Mono<SaveLogRQ>> logsEmitter;
    final Mono<String> itemId;

    LoggingContext(Mono<String> itemId, ReportPortalClient client) {
        this.itemId = itemId;

        final EmitterProcessor<Mono<SaveLogRQ>> processor = EmitterProcessor.create();
        this.logsEmitter = processor.connectSink();
        this.loggingSubscription = processor
                .flatMap(rqMono -> rqMono)
                .parallel(8) //parallelism
                .runOn(Schedulers.parallel())
                .flatMap(rq -> {
                    return client.log(rq)
                            .doOnSuccess(e -> System.out.println("Yuah!" + Thread.currentThread().getName()))
                            .doOnError(Throwable::printStackTrace)
                            /* silently ignore all logging errors */
                            .otherwise((e) -> Mono.empty());
                })
                .sequential().then().subscribe();
    }

    public void emitLog(Function<String, SaveLogRQ> logSupplier) {
        logsEmitter.next(itemId.map(logSupplier));

    }
}
