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

import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.resource.TextFragment.TagType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.sf.okapi.filters.openxml.CodeTypeFactory.createCodeType;

/**
 * Converts a parsed Block structure into a TextUnit.
 */
class BlockTextUnitMapper extends TextUnitMapper {
	private static final String NESTED_ID_GENERATOR_PREFIX = "sub";
	private final Block block;


	BlockTextUnitMapper(Block block, IdGenerator idGenerator) {
		super(idGenerator);
		this.block = block;
	}

	public List<ITextUnit> getTextUnits() {
		if (textUnits == null) {
			textUnits = process();
		}
		return textUnits;
	}

	public List<ITextUnit> process() {
		// Since blocks typically start and end with markup, blocks with <= 2 chunks should
		// be empty.
		if (block.getChunks().size() <= 2) {
			// Sanity check
			for (XMLEvents chunk : block.getChunks()) {
				if (chunk instanceof Run) {
					throw new IllegalStateException(ExceptionMessages.UNEXPECTED_STRUCTURE);
				}
			}
			return Collections.emptyList();
		}
		ITextUnit textUnit = new TextUnit(idGenerator.createId());
		textUnit.setPreserveWhitespaces(true);
		TextFragment tf = new TextFragment();
		textUnit.setSource(new TextContainer(tf));

		// The first and last chunks should always be markup.  We skip them.
		List<Chunk> chunks = block.getChunks().subList(1, block.getChunks().size() - 1);
		boolean runHasText = false;
		for (Chunk chunk : chunks) {
			if (chunk instanceof Run) {
				runHasText |= processRun(tf, (Run)chunk, textUnit);
			}
			else if (chunk instanceof RunContainer) {
				RunContainer rc = (RunContainer)chunk;

				if (rc.getChunks().isEmpty()) {
					addIsolatedCode(tf, chunk);
					continue;
				}

				Code openCode = addOpeningCode(tf, rc);
				int savedFormattingCodeDepth = runCodeStack.size();
				for (int nestedRunPosition = 0; nestedRunPosition < rc.getChunks().size(); nestedRunPosition++) {
					if (rc.getChunks().get(nestedRunPosition) instanceof Run) {
						runHasText |= processNestedRun(tf, rc.getChunks(), textUnit, nestedRunPosition, savedFormattingCodeDepth);
					} else if (rc.getChunks().get(nestedRunPosition) instanceof RunContainer) {
						RunContainer nestedRunContainer = (RunContainer) rc.getChunks().get(nestedRunPosition);
						runHasText |= processNestedRunContainer(tf, nestedRunContainer, textUnit);
					} else {
						throw new OkapiException("Wrong type of node");
					}
				}
				// Close out any formatting tags that were opened inside the container
				popRunCodesToDepth(tf, savedFormattingCodeDepth);
				addClosingCode(tf, rc, openCode);
			}
			else {
				addIsolatedCode(tf, chunk);
			}
		}
		popAllRunCodes(tf);
		List<ITextUnit> tus = new ArrayList<>();
		tus.addAll(referentTus);
		// Runs containing no text can be skipped, but only if they don't
		// contain a reference to an embedded TU.  (If they do, we need
		// to anchor the skeleton here.  It would be possible to fix this,
		// but would require this class to distinguish deferred TUs from real
		// TUs in its return value, so the part handler could make a decision.)
		if (runHasText || !referentTus.isEmpty()) {
			// Deferred TUs already have their own block skeletons set
			ISkeleton skel = new BlockSkeleton(block, codeMap);
			skel.setParent(textUnit);
			textUnit.setSkeleton(skel);
			tus.add(textUnit);
		}
		return tus;
	}

	private boolean processNestedRunContainer(TextFragment tf, RunContainer rc, ITextUnit textUnit) {
		boolean runHasText = false;
		Code openNestedCode = addOpeningCode(tf, rc);
		int savedFormattingNestedCodeDepth = runCodeStack.size();
		for (int i = 0; i < rc.getChunks().size(); i++) {
			Block.BlockChunk chunk = rc.getChunks().get(i);
			if (chunk instanceof Run) {
				runHasText |= processNestedRun(tf, rc.getChunks(), textUnit, i, savedFormattingNestedCodeDepth);
			} else if (chunk instanceof RunContainer) {
				runHasText |= processNestedRunContainer(tf, (RunContainer) chunk, textUnit);
			}
		}
		popRunCodesToDepth(tf, savedFormattingNestedCodeDepth);
		addClosingCode(tf, rc, openNestedCode);
		return runHasText;
	}

	private boolean processNestedRun(TextFragment tf, List<Block.BlockChunk> chunks, ITextUnit textUnit, int runPosition, int codeStackPopsLimit) {
		referentTus.addAll(processNestedBlocks((Run) chunks.get(runPosition), textUnit.getId()));

		Run run = (Run) chunks.get(runPosition);
		int nextRunPosition = runPosition + 1;

		Run nextRun = nextRunPosition < chunks.size() && chunks.get(nextRunPosition) instanceof  Run
				? (Run) chunks.get(nextRunPosition)
				: null;

		return addRun(tf, codeStackPopsLimit, runPosition, run, nextRun);
	}

	protected List<ITextUnit> processNestedBlocks(Run run, String parentId) {
		IdGenerator nestedIdsGenerator = getNestedIdsGenerator(parentId);
		List<ITextUnit> tus = new ArrayList<>();
		for (Textual textual : run.getNestedTextualItems()) {
			if (textual instanceof Block) {
				BlockTextUnitMapper nestedMapper = new BlockTextUnitMapper((Block) textual, nestedIdsGenerator);
				tus.addAll(nestedMapper.process());
			} else if (textual instanceof UnstyledText) {
				TextUnit tu = new TextUnit(nestedIdsGenerator.createId(), ((UnstyledText) textual).getText());
				tu.setPreserveWhitespaces(true);
				tus.add(tu);
			}
		}
		for (ITextUnit tu : tus) {
			tu.setIsReferent(true);
		}
		return tus;
	}

	private IdGenerator getNestedIdsGenerator(String parentId) {
		if (nestedIdsGenerator == null) {
			nestedIdsGenerator = new IdGenerator(parentId, NESTED_ID_GENERATOR_PREFIX);
		}
		return nestedIdsGenerator;
	}

	private void popRunCodesToDepth(TextFragment tf, int desiredDepth) {
		while (runCodeStack.size() > desiredDepth) {
			addClosingCode(tf, runCodeStack.pop());
		}
	}

	private Code addOpeningCode(TextFragment tf, RunContainer rc) {
		Code code = new Code(TagType.OPENING, createCodeType(rc));
		code.setData("<" + rc.getType().getValue() + nextCodeId + ">");
		code.setId(nextCodeId);
		tf.append(code);
		codeMap.put(nextCodeId, rc);
		// Entering the container means we also assume its default properties.
		runCodeStack.push(new RunCode(nextCodeId++, rc.getDefaultRunProperties(), rc.getDefaultCombinedRunProperties()));
		return code;
	}

	private void addClosingCode(TextFragment tf, RunContainer rc, Code openCode) {
		Code code = new Code(TagType.CLOSING, openCode.getType());
		code.setData("</" + rc.getType().getValue() + openCode.getId() + ">");
		code.setId(openCode.getId());
		// Clear container default properties
		runCodeStack.pop();
		tf.append(code);
	}
}
