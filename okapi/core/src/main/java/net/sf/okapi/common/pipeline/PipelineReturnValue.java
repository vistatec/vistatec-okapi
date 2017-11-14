/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.common.pipeline;

/**
 * Values returned by implementations of the {@link IPipeline} interface.
 */
public enum PipelineReturnValue {
	/**
	 * {@link IPipeline} completed successfully.
	 */
	SUCCEDED,

	/**
	 * One of the {@link IPipeline} steps has failed.
	 */
	FAILED,

	/**
	 * {@link IPipeline} was interrupted.
	 */
	INTERRUPTED,

	/**
	 * {@link IPipeline} is currently running.
	 */
	RUNNING,

	/**
	 * {@link IPipeline} has been paused.
	 */
	PAUSED,

	/**
	 * {@link IPipeline} was canceled.
	 */
	CANCELLED,

	/**
	 * {@link IPipeline} has been stopped and all steps have finalized. The
	 * {@link IPipeline} must be reinitialized after being destroyed.
	 */
	DESTROYED
}
