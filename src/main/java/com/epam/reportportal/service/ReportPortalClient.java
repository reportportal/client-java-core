package com.epam.reportportal.service;

import com.epam.ta.reportportal.ws.model.BatchElementCreatedRS;
import com.epam.ta.reportportal.ws.model.BatchSaveOperatingRS;
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
import io.reactivex.Maybe;

import static com.github.avarabyeu.restendpoint.http.HttpMethod.POST;
import static com.github.avarabyeu.restendpoint.http.HttpMethod.PUT;

/**
 * @author Andrei Varabyeu
 */
public interface ReportPortalClient {

    @Request(method = POST, url = "/launch")
    Maybe<EntryCreatedRS> startLaunch(@Body StartLaunchRQ rq);

    @Request(method = PUT, url = "/launch/{launchId}/finish")
    Maybe<OperationCompletionRS> finishLaunch(@Path("launchId") String launch, @Body FinishExecutionRQ rq);

    @Request(method = POST, url = "/item/")
    Maybe<EntryCreatedRS> startTestItem(@Body StartTestItemRQ rq);

    @Request(method = POST, url = "/item/{parent}")
    Maybe<EntryCreatedRS> startTestItem(@Path("parent") String parent, @Body StartTestItemRQ rq);

    @Request(method = PUT, url = "/item/{itemId}")
    Maybe<OperationCompletionRS> finishTestItem(@Path("itemId") String itemId, @Body FinishTestItemRQ rq);

    @Request(method = POST, url = "/log/")
    Maybe<EntryCreatedRS> log(@Body SaveLogRQ rq);

    @Request(method = POST, url = "/log/")
    Maybe<BatchSaveOperatingRS> log(@Body @Multipart MultiPartRequest rq);
}
