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
package com.epam.reportportal.utils.queue;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.Future;

import com.google.common.collect.Lists;

/**
 * Abstract Batch executor. Waits onle {@link #execute(Queue)} is allowed and
 * {@link #executeBatch(Queue)}. Returns {@link Future} since we cannot
 * guarantee that operation will be execute in submit time
 * 
 * @author Andrei Varabyeu
 * 
 * @param
 * 			<P>
 * @param <R>
 */
public abstract class BatchExecutor<P, R> {

	/** {@link Queue} delegate */
	private Queue<Parameter<P, R>> batch;

	public BatchExecutor() {
		this.batch = Lists.newLinkedList();
	}

	/**
	 * Push to queue. After that flushes queue if needed
	 * 
	 * @param parameter
	 * @throws IOException
	 */
	public Result<R> submit(final P parameter) throws IOException {
		final Result<R> result = new Result<R>();
		batch.offer(new Parameter<P, R>(parameter, result));
		if (execute(batch)) {
			executeAndClear();
		}
		return result;

	}

	/**
	 * Executes all hold operations and clears batch queue
	 * 
	 * @throws IOException
	 */
	public void executeAndClear() throws IOException {
		Queue<Parameter<P, R>> flushed = batch;
		batch = Lists.newLinkedList();
		if (!flushed.isEmpty())
			executeBatch(flushed);
	}

	/**
	 * Executes batch operation for queued parameters. <b>Be aware that the
	 * result of execution of each parameter is placed inside {@link Parameter}
	 * object and needs to be populated in this method</b>
	 * 
	 * @param batch
	 * @throws IOException
	 */
	abstract protected void executeBatch(Queue<Parameter<P, R>> batch) throws IOException;

	/**
	 * Whether parameters should be executed in batch or queued. This method are
	 * executed after each {@link #submit(Object)} operation
	 * 
	 * @param batch
	 * @throws IOException
	 */
	abstract protected boolean execute(Queue<Parameter<P, R>> batch) throws IOException;

	/**
	 * Wraps provided parameter and execution result for this parameter
	 * 
	 * @author Andrei Varabyeu
	 * 
	 * @param
	 * 			<P>
	 *            - type of parameter
	 * @param <R>
	 *            - type of result
	 */
	protected static final class Parameter<P, R> {
		private final Result<R> result;
		private final P parameter;

		public Parameter(final P parameter, final Result<R> result) {
			this.parameter = parameter;
			this.result = result;
		}

		public P getParameter() {
			return parameter;
		}

		public Result<R> getResult() {
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Parameter [result=" + result + ", parameter=" + parameter + "]";
		}

	}

}
