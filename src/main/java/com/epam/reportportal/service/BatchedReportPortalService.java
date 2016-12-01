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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.epam.reportportal.utils.queue.BatchExecutor;
import com.epam.reportportal.utils.queue.Result;
import com.epam.reportportal.apache.http.entity.ContentType;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.epam.reportportal.restclient.endpoint.MultiPartRequest;
import com.epam.reportportal.restclient.endpoint.RestEndpoint;
import com.epam.reportportal.restclient.endpoint.exception.RestEndpointIOException;

/**
 * Implementation with logging via batches. Keeps some logs until defined count
 * and sends them to reportportal in the batch request
 * 
 * @author Andrei Varabyeu
 * 
 */
public class BatchedReportPortalService extends ReportPortalService {

	private ThreadLocal<BatchExecutor<SaveLogRQ, BatchElementCreatedRS>> saveLogQueue;

	public BatchedReportPortalService(final RestEndpoint endpoint, final String apiBase, final String project, final int logsBatchSize) {

		super(endpoint, apiBase, project);

		saveLogQueue = new ThreadLocal<BatchExecutor<SaveLogRQ, BatchElementCreatedRS>>() {
			@Override
			protected BatchExecutor<SaveLogRQ, BatchElementCreatedRS> initialValue() {
				return new BatchExecutor<SaveLogRQ, BatchElementCreatedRS>() {

					@Override
					protected void executeBatch(Queue<BatchExecutor.Parameter<SaveLogRQ, BatchElementCreatedRS>> batch) throws IOException {

						MultiPartRequest.Builder<List<SaveLogRQ>> requestBuilder = new MultiPartRequest.Builder<List<SaveLogRQ>>();
						List<SaveLogRQ> serializedPart = new ArrayList<SaveLogRQ>(batch.size());

						for (BatchExecutor.Parameter<SaveLogRQ, BatchElementCreatedRS> batchItem : batch) {
							SaveLogRQ rq = batchItem.getParameter();
							serializedPart.add(batchItem.getParameter());

							if (null != rq.getFile()) {
								requestBuilder.addBinaryPart(Constants.LOG_REQUEST_BINARY_PART, rq.getFile().getName(),
										ContentType.APPLICATION_OCTET_STREAM.getMimeType(), rq.getFile().getContent());
							}
						}

						requestBuilder.addSerializedPart(Constants.LOG_REQUEST_JSON_PART, serializedPart);

						BatchSaveOperatingRS batchResponse = endpoint.post(apiBase + "/" + project + "/log", requestBuilder.build(),
								BatchSaveOperatingRS.class);
						for (BatchElementCreatedRS rsItem : batchResponse.getResponses()) {
							batch.poll().getResult().set(rsItem);
						}

					}

					@Override
					protected boolean execute(Queue<BatchExecutor.Parameter<SaveLogRQ, BatchElementCreatedRS>> batch) throws IOException {
						return batch.size() >= logsBatchSize;
					}

				};
			}
		};

	}

	@Override
	public Result<BatchElementCreatedRS> log(SaveLogRQ rq) throws RestEndpointIOException {
		try {
			return saveLogQueue.get().submit(rq);
		} catch (RestEndpointIOException e) {
			throw e;
		} catch (IOException e) {
			throw new RestEndpointIOException("Error occurred on logs saving", e);
		}
	}

	@Override
	public OperationCompletionRS finishTestItem(String itemId, FinishTestItemRQ rq) throws RestEndpointIOException {
		OperationCompletionRS operationCompletionRS;
		try {
			saveLogQueue.get().executeAndClear();
		} catch (RestEndpointIOException e) {
			throw e;
		} catch (IOException e) {
			throw new RestEndpointIOException("Error occurred on logs saving", e);
		} finally {
			operationCompletionRS = super.finishTestItem(itemId, rq);
		}
		return operationCompletionRS;
	}

}
