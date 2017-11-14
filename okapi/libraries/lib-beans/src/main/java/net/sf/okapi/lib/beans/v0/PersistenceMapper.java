/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.lib.beans.v0;

import java.util.LinkedHashMap;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.BaseNameable;
import net.sf.okapi.common.resource.BaseReferenceable;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.Document;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.InlineAnnotation;
import net.sf.okapi.common.resource.MultiEvent;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TargetPropertiesAnnotation;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonPart;
import net.sf.okapi.common.skeleton.ZipSkeleton;
import net.sf.okapi.lib.persistence.BeanMapper;
import net.sf.okapi.lib.persistence.IVersionDriver;

@Deprecated
public class PersistenceMapper implements IVersionDriver {
	
	private static final String MSG1 = "PersistenceFactory: bean mapping is not initialized";
	private static final String MSG2 = "PersistenceFactory: unknown class: %s";
	private static final String MSG3 = "PersistenceFactory: class %s is not registered";
	private static final String MSG4 = "PersistenceFactory: cannot instantiate %s";	
	private static final String MSG5 = "PersistenceFactory: Class reference cannot be empty";
	
	// !!! LinkedHashMap to preserve registration order
	private static LinkedHashMap<Class<?>, Class<? extends IPersistenceBean>> beanMapping;
	//private static ConcurrentHashMap<Class<? extends IPersistenceBean>, IPersistenceBean> persistenceCache;
	
	static {
		beanMapping = new LinkedHashMap<Class<?>, Class<? extends IPersistenceBean>> ();
		//persistenceCache = new ConcurrentHashMap<Class<? extends IPersistenceBean>, IPersistenceBean> ();
	}
	
	public static void registerBean(
			Class<?> classRef, 
			Class<? extends IPersistenceBean> beanClassRef) {
		if (classRef == null || beanClassRef == null)
			throw(new IllegalArgumentException());
		
		if (beanMapping == null)
			throw(new OkapiException(MSG1));
		
		beanMapping.put(classRef, beanClassRef);		
	}
	
	public static Class<? extends IPersistenceBean> getBeanClass(Class<?> classRef) {
		if (classRef == null)
			throw(new IllegalArgumentException(MSG5));
	
		if (beanMapping == null)
			throw(new OkapiException(MSG1));
		
		Class<? extends IPersistenceBean> beanClass = beanMapping.get(classRef);
		
		// If not found explicitly, try to find a matching bean
		if (beanClass == null)
			for (Class<?> cls : beanMapping.keySet())
				if (cls.isAssignableFrom(classRef)) {
					beanClass = beanMapping.get(cls);
					break;
				}
		
		return beanClass;		
	}
	
	public static Class<? extends IPersistenceBean> getBeanClass(String className) {
			
		Class<? extends IPersistenceBean> res = null;
		try {
			res = getBeanClass(Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw(new OkapiException(String.format(MSG2, className)));
		}
		return res;		
	}
	
	public static IPersistenceBean getBean(Class<?> classRef) {
		Class<? extends IPersistenceBean> beanClass = 
			getBeanClass(classRef); // Checks for skelClass == null, beanMapping == null
		
		if (beanClass == null)
			throw(new OkapiException(String.format(MSG3, classRef.getName())));
		
//		if (persistenceCache == null)
//			throw(new OkapiException(MSG2));
		
		IPersistenceBean bean = null; //persistenceCache.get(beanClass); 
		//if (bean == null) {
			try {
				bean = ClassUtil.instantiateClass(beanClass);
				//persistenceCache.put(beanClass, bean);
			} catch (Exception e) {
				throw new OkapiException(String.format(MSG4, beanClass.getName()), e);
			}
		//}		
		return bean;		
	}
	
	@Override
	public void registerBeans(BeanMapper beanMapper) {
		// General purpose beans
		registerBean(IParameters.class, ParametersBean.class);
		registerBean(IFilterWriter.class, FilterWriterBean.class);
		registerBean(Object.class, TypeInfoBean.class); // If no bean was found, use just this one to store class info
		
		// Specific class beans
		registerBean(Event.class, EventBean.class);
		registerBean(ITextUnit.class, TextUnitBean.class);
		registerBean(RawDocument.class, RawDocumentBean.class);
		registerBean(Property.class, PropertyBean.class);
		registerBean(TextFragment.class, TextFragmentBean.class);
		registerBean(TextContainer.class, TextContainerBean.class);
		registerBean(Code.class, CodeBean.class);
		registerBean(Document.class, DocumentBean.class);
		registerBean(DocumentPart.class, DocumentPartBean.class);
		registerBean(Ending.class, EndingBean.class);
		registerBean(MultiEvent.class, MultiEventBean.class);
		registerBean(Segment.class, SegmentBean.class);
		registerBean(BaseNameable.class, BaseNameableBean.class);
		registerBean(BaseReferenceable.class, BaseReferenceableBean.class);
		registerBean(StartDocument.class, StartDocumentBean.class);
		registerBean(StartGroup.class, StartGroupBean.class);
		registerBean(StartSubDocument.class, StartSubDocumentBean.class);
		registerBean(TargetPropertiesAnnotation.class, TargetPropertiesAnnotationBean.class);
		registerBean(GenericSkeleton.class, GenericSkeletonBean.class);
		registerBean(GenericSkeletonPart.class, GenericSkeletonPartBean.class);
		registerBean(ZipSkeleton.class, ZipSkeletonBean.class);		
		registerBean(InlineAnnotation.class, InlineAnnotationBean.class);		
	}

	@Override
	public String getVersionId() {		
		return "OKAPI 0.0";
	}
}
