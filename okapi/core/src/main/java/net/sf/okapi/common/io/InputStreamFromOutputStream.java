/*
 * Copyright (c) 2008, Davide Simonetti
 * All rights reserved.
 * Redistribution and use in source and binary forms,
 * with or without modification, are permitted provided that the following
 * conditions are met:
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Davide Simonetti nor the names of its contributors may
 *    be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*===========================================================================
 Changes to original Copyright (C) 2014 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 ===========================================================================*/

package net.sf.okapi.common.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiMergeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class allow to read the data written to an OutputStream from an
 * InputStream.
 * </p>
 * <p>
 * To use this class you must subclass it and implement the abstract method
 * {@linkplain #produce(OutputStream)}. The data who is produced inside this
 * function can be written to the sink <code>OutputStream</code> passed as a
 * parameter. Later it can be read back from from the
 * <code>InputStreamFromOutputStream</code> class (whose ancestor is
 * <code>java.io.InputStream</code> ).
 * </p>
 * 
 * <pre>
 * final String dataId=//id of some data.
 * final InputStreamFromOutputStream&lt;String&gt; isos
 *                          = new InputStreamFromOutputStream&lt;String&gt;() {
 *   &#064;Override
 *   public String produce(final OutputStream dataSink) throws Exception {
 *      //call your application function who produces the data here
 *      //WARNING: we're in another thread here, so this method shouldn't
 *      //write any class field or make assumptions on the state of the class.
 *      return produceMydata(dataId,dataSink)
 *   }
 * };
 *  try {
 *    //now you can read from the InputStream the data that was written to the
 *    //dataSink OutputStream
 *     byte[] read=IOUtils.toByteArray(isos);
 *     //Use data here
 *   } catch (final IOException e) {
 *     //Handle exception here
 *   } finally {
 *   isos.close();
 * }
 * //You can get the result of produceMyData after the stream has been closed.
 * String resultOfProduction = isos.getResult();
 * </pre>
 * <p>
 * This class encapsulates a pipe and a <code>Thread</code>, hiding the
 * complexity of using them. It is possible to select different strategies for
 * allocating the internal thread or even specify the
 * {@linkplain ExecutorService} for thread execution.
 * </p>
 * 
 * @param <T>
 *            Optional result returned by the function
 *            {@linkplain #produce(OutputStream)} after the data has been
 *            written. It can be obtained calling the {@linkplain #getResult()}
 * @author dvd.smnt
 * @since 1.0
 */
public abstract class InputStreamFromOutputStream<T> extends PipedInputStream {
	private static final int DEFAULT_PIPE_SIZE = 4096;

	/**
	 * This inner class run in another thread and calls the
	 * {@link #produce(OutputStream)} method.
	 * 
	 * @author dvd.smnt
	 */
	private final class DataProducer implements Callable<T> {

		@Override
		public T call() throws Exception {
			final String threadName = Thread.currentThread().getName();
			T result;
			InputStreamFromOutputStream.ACTIVE_THREAD_NAMES.add(threadName);
			InputStreamFromOutputStream.LOG.debug("thread [" + threadName
					+ "] started.");
			try {
				result = produce(InputStreamFromOutputStream.this.pipedOs);
			} finally {
				closeStream();
				InputStreamFromOutputStream.ACTIVE_THREAD_NAMES.remove(threadName);
				InputStreamFromOutputStream.LOG.debug("thread [" + threadName + "] closed.");
			}
			return result;
		}

		private void closeStream() {
			try {
				InputStreamFromOutputStream.this.pipedOs.close();
			} catch (final IOException e) {
				if ((e.getMessage() != null)
						&& (e.getMessage().indexOf("closed") > 0)) {
					InputStreamFromOutputStream.LOG
							.debug("Stream already closed");
				} else {
					InputStreamFromOutputStream.LOG.error(
							"IOException closing OutputStream"
									+ " Thread might be locked", e);
				}
			} catch (final Throwable t) {
				InputStreamFromOutputStream.LOG
						.error("Error closing InputStream"
								+ " Thread might be locked", t);
			}
		}

	}

	/**
	 * Collection for debugging purpose containing names of threads (name is
	 * calculated from the instantiation line. See <code>getCaller()</code>.
	 */
	private static final List<String> ACTIVE_THREAD_NAMES = Collections
			.synchronizedList(new ArrayList<String>());

	/**
	 * The default pipe buffer size for the newly created pipes.
	 */
	private static int defaultPipeSize = DEFAULT_PIPE_SIZE;
	private static final Logger LOG = LoggerFactory
			.getLogger(InputStreamFromOutputStream.DataProducer.class);

	/**
	 * This method can be used for debugging purposes to get a list of the
	 * currently active threads.
	 * 
	 * @return Array containing names of the threads currently active.
	 */
	public static final String[] getActiveThreadNames() {
		final String[] result;
		synchronized (InputStreamFromOutputStream.ACTIVE_THREAD_NAMES) {
			result = InputStreamFromOutputStream.ACTIVE_THREAD_NAMES
					.toArray(new String[0]);
		}
		return result;
	}

	/**
	 * Set the size for the pipe circular buffer for the newly created
	 * <code>InputStreamFromOutputStream</code>. Default is 4096 bytes.
	 * 
	 * @since 1.2.2
	 * @param defaultPipeSize
	 *            the default pipe buffer size in bytes.
	 */
	public static void setDefaultPipeSize(final int defaultPipeSize) {
		InputStreamFromOutputStream.defaultPipeSize = defaultPipeSize;
	}

	private boolean closeCalled = false;
	private final ExecutorService executorService;
	private Future<T> futureResult;
	private boolean started = false;

	private final PipedOutputStream pipedOs = new PipedOutputStream() {
		private boolean outputStreamCloseCalled = false;

		// duplicate close hangs the pipeStream see issue #36
		@Override
		public void close() throws IOException {
			synchronized (this) {
				if (outputStreamCloseCalled) {
					return;
				}
				outputStreamCloseCalled = true;
			}
			super.close();
		}

	};

	/**
	 * Creates a <code>InputStreamFromOutputStream</code> with a
	 * THREAD_PER_INSTANCE thread strategy.
	 */
	public InputStreamFromOutputStream() {
		this(false);
	}

	private InputStreamFromOutputStream(final boolean startImmediately) {
		super(defaultPipeSize);
		this.executorService = Executors.newSingleThreadExecutor();
		try {
			connect(this.pipedOs);
		} catch (final IOException e) {
			throw new OkapiException("Error during pipe creaton", e);
		}
		if (startImmediately) {
			checkInitialized();
		}
	}

	/**
	 * <p>
	 * This method is called just before the {@link #close()} method completes,
	 * and after the eventual join with the internal thread.
	 * </p>
	 * <p>
	 * It is an extension point designed for applications that need to perform
	 * some operation when the <code>InputStream</code> is closed.
	 * </p>
	 * 
	 * @since 1.2.9
	 * @throws IOException
	 *             threw when the subclass wants to communicate an exception
	 *             during close.
	 */
	protected void afterClose() throws IOException {
		// extension point;
	}

	private void checkException() throws IOException {
		try {
			this.futureResult.get(1, TimeUnit.SECONDS);
		} catch (final ExecutionException e) {
			final Throwable t = e.getCause();
			// attempt to recover original exception
			if (t instanceof OkapiMergeException)
				 throw (OkapiMergeException)t;
			if (t instanceof OkapiException)
				 throw (OkapiException)t;
			final OkapiException e1 = new OkapiException("Exception in InputStreamFromOutputStream thread. "
					+ "Check exception stack for original cuase");
			e1.initCause(t);
			throw e1;
		} catch (final InterruptedException e) {
			final IOException e1 = new IOException("InputStreamFromOutputStream Thread interrupted");
			e1.initCause(e);
			throw e1;
		} catch (final TimeoutException e) {
			LOG.error("This timeout should never happen, "
					+ "the thread should terminate correctly. "
					+ "Please contact io-tools support.", e);
		}
	}

	private synchronized void checkInitialized() {
		if (!this.started) {
			this.started = true;
			final Callable<T> executingCallable = new DataProducer();
			this.futureResult = this.executorService.submit(executingCallable);
		}
	}

	/** {@inheritDoc} */
	@Override
	public final void close() throws IOException {
		checkInitialized();
		if (!this.closeCalled) {
			this.closeCalled = true;
			super.close();
			executorService.shutdownNow();
			afterClose();
		}
	}

	/**
	 * <p>
	 * Returns the object that was previously returned by the
	 * {@linkplain #produce(OutputStream)} method. It performs all the
	 * synchronization operations needed to read the result and waits for the
	 * internal thread to terminate.
	 * </p>
	 * <p>
	 * This method must be called after the method {@linkplain #close()},
	 * otherwise an IllegalStateException is thrown.
	 * </p>
	 * 
	 * @since 1.2.0
	 * @return Object that was returned by the
	 *         {@linkplain #produce(OutputStream)} method.
	 * @throws java.lang.Exception
	 *             If the {@linkplain #produce(OutputStream)} method threw an
	 *             java.lang.Exception this method will throw again the same
	 *             exception.
	 * @throws java.lang.IllegalStateException
	 *             If the {@linkplain #close()} method hasn't been called yet.
	 */
	public T getResult() throws Exception {
		if (!this.closeCalled) {
			throw new IllegalStateException(
					"getResult() called before close()."
							+ "This method can be called only "
							+ "after the stream has been closed.");
		}
		T result;
		try {
			result = this.futureResult.get();
		} catch (final ExecutionException e) {
			final Throwable cause = e.getCause();
			if (cause instanceof Exception) {
				throw (Exception) cause;
			}
			throw e;
		}
		return result;
	}

	/**
	 * <p>
	 * This method must be implemented by the user of this class to produce the
	 * data that must be read from the external <code>InputStream</code>.
	 * </p>
	 * <p>
	 * Special care must be paid passing arguments to this method or setting
	 * global fields because it is executed in another thread.
	 * </p>
	 * <p>
	 * The right way to set a field variable is to return a value in the
	 * <code>produce</code>and retrieve it in the getResult().
	 * </p>
	 * 
	 * @return The implementing class can use this to return a result of data
	 *         production. The result will be available through the method
	 *         {@linkplain #getResult()}.
	 * @param sink
	 *            the implementing class should write its data to this stream.
	 * @throws java.lang.Exception
	 *             the exception eventually thrown by the implementing class is
	 *             returned by the {@linkplain #read()} methods.
	 * @see #getResult()
	 */
	protected abstract T produce(final OutputStream sink) throws Exception;

	/** {@inheritDoc} */
	@Override
	public final int read() throws IOException {
		checkInitialized();
		final int result = super.read();
		if (result < 0) {
			checkException();
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public final int read(final byte[] b, final int off, final int len)
			throws IOException {
		checkInitialized();
		final int result = super.read(b, off, len);
		if (result < 0) {
			checkException();
		}
		return result;
	}
}
