package com.epam.reportportal.service;

import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.github.avarabyeu.restendpoint.http.MultiPartRequest;
import com.github.avarabyeu.restendpoint.http.annotation.Body;
import com.github.avarabyeu.restendpoint.http.annotation.Multipart;
import com.github.avarabyeu.restendpoint.http.annotation.Path;
import com.github.avarabyeu.restendpoint.http.annotation.Request;
import reactor.core.publisher.Mono;

import static com.github.avarabyeu.restendpoint.http.HttpMethod.POST;
import static com.github.avarabyeu.restendpoint.http.HttpMethod.PUT;

/**
 * @author Andrei Varabyeu
 */

public interface ReportPortalClient {

    @Request(method = POST, url = "/launch")
    Mono<EntryCreatedRS> startLaunch(@Body StartLaunchRQ rq);

    @Request(method = PUT, url = "/launch/{launchId}/finish")
    Mono<OperationCompletionRS> finishLaunch(@Path("launchId") String launch, @Body FinishExecutionRQ rq);

    @Request(method = POST, url = "/item/")
    Mono<EntryCreatedRS> startTestItem(@Body StartTestItemRQ rq);

    @Request(method = POST, url = "/item/{parent}")
    Mono<EntryCreatedRS> startTestItem(@Path("parent") String parent, @Body StartTestItemRQ rq);

    @Request(method = PUT, url = "/item/{itemId}")
    Mono<OperationCompletionRS> finishTestItem(@Path("itemId") String itemId, @Body FinishTestItemRQ rq);

    @Request(method = POST, url = "/log/")
    Mono<EntryCreatedRS> log(@Body SaveLogRQ rq);

    @Request(method = POST, url = "/log/")
    Mono<EntryCreatedRS> log(@Body @Multipart MultiPartRequest rq);

}
