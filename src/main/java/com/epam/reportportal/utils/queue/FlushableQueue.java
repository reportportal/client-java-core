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
package com.epam.reportportal.utils.queue;

import java.io.Flushable;
import java.io.IOException;
import java.util.Queue;

import com.google.common.collect.Lists;

/**
 * Flushable Queue. Doesn't use Java's {@link Queue} interface since it's
 * unnacessary<br>
 * Uses flush policy to be notified when query should be flushed.
 * {@link FlushableQueue#flushPolicy} should be overridden by user. <br>
 * Usage example: <br>
 * 
 * <pre>
 * <code>	FlushableQueue<String> q = new FlushableQueue<String>(new SizeBasedFlushPolicy<String>(2)) {
 * 			{@literal @}Override
 * 			protected void performFlush(Queue<String> q) {
 * 				while (!q.isEmpty()) {
 * 					System.out.println(q.poll());
 * 
 * 				}
 * 			}
 * 		};</code>
 * </pre>
 * 
 * @author Andrei Varabyeu
 * 
 * @param <E>
 */
abstract public class FlushableQueue<E> implements Flushable {

	/** Flush policy */
	private FlushPolicy<E> flushPolicy;

	/** {@link Queue} delegate */
	private Queue<E> delegate;

	public FlushableQueue(FlushPolicy<E> flushPolicy) {
		this.delegate = Lists.newLinkedList();
		this.flushPolicy = flushPolicy;
	}

	/**
	 * Push to queue. After that flushes queue if needed
	 * 
	 * @param e
	 * @throws IOException
	 */
	public void push(E e) throws IOException {
		delegate.offer(e);
		if (flushPolicy.shouldBeFlushed(delegate)) {
			flush();
		}
	}

	/**
	 * Pop from queue
	 */
	public E pop() {
		return delegate.poll();
	}

	@Override
	public void flush() throws IOException {
		Queue<E> flushed = delegate;
		delegate = Lists.newLinkedList();
		performFlush(flushed);
	}

	abstract protected void performFlush(Queue<E> flushed) throws IOException;

	public interface FlushPolicy<E> {
		boolean shouldBeFlushed(Queue<E> queue);
	}

}
