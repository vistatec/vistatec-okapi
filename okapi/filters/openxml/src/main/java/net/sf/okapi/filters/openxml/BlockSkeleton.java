/*===========================================================================
  Copyright (C) 2016-2017 by the Okapi Framework contributors
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

package net.sf.okapi.filters.openxml;

import java.util.HashMap;
import java.util.Map;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;

public class BlockSkeleton implements ISkeleton {
	private Block block;
	private IResource parent;
	private Map<Integer, XMLEvents> codeMap = new HashMap<>();

	public BlockSkeleton(Block block, Map<Integer, XMLEvents> codeMap) {
		this.block = block;
		this.codeMap = codeMap;
	}

	@Override
	public ISkeleton clone() {
		BlockSkeleton blockSkeleton =  new BlockSkeleton(block, codeMap);
		blockSkeleton.setParent(getParent());
		return  blockSkeleton;
	}

	public Block getBlock() {
		return block;
	}

	public Map<Integer, XMLEvents> getCodeMap() {
		return codeMap;
	}

	@Override
	public void setParent(IResource parent) {
		this.parent = parent;
	}

	@Override
	public IResource getParent() {
		return parent;
	}

}
