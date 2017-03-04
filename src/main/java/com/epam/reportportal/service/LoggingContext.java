package com.epam.reportportal.service;

import com.epam.ta.reportportal.ws.model.BatchSaveOperatingRS;
import com.epam.ta.reportportal.ws.model.Constants;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.github.avarabyeu.restendpoint.http.MultiPartRequest;
import com.google.common.base.Strings;
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
 * Logging context holds thread-local context for logging and converts
 * {@link SaveLogRQ} to multipart HTTP request to ReportPortal
 * Basic flow:
 * After start some test item (suite/test/step) context should be initialized with observable of
 * item ID and ReportPortal client.
 * Before actual finish of test item, context should be closed/completed.
 * Context consists of {@link Flowable} with buffering back-pressure strategy to be able
 * to batch incoming log messages into one request
 *
 * @author Andrei Varabyeu
 * @see #init(Maybe, ReportPortalClient)
 */
public class LoggingContext {

    private static final ThreadLocal<LoggingContext> CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * Initializes new logging context and attaches it to current thread
     *
     * @param itemId Test Item ID
     * @param client Client of ReportPortal
     * @return New Logging Context
     */
    public static LoggingContext init(Maybe<String> itemId, final ReportPortalClient client) {
        LoggingContext context = new LoggingContext(itemId, client);
        CONTEXT_THREAD_LOCAL.set(context);
        return context;
    }

    /**
     * Emits log message if there is any active context attached to the current thread
     *
     * @param logSupplier Log supplier
     */
    public static void emitLog(com.google.common.base.Function<String, SaveLogRQ> logSupplier) {
        final LoggingContext loggingContext = CONTEXT_THREAD_LOCAL.get();
        if (null != loggingContext) {
            loggingContext.emit(logSupplier);
        }
    }

    /**
     * Completes context attached to the current thread
     *
     * @return Waiting queue to be able to track request sending completion
     */
    public static Completable complete() {
        final LoggingContext loggingContext = CONTEXT_THREAD_LOCAL.get();
        if (null != loggingContext) {
            return loggingContext.completed();
        } else {
            return Maybe.empty().ignoreElement();
        }
    }

    /* Log emitter */
    private final PublishSubject<Maybe<SaveLogRQ>> emitter;
    /* ID of TestItem in ReportPortal */
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
                //TODO make configurable
                .buffer(10)
                .flatMap(new Function<List<SaveLogRQ>, Flowable<BatchSaveOperatingRS>>() {
                    @Override
                    public Flowable<BatchSaveOperatingRS> apply(List<SaveLogRQ> rqs) throws Exception {
                        MultiPartRequest.Builder builder = new MultiPartRequest.Builder();

                        builder.addSerializedPart(Constants.LOG_REQUEST_JSON_PART, rqs);

                        for (SaveLogRQ rq : rqs) {
                            final SaveLogRQ.File file = rq.getFile();
                            if (null != file) {
                                builder.addBinaryPart(Constants.LOG_REQUEST_BINARY_PART, file.getName(),
                                        Strings.isNullOrEmpty(file.getContentType()) ?
                                                MediaType.OCTET_STREAM.toString() :
                                                file.getContentType(), ByteSource.wrap(file.getContent()));
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
