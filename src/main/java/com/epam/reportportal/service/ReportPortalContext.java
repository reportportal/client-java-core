package com.epam.reportportal.service;

import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import io.reactivex.Completable;
import io.reactivex.Maybe;

/**
 * Created by avarabyeu on 2/17/17.
 */
public class ReportPortalContext {

    private static final ThreadLocal<LoggingContext> LOGGING_CONTEXT = new ThreadLocal<>();

    public static LoggingContext initLoggingContext(Maybe<String> itemId, final ReportPortalClient client) {
        LoggingContext context = new LoggingContext(itemId, client);
        LOGGING_CONTEXT.set(context);
        return context;
    }

    public static void emitLog(com.google.common.base.Function<String, SaveLogRQ> logSupplier) {
        final LoggingContext loggingContext = LOGGING_CONTEXT.get();
        if (null != loggingContext) {
            loggingContext.emit(logSupplier);
        }
    }

    public static Completable completeLogging() {
        final LoggingContext loggingContext = LOGGING_CONTEXT.get();
        if (null != loggingContext) {
            return loggingContext.completed();
        } else {
            return Maybe.empty().ignoreElement();
        }
    }

}
