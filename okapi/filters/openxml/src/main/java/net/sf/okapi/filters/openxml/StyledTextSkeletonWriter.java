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

import static net.sf.okapi.filters.openxml.DocumentParts.EMPTY_STRING;
import static net.sf.okapi.filters.openxml.DocumentParts.XML_HEADER;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLEventFactory;

import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.layerprovider.ILayerProvider;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.EndSubfilter;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.StartSubfilter;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

public class StyledTextSkeletonWriter implements ISkeletonWriter {
	private XMLEventFactory eventFactory;
	private LocaleId targetLocale;
	private IdGenerator nestedBlockIds = new IdGenerator(null);
	private Map<String, String> processedReferents = new HashMap<>();
	private String partName;
	private ConditionalParameters cparams;

	public StyledTextSkeletonWriter(XMLEventFactory eventFactory, String partName, ConditionalParameters cparams) {
		this.eventFactory = eventFactory;
		this.partName = partName;
		this.cparams = cparams;
	}

	/**
	 * Sets a target locale.
	 *
	 * And is used for testing purposes only.
	 *
	 * @param targetLocale A target locale
	 */
	void setTargetLocale(LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}

	@Override
	public void close() {
	}

	@Override
	public String processStartDocument(LocaleId outputLocale, String outputEncoding, ILayerProvider layer,
			EncoderManager encoderManager, StartDocument resource) {
		this.targetLocale = outputLocale;
		return XML_HEADER;
	}

	@Override
	public String processEndDocument(Ending resource) {
		return EMPTY_STRING;
	}

	@Override
	public String processStartSubDocument(StartSubDocument resource) {
		return XML_HEADER;
	}

	@Override
	public String processEndSubDocument(Ending resource) {
		return EMPTY_STRING;
	}

	@Override
	public String processStartGroup(StartGroup resource) {
		return EMPTY_STRING;
	}

	@Override
	public String processEndGroup(Ending resource) {
		return EMPTY_STRING;
	}

	@Override
	public String processTextUnit(ITextUnit tu) {
		TextContainer target = getTargetForOutput(tu);
		// Now I need to transform the block by replacing the run text with
		// the content in the target.
		// I will need to track codes and map them against the run code stack
		// produced in the original mapper.
		// The block contains chunks that look like
		// - [start block]
		// - [good stuff]: 0 or more
		// - [end block]
		// The [good stuff] is all represented somehow in the codes and the text.
		// For every character of text in the target TextContainer, we need to use
		// the most recent open code to indicate our run styling.  If there is none,
		// a default style is used.
		// I'll need to know what run element to use, maybe, which is probably info that should
		// be stored in the block.

		String serialized = null;
		// Unstyled text TUs has no skeleton, as it's always a referent.
		List<Chunk> chunks = null;
		if (tu.getSkeleton() != null) {
			XMLEventSerializer xmlWriter = new XMLEventSerializer();
			if (tu.getSkeleton() instanceof BlockSkeleton) {
				BlockSkeleton skel = ((BlockSkeleton)tu.getSkeleton());
				Block block = skel.getBlock();
				Map<Integer, XMLEvents> codeMap = skel.getCodeMap();
				// This should always have > 2 entries, as otherwise this would have been serialized
				// as a document part.
				chunks = block.getChunks();
				Nameable nameableMarkupComponent = ((Markup) chunks.get(0)).getNameableMarkupComponent();

				BidirectionalityClarifier bidirectionalityClarifier = new BidirectionalityClarifier(
						new CreationalParameters(
								eventFactory,
								nameableMarkupComponent.getName().getPrefix(),
								nameableMarkupComponent.getName().getNamespaceURI()),
						new ClarificationParameters(LocaleId.isBidirectional(targetLocale),
								LocaleId.hasCharactersAsNumeralSeparators(targetLocale),
								targetLocale.toString()));

				bidirectionalityClarifier.clarifyMarkup((Markup) chunks.get(0));
				xmlWriter.add(chunks.get(0));
				new BlockTextUnitWriter(eventFactory, block.getRunName(), block.getTextName(), codeMap, xmlWriter,
						bidirectionalityClarifier, cparams).write(target);

			} else if (tu.getSkeleton() instanceof StringItemSkeleton) {
				StringItemSkeleton skel = ((StringItemSkeleton)tu.getSkeleton());
				StringItem stringItem = skel.getStringItem();
				chunks = stringItem.getChunks();
				xmlWriter.add(chunks.get(0));
				new StringItemTextUnitWriter(eventFactory, stringItem, xmlWriter).write(target);
			} else {
				throw new IllegalArgumentException("TextUnit " + tu.getId() +
						" has no associated block content");
			}

			// Handle the final one
			xmlWriter.add(chunks.get(chunks.size() - 1));
			serialized = xmlWriter.toString();
		}
		else {
			serialized = target.toString();
		}

		// If this TU is a referent of something, it means it was part of a nested block.
		// We need to save it up for reinsertion into some other TU later on, when we find
		// the correct reference.
		if (tu.isReferent()) {
			processedReferents.put(nestedBlockIds.createId(), serialized);
			return EMPTY_STRING;
		}
		else {
			if (!processedReferents.isEmpty()) {
				serialized = resolveReferences(serialized);
			}
			return serialized;
		}
	}

	private String resolveReferences(String original) {
		// TODO get the StringBuilder directly from the XMLEvent Serializer
		StringBuilder sb = new StringBuilder(original);
		for (Object[] markerInfo = TextFragment.getRefMarker(sb); markerInfo != null;
					  markerInfo = TextFragment.getRefMarker(sb)) {
			String processedReferent = processedReferents.get(markerInfo[0]);
			sb.replace((int)markerInfo[1], (int)markerInfo[2], processedReferent);
		}
		return sb.toString();
	}

	private TextContainer getTargetForOutput(ITextUnit tu) {
		// disallow empty targets

		if (targetLocale == null) {
			return tu.getSource();
		}

		TextContainer trgCont = tu.getTarget(targetLocale);

		if (trgCont == null || trgCont.isEmpty()) {
			return tu.getSource();
		}

		return trgCont;
	}

	@Override
	public String processDocumentPart(DocumentPart documentPart) {
		MarkupSkeleton markupSkeleton = (MarkupSkeleton) documentPart.getSkeleton();
		Markup markup = markupSkeleton.getMarkup();

		Nameable nameableMarkupComponent = markup.getNameableMarkupComponent();

		if (null != nameableMarkupComponent) {
			// do care about a markup with the start markup component only, as otherwise there is nothing to clarify at all
			BidirectionalityClarifier bidirectionalityClarifier = new BidirectionalityClarifier(
					new CreationalParameters(
							eventFactory,
							nameableMarkupComponent.getName().getPrefix(),
							nameableMarkupComponent.getName().getNamespaceURI()),
					new ClarificationParameters(LocaleId.isBidirectional(targetLocale),
							LocaleId.hasCharactersAsNumeralSeparators(targetLocale),
							targetLocale.toString()));

			bidirectionalityClarifier.clarifyMarkup(markup);
		}

		return XMLEventSerializer.serialize(markup);
	}

	@Override
	public String processStartSubfilter(StartSubfilter resource) {
		return EMPTY_STRING;
	}

	@Override
	public String processEndSubfilter(EndSubfilter resource) {
		return EMPTY_STRING;
	}
}
