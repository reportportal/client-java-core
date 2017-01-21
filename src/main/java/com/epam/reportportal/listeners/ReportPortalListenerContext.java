/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/client-java-core
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.reportportal.listeners;

import com.epam.reportportal.service.ReportPortalClient;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Function;

/**
 * This context contains currently running item id
 */
public class ReportPortalListenerContext {

    private static ThreadLocal<LoggingContext> LOGGING_CONTEXT = new ThreadLocal<>();

    private ReportPortalListenerContext() {
    }

    public static Mono<Void> getLoggingSubscription() {
        return LOGGING_CONTEXT.get().loggingSubscription;
    }

    public static void emitLog(Function<String, SaveLogRQ> logSupplier) {
        Optional.ofNullable(LOGGING_CONTEXT.get()).ifPresent(lc -> lc.emitLog(logSupplier));
    }

    public static void stopLogging() {
        Optional.ofNullable(LOGGING_CONTEXT.get()).ifPresent(c -> c.logsEmitter.complete());
    }

    public static void init(Mono<String> itemId, ReportPortalClient client) {
        LOGGING_CONTEXT.set(new LoggingContext(itemId, client));
    }

    public static void cleanUp() {
        LOGGING_CONTEXT.set(null);
    }

}
