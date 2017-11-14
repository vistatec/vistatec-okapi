/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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

package net.sf.okapi.lib.beans.v2;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.annotation.IssueAnnotation;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.encoder.IEncoder;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.filterwriter.ITSContent;
import net.sf.okapi.common.resource.AlignedSegments;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.InlineAnnotation;
import net.sf.okapi.common.resource.Segments;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.filters.xliff.XLIFFSkeletonWriter;
import net.sf.okapi.filters.xliff.its.IITSDataStore;
import net.sf.okapi.filters.xliff.its.ITSLQI;
import net.sf.okapi.filters.xliff.its.ITSLQICollection;
import net.sf.okapi.filters.xliff.its.ITSProvenance;
import net.sf.okapi.filters.xliff.its.ITSProvenanceCollection;
import net.sf.okapi.filters.xliff.its.ITSStandoffManager;
import net.sf.okapi.lib.beans.v1.InlineAnnotationBean;
import net.sf.okapi.lib.beans.v1.OkapiBeans;
import net.sf.okapi.lib.persistence.BeanMapper;
import net.sf.okapi.lib.persistence.VersionMapper;

public class OkapiBeans2 extends OkapiBeans {

	public static final String VERSION = "OKAPI 2.0";
	
	@Override
	public void registerBeans(BeanMapper beanMapper) {
		super.registerBeans(beanMapper);
		
		beanMapper.registerBean(EncoderManager.class, EncoderManagerBean.class);
		beanMapper.registerBean(IEncoder.class, EncoderBean.class);
		
		beanMapper.registerBean(GenericFilterWriter.class, GenericFilterWriterBean.class);
//		beanMapper.registerBean(ZipFilterWriter.class, ZipFilterWriterBean.class);
		beanMapper.registerBean(InlineAnnotation.class, InlineAnnotationBean.class);
		beanMapper.registerBean(GenericAnnotation.class, GenericAnnotationBean.class);
		beanMapper.registerBean(IssueAnnotation.class, IssueAnnotationBean.class);
		beanMapper.registerBean(GenericAnnotations.class, GenericAnnotationsBean.class);
//		beanMapper.registerBean(ITSLQIAnnotations.class, ITSLQIAnnotationsBean.class);
		
		beanMapper.registerBean(XLIFFSkeletonWriter.class, XLIFFSkeletonWriterBean.class);
		beanMapper.registerBean(CharsetEncoder.class, CharsetEncoderBean.class);
//		beanMapper.registerBean(XLIFFContent.class, XLIFFContentBean.class);
		beanMapper.registerBean(ITSContent.class, ITSContentBean.class);
		beanMapper.registerBean(ITSStandoffManager.class, ITSStandoffManagerBean.class);
		beanMapper.registerBean(Charset.class, CharsetBean.class);		
		beanMapper.registerBean(ITSLQICollection.class, ITSLQICollectionBean.class);
		beanMapper.registerBean(ITSProvenanceCollection.class, ITSProvenanceCollectionBean.class);
		beanMapper.registerBean(IITSDataStore.class, IITSDataStoreBean.class);
		beanMapper.registerBean(ITSLQI.class, ITSLQIBean.class);		
		beanMapper.registerBean(ITSProvenance.class, ITSProvenanceBean.class);
//		beanMapper.registerBean(GenericSkeletonWriter.class, GenericSkeletonWriterBean.class);
//		beanMapper.registerBean(SubFilterSkeletonWriter.class, SubFilterSkeletonWriterBean.class);
//		beanMapper.registerBean(StartSubfilter.class, StartSubfilterBean.class);
		beanMapper.registerBean(net.sf.okapi.filters.its.Parameters.class, ITSParametersBean.class);
		
		beanMapper.registerBean(ITextUnit.class, TextUnitBean.class);
		beanMapper.registerBean(TextUnit.class, TextUnitBean.class);
		
		beanMapper.registerBean(AlignedSegments.class, AlignedSegmentsBean.class);
		beanMapper.registerBean(TextContainer.class, TextContainerBean.class);
		
		beanMapper.registerBean(ISegments.class, SegmentsBean.class);
		beanMapper.registerBean(Segments.class, SegmentsBean.class);

//		beanMapper.registerBean(LayerProvider.class, LayerProviderBean.class);		
		
		VersionMapper.mapVersionId("2.0", VERSION);
	}

	@Override
	public String getVersionId() {
		return VERSION;
	}
}
