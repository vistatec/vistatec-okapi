/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.lib.beans.v1;

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Range;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.annotation.Annotations;
import net.sf.okapi.common.annotation.XLIFFTool;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.filterwriter.TMXFilterWriter;
import net.sf.okapi.common.filterwriter.ZipFilterWriter;
import net.sf.okapi.common.resource.BaseNameable;
import net.sf.okapi.common.resource.BaseReferenceable;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.Document;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.EndSubfilter;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.InlineAnnotation;
import net.sf.okapi.common.resource.MultiEvent;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.StartSubfilter;
import net.sf.okapi.common.resource.TargetPropertiesAnnotation;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonPart;
import net.sf.okapi.common.skeleton.ZipSkeleton;
import net.sf.okapi.filters.openxml.ConditionalParameters;
import net.sf.okapi.filters.openxml.OpenXMLZipFilterWriter;
import net.sf.okapi.filters.pensieve.PensieveFilterWriter;
import net.sf.okapi.filters.po.POWriter;
import net.sf.okapi.lib.persistence.BeanMapper;
import net.sf.okapi.lib.persistence.IVersionDriver;
import net.sf.okapi.lib.persistence.NamespaceMapper;
import net.sf.okapi.lib.persistence.VersionMapper;
import net.sf.okapi.lib.persistence.beans.TypeInfoBean;
import net.sf.okapi.steps.formatconversion.TableFilterWriter;
import net.sf.okapi.steps.repetitionanalysis.RepetitiveSegmentAnnotation;
import net.sf.okapi.steps.repetitionanalysis.SegmentInfo;
import net.sf.okapi.steps.tokenization.common.InputTokenAnnotation;
import net.sf.okapi.steps.tokenization.common.Lexem;
import net.sf.okapi.steps.tokenization.common.Token;
import net.sf.okapi.steps.tokenization.common.TokensAnnotation;
import net.sf.okapi.steps.wordcount.common.MetricsAnnotation;

public class OkapiBeans implements IVersionDriver {

	public static final String VERSION = "OKAPI 1.0";
	
	@SuppressWarnings("deprecation")
	@Override
	public void registerBeans(BeanMapper beanMapper) {
		// General purpose beans		
		beanMapper.registerBean(Object.class, TypeInfoBean.class); // If no bean was found, fall back to this one to store class info
		beanMapper.registerBean(IParameters.class, ParametersBean.class, true);
		
		// Specific class beans				
		beanMapper.registerBean(Event.class, EventBean.class);		
		beanMapper.registerBean(TextUnit.class, TextUnitBean.class);
		beanMapper.registerBean(RawDocument.class, RawDocumentBean.class);
		beanMapper.registerBean(Property.class, PropertyBean.class);		
		beanMapper.registerBean(ConditionalParameters.class, ConditionalParametersBean.class);
		beanMapper.registerBean(TextFragment.class, TextFragmentBean.class);
		beanMapper.registerBean(TextContainer.class, TextContainerBean.class);
		beanMapper.registerBean(Code.class, CodeBean.class);
		beanMapper.registerBean(Document.class, DocumentBean.class);
		beanMapper.registerBean(DocumentPart.class, DocumentPartBean.class);
		beanMapper.registerBean(Ending.class, EndingBean.class);
		beanMapper.registerBean(MultiEvent.class, MultiEventBean.class);
		beanMapper.registerBean(TextPart.class, TextPartBean.class);
		beanMapper.registerBean(Segment.class, SegmentBean.class);
		beanMapper.registerBean(SegmentInfo.class, SegmentInfoBean.class);
		beanMapper.registerBean(Range.class, RangeBean.class);
		beanMapper.registerBean(BaseNameable.class, BaseNameableBean.class);
		beanMapper.registerBean(BaseReferenceable.class, BaseReferenceableBean.class);
		beanMapper.registerBean(StartSubfilter.class, StartSubfilterBean.class);
		beanMapper.registerBean(EndSubfilter.class, EndSubfilterBean.class);
		beanMapper.registerBean(StartDocument.class, StartDocumentBean.class);
		beanMapper.registerBean(StartGroup.class, StartGroupBean.class);
		beanMapper.registerBean(StartSubDocument.class, StartSubDocumentBean.class);		
		beanMapper.registerBean(GenericSkeleton.class, GenericSkeletonBean.class);
		beanMapper.registerBean(GenericSkeletonPart.class, GenericSkeletonPartBean.class);
		beanMapper.registerBean(ZipSkeleton.class, ZipSkeletonBean.class);
		beanMapper.registerBean(ZipFile.class, ZipFileBean.class);
		beanMapper.registerBean(ZipEntry.class, ZipEntryBean.class);
		beanMapper.registerBean(InputStream.class, InputStreamBean.class);		
		beanMapper.registerBean(GenericFilterWriter.class, GenericFilterWriterBean.class);
		beanMapper.registerBean(TMXFilterWriter.class, TMXFilterWriterBean.class);
		beanMapper.registerBean(ZipFilterWriter.class, ZipFilterWriterBean.class);
		beanMapper.registerBean(Token.class, TokenBean.class);
		beanMapper.registerBean(Lexem.class, LexemBean.class);
		beanMapper.registerBean(LocaleId.class, LocaleIdBean.class);
		beanMapper.registerBean(AltTranslation.class, AltTranslationBean.class);
		// Registered here to require dependencies at compile-time
		beanMapper.registerBean(OpenXMLZipFilterWriter.class, TypeInfoBean.class); 		
		beanMapper.registerBean(PensieveFilterWriter.class, TypeInfoBean.class);
		beanMapper.registerBean(POWriter.class, TypeInfoBean.class);
		beanMapper.registerBean(TableFilterWriter.class, TypeInfoBean.class);
		// Annotations		
		beanMapper.registerBean(Annotations.class, AnnotationsBean.class);
		beanMapper.registerBean(AltTranslationsAnnotation.class, AltTranslationsAnnotationBean.class);		
		beanMapper.registerBean(InlineAnnotation.class, InlineAnnotationBean.class);
		beanMapper.registerBean(InputTokenAnnotation.class, InputTokenAnnotationBean.class);
		beanMapper.registerBean(MetricsAnnotation.class, MetricsAnnotationBean.class);
		beanMapper.registerBean(TargetPropertiesAnnotation.class, TargetPropertiesAnnotationBean.class);
		beanMapper.registerBean(TokensAnnotation.class, TokensAnnotationBean.class);
		beanMapper.registerBean(RepetitiveSegmentAnnotation.class, RepetitiveSegmentAnnotationBean.class);
		
		beanMapper.registerBean(XLIFFTool.class, XLIFFToolBean.class);		
		//beanMapper.registerBean(.class, Bean.class);
		
		VersionMapper.mapVersionId("1.0", VERSION);
		NamespaceMapper.mapName("net.sf.okapi.steps.xliffkit.common.persistence.versioning.TestEvent", 
			net.sf.okapi.lib.beans.v0.TestEvent.class);
	}

	@Override
	public String getVersionId() {
		return VERSION;
	}
}
