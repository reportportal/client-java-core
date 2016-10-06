/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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
package com.epam;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

import com.epam.reportportal.utils.queue.BatchExecutor;
import com.epam.reportportal.utils.queue.Result;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for batch executor
 * 
 * @author Andrei Varabyeu
 * 
 */
public class BatchExecutorTest {

	@Test
	public void testBatchExecutor() throws IOException, InterruptedException, ExecutionException {
		BatchExecutor<String, String> batchExecutor = new BatchExecutor<String, String>() {

			@Override
			protected void executeBatch(
					Queue<BatchExecutor.Parameter<String, String>> batch)
					throws IOException {
				for (Parameter<String, String> batchItem : batch) {
					batchItem.getResult().set("Item " + batchItem.getParameter() + " result");
				}

			}

			@Override
			protected boolean execute(Queue<BatchExecutor.Parameter<String, String>> batch)
					throws IOException {
				/*
				 * Executes once size exceeds 3
				 */
				return batch.size() >= 3;
			}

		};

		Result<String> firstResult = batchExecutor.submit("1");
		batchExecutor.submit("2");


		/*
		 * Check that execution is not started yet
		 */
		Assert.assertNull(firstResult.get());
		Assert.assertFalse(firstResult.isPresent());

		/*
		 * After this execution should start
		 */
		batchExecutor.submit("3");

		Assert.assertTrue(firstResult.isPresent());
		Assert.assertEquals("There are no result of operation", firstResult.get(), "Item 1 result");
	}
}
