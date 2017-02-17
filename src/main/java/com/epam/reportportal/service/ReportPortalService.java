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
package com.epam.reportportal.service;

import com.epam.ta.reportportal.ws.model.Constants;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.github.avarabyeu.restendpoint.http.MultiPartRequest;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.net.MediaType;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.functions.Function;

import java.util.LinkedList;
import java.util.List;

/**
 * Default ReportPortal Reporter implementation. Uses
 * {@link com.github.avarabyeu.restendpoint.http.RestEndpoint} as REST WS Client
 *
 * @author Andrei Varabyeu
 */
public class ReportPortalService {

    private static final Function<EntryCreatedRS, String> TO_ID = new Function<EntryCreatedRS, String>() {
        @Override
        public String apply(EntryCreatedRS rs) throws Exception {
            return rs.getId();
        }
    };

    /**
     * REST Client
     */
    private ReportPortalClient reportPortalClient;
    private LoadingCache<Maybe<String>, List<Completable>> QUEUE = CacheBuilder.newBuilder().build(
            new CacheLoader<Maybe<String>, List<Completable>>() {
                @Override
                public List<Completable> load(Maybe<String> key) throws Exception {
                    return new LinkedList<>();
                }
            });

    private Maybe<String> launch;

    private ReportPortalService(ReportPortalClient reportPortalClient) {
        this.reportPortalClient = Preconditions.checkNotNull(reportPortalClient, "RestEndpoing shouldn't be NULL");
    }

    public static ReportPortalService startLaunch(ReportPortalClient client, StartLaunchRQ rq) {
        ReportPortalService service = new ReportPortalService(client);
        service.startLaunch(rq);
        return service;
    }

    public Maybe<String> startLaunch(StartLaunchRQ rq) {
        this.launch = reportPortalClient.startLaunch(rq).map(TO_ID);
        return launch;
    }

    public Maybe<OperationCompletionRS> finishLaunch(final FinishExecutionRQ rq) {
        Completable.concat(QUEUE.getUnchecked(this.launch)).blockingAwait();
        return this.launch.flatMap(new Function<String, MaybeSource<OperationCompletionRS>>() {
            @Override
            public Maybe<OperationCompletionRS> apply(String id) throws Exception {
                return reportPortalClient.finishLaunch(id, rq);
            }
        });
    }

    public Maybe<? extends EntryCreatedRS> log(SaveLogRQ rq) {
        if (null == rq.getFile()) {
            return reportPortalClient.log(rq);
        } else {
            MultiPartRequest request = new MultiPartRequest.Builder()
                    .addSerializedPart(Constants.LOG_REQUEST_JSON_PART, new SaveLogRQ[] { rq })
                    .addBinaryPart(rq.getFile().getName(),
                            rq.getFile().getName(), MediaType.OCTET_STREAM.toString(),
                            rq.getFile().getContent())
                    .build();

            return reportPortalClient.log(request);

        }
    }

    public Maybe<String> startTestItem(StartTestItemRQ rq) {
        final Maybe<String> testItem = this.reportPortalClient.startTestItem(rq).map(TO_ID);
        QUEUE.getUnchecked(launch).add(testItem.ignoreElement());
        return testItem;
    }

    public Maybe<String> startTestItem(Maybe<String> parentId, final StartTestItemRQ rq) {
        final Maybe<String> itemId = parentId.flatMap(new Function<String, MaybeSource<String>>() {
            @Override
            public MaybeSource<String> apply(String parentId) throws Exception {
                return reportPortalClient.startTestItem(parentId, rq).map(TO_ID);
            }
        });
        QUEUE.getUnchecked(parentId).add(itemId.ignoreElement());
        return itemId;
    }

    public Maybe<OperationCompletionRS> finishTestItem(Maybe<String> itemId, final FinishTestItemRQ rq) {
        //wait for the children
        Completable.concat(QUEUE.getUnchecked(itemId)).blockingAwait();
        return itemId.flatMap(new Function<String, MaybeSource<OperationCompletionRS>>() {
            @Override
            public MaybeSource<OperationCompletionRS> apply(String itemId) throws Exception {
                return reportPortalClient.finishTestItem(itemId, rq);
            }
        });
    }

}
