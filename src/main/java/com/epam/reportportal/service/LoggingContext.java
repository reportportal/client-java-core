package com.epam.reportportal.service;

import com.epam.ta.reportportal.ws.model.BatchSaveOperatingRS;
import com.epam.ta.reportportal.ws.model.Constants;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.github.avarabyeu.restendpoint.http.MultiPartRequest;
import com.google.common.io.ByteSource;
import com.google.common.net.MediaType;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;
import org.reactivestreams.Publisher;

import java.util.List;

/**
 * @author Andrei Varabyeu
 */
public class LoggingContext {

    private static final ThreadLocal<LoggingContext> CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    public static LoggingContext init(Maybe<String> itemId, final ReportPortalClient client) {
        LoggingContext context = new LoggingContext(itemId, client);
        CONTEXT_THREAD_LOCAL.set(context);
        return context;
    }

    public static void emitLog(com.google.common.base.Function<String, SaveLogRQ> logSupplier) {
        final LoggingContext loggingContext = CONTEXT_THREAD_LOCAL.get();
        if (null != loggingContext) {
            loggingContext.emit(logSupplier);
        }
    }

    public static Completable complete() {
        final LoggingContext loggingContext = CONTEXT_THREAD_LOCAL.get();
        if (null != loggingContext) {
            return loggingContext.completed();
        } else {
            return Maybe.empty().ignoreElement();
        }
    }

    private final PublishSubject<Maybe<SaveLogRQ>> emitter;
    private final Maybe<String> itemId;

    LoggingContext(Maybe<String> itemId, final ReportPortalClient client) {
        this.itemId = itemId;
        this.emitter = PublishSubject.create();
        emitter.toFlowable(BackpressureStrategy.BUFFER)
                .flatMap(new Function<Maybe<SaveLogRQ>, Publisher<SaveLogRQ>>() {
                    @Override
                    public Publisher<SaveLogRQ> apply(Maybe<SaveLogRQ> rq) throws Exception {
                        return rq.toFlowable();
                    }
                })
                .buffer(10)
                .flatMap(new Function<List<SaveLogRQ>, Flowable<BatchSaveOperatingRS>>() {
                    @Override
                    public Flowable<BatchSaveOperatingRS> apply(List<SaveLogRQ> rqs) throws Exception {
                        MultiPartRequest.Builder builder = new MultiPartRequest.Builder();

                        builder.addSerializedPart(Constants.LOG_REQUEST_JSON_PART, rqs);

                        for (SaveLogRQ rq : rqs) {
                            if (null != rq.getFile()) {

                                builder.addBinaryPart(Constants.LOG_REQUEST_BINARY_PART, rq.getFile().getName(),
                                        MediaType.OCTET_STREAM.toString(), ByteSource.wrap(rq.getFile().getContent()));
                            }
                        }
                        return client.log(builder.build()).toFlowable();
                    }
                }).subscribe();

    }

    public void emit(final com.google.common.base.Function<String, SaveLogRQ> logSupplier) {
        emitter.onNext(itemId.map(new Function<String, SaveLogRQ>() {
            @Override
            public SaveLogRQ apply(String input) throws Exception {
                return logSupplier.apply(input);
            }
        }));

    }

    public Completable completed() {
        emitter.onComplete();
        return emitter.ignoreElements();
    }

}
