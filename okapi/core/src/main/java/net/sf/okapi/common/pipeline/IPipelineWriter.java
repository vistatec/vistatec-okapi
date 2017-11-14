/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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
 * Facade to save a pipeline into a storage.
 */
public interface IPipelineWriter {

	/**
	 * Writes the steps of the pipeline and the parameters of each step of
	 * a given pipeline into the implementation-specific storage.
	 * @param pipeline the pipeline to write.
	 */
	public void write (IPipeline pipeline);

}
