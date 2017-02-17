package com.epam.reportportal.service;

import com.epam.ta.reportportal.ws.model.BatchSaveOperatingRS;
import com.epam.ta.reportportal.ws.model.Constants;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.github.avarabyeu.restendpoint.http.MultiPartRequest;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.PublishSubject;
import org.apache.http.entity.ContentType;

import java.util.List;

/**
 * @author Andrei Varabyeu
 */
public class LoggingContext {

    final PublishSubject<SaveLogRQ> emitter;

    public LoggingContext(final ReportPortalClient client) {

        emitter = PublishSubject.create();
        emitter.toFlowable(BackpressureStrategy.BUFFER)
                .buffer(10)
                .flatMap(new Function<List<SaveLogRQ>, Flowable<BatchSaveOperatingRS>>() {
                    @Override
                    public Flowable<BatchSaveOperatingRS> apply(List<SaveLogRQ> rqs) throws Exception {
                        MultiPartRequest.Builder builder = new MultiPartRequest.Builder();

                        builder.addSerializedPart(Constants.LOG_REQUEST_JSON_PART, rqs);

                        for (SaveLogRQ rq : rqs) {
                            if (null != rq.getFile()) {
                                builder.addBinaryPart(Constants.LOG_REQUEST_BINARY_PART, rq.getFile().getName(),
                                        ContentType.APPLICATION_OCTET_STREAM.getMimeType(), rq.getFile().getContent());
                            }
                        }
                        return client.log(builder.build()).toFlowable();
                    }
                }).subscribe();

    }

    public void log(SaveLogRQ rq) {
        emitter.onNext(rq);
    }

    public Completable completed() {
        emitter.onComplete();
        return emitter.ignoreElements();
    }

}
