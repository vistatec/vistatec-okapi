/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.lib.persistence;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.exceptions.OkapiException;

public class ReferenceResolver {
	private final static String MSG1 = "ReferenceResolver: class %s is not registered";
	private final static String MSG2 = "ReferenceResolver: cannot instantiate %s";
//	private final static String MSG3 = "ReferenceResolver.createAntiBean: anti-bean class mismatch (actual: %s, expected: %s)";
	private final static String MSG3 = "ReferenceResolver: object references are broken, reference to a non-existing object";
	private final static String MSG4 = "ReferenceResolver.createAntiBean: objClassRef cannot be null";
	private final static String MSG5 = "ReferenceResolver.createAntiBean: refId cannot be 0";
	private final static String MSG6 = "ReferenceResolver: idCounter overflow";
	
	private static Map<Thread, Long> idCounters = new ConcurrentHashMap<Thread, Long>(); 
	private long rootId = 0;
	private IPersistenceSession session;

	private Map<Object, Long> refIdLookup = new ConcurrentHashMap<Object, Long>();
	private Map<Long, Object> objectLookup = new ConcurrentHashMap<Long, Object>();
	private Map<Long, Long> rootLookup = new ConcurrentHashMap<Long, Long>();
	private Map<Object, IPersistenceBean<?>> beanCache = new ConcurrentHashMap<Object, IPersistenceBean<?>>();
	private Map<Long, IPersistenceBean<?>> beanCache2 = new ConcurrentHashMap<Long, IPersistenceBean<?>>();
	private Map<Long, Set<Long>> references = new LinkedHashMap<Long, Set<Long>>();
	private Set<Set<Long>> frames = new TreeSet<Set<Long>>(new Comparator<Set<Long>>() {
		@Override
		public int compare(Set<Long> frame1, Set<Long> frame2) {
			if (frame1 == null || frame2 == null) return 0;
			if (frame1.size() < 1 || frame2.size() < 1) return 0;
			
			// Frames are sorted by the first element
			long e1 = frame1.iterator().next();
			long e2 = frame2.iterator().next();
			
			if (e1 < e2) 
				return -1;
			else 
				if (e1 > e2) return 1;
			else
				return 0;
		}
	});	
	private Map<Long, Set<Long>> frameLookup = new ConcurrentHashMap<Long, Set<Long>>();
	private List<Object> serialized = new ArrayList<Object>();

	public ReferenceResolver(IPersistenceSession session) {
		super();
		this.session = session;
	}
	
	public void hardReset() {
		reset();
		idCounters.put(Thread.currentThread(), 0L);
		rootId = 0;
	}
	
	public void reset() {
		//!!! Sessions are not allowed to reset the global static counters
		//idCounter = 0;
		//rootId = 0;
		refIdLookup.clear();
		objectLookup.clear();
		rootLookup.clear();
		beanCache.clear();
		beanCache2.clear();
		references.clear();
		frames.clear();
		frameLookup.clear();
		serialized.clear();
	}
	
	public void releaseObject(Object obj) {
		long refId = refIdLookup.remove(obj);
		beanCache.remove(obj);
		if (refId != 0) {
			objectLookup.remove(refId);
			rootLookup.remove(refId);
			beanCache2.remove(refId);
		}
	}
	
	public void removeFrame(Set<Long> frame) {
		frames.remove(frame);
		for (Long rid : frame)
			frameLookup.remove(rid);
	}
	
	public static long generateRefId() {
		long idCounter = 0;
		if (idCounters.containsKey(Thread.currentThread())) {
			idCounter = idCounters.get(Thread.currentThread());
		}
		
		if (idCounter == Long.MAX_VALUE)
			throw new OkapiException(MSG6);
		
		idCounters.put(Thread.currentThread(), ++idCounter);
		return idCounter;
	}
	
	/**
	 * To prevent overlapping refIds.
	 * @param refId
	 */
	public static void updateRefIdGenerator(long refId) {
		long idCounter = 0;
		if (idCounters.containsKey(Thread.currentThread())) {
			idCounter = idCounters.get(Thread.currentThread());
		}
		if (idCounter < refId) {
			idCounters.put(Thread.currentThread(), refId);
		}
	}

	public long getRefIdForObject(Object obj) {
		return (refIdLookup.containsKey(obj)) ? refIdLookup.get(obj) : 0;
	}
	
	public Object getObject(long refId) {
		return objectLookup.get(refId);
	}
	
	public long getRootId(long refId) {
		if (refId < 0) return refId; // anti-bean is the root for itself 
		return (rootLookup.containsKey(refId)) ? rootLookup.get(refId) : 0;
	}

	public void setRefIdForObject(Object obj, long refId) {
		if (obj == null) return;
		if (refId == 0)
			throw new OkapiException(MSG3);
		
		updateRefIdGenerator(refId);
		refIdLookup.put(obj, refId);  // refIdLookup.get(obj)
		rootLookup.put(refId, rootId);
		objectLookup.put(refId, obj);
	}
	
	public void setReference(long parentRefId, long childRefId) {
		if (parentRefId == 0 || childRefId == 0)
			throw new OkapiException(MSG3);
			
		Set<Long> list = references.get(parentRefId);
		if (list == null) {
			list = new HashSet<Long>();
			references.put(parentRefId, list);
		}		
		list.add(childRefId);
	}

	public Map<Long, Set<Long>> getReferences() {
		return references;
	}
	
	public Set<Long> getFrame(long refId) {
		return frameLookup.get(refId);
	}
	
	public void updateFrames() {
		
//		Set<Set<Long>> frameSet = new TreeSet<Set<Long>>(new Comparator<Set<Long>>() {
//			@Override
//			public int compare(Set<Long> frame1, Set<Long> frame2) {
//				if (frame1 == null || frame2 == null) return 0;
//				if (frame1.size() < 1 || frame2.size() < 1) return 0;
//				
//				// Frames are sorted by the first element
//				long e1 = frame1.iterator().next();
//				long e2 = frame2.iterator().next();
//				
//				if (e1 < e2) 
//					return -1;
//				else 
//					if (e1 > e2) return 1;
//				else
//					return 0;
//			}
//		});
		
		frames.clear();
		frameLookup.clear();
		
		for (Long parentRefId : references.keySet()) {
			Set<Long> childRefs = references.get(parentRefId);
			if (childRefs == null) continue;
			
			for (Long childRefId : childRefs) {
				
				long parentRoot = getRootId(parentRefId);
				long childRoot = getRootId(childRefId);

				if (parentRoot == 0 || childRoot == 0)
					throw new OkapiException(MSG3);
				
				if (parentRoot == childRoot) continue; // refs within same bean
				
				Set<Long> parentFrame = getFrame(parentRoot);
				Set<Long> childFrame = getFrame(childRoot);
				
				if (parentFrame == childFrame && parentFrame != null) continue; // already both in the same frame 
			
				if (parentFrame == null && childFrame == null) { // 00
					Set<Long> frame = new TreeSet<Long>(); // default Long comparator is used
					frame.add(parentRoot);
					frame.add(childRoot);
					frames.add(frame);
					frameLookup.put(parentRoot, frame);
					frameLookup.put(childRoot, frame);
				} else 
				if (parentFrame == null && childFrame != null) { // 01
					childFrame.add(parentRoot);
					frameLookup.put(parentRoot, childFrame);
				} 
				else 
				if (parentFrame != null && childFrame == null) { // 10
					parentFrame.add(childRoot);
					frameLookup.put(childRoot, parentFrame);
				} 
				else 
				if (parentFrame != null && childFrame != null) { // 11
					// Merge frames
					parentFrame.addAll(childFrame);
					frames.remove(childFrame);
					frameLookup.remove(childRoot);
				}
			}
		}					
	}

	public void setRootId(long rootId) {
		this.rootId = rootId;		
	}
	
	public <T> IPersistenceBean<T> createBean(Class<T> classRef) {
		Class<IPersistenceBean<T>> beanClass = 
			session.getBeanClass(classRef);
		
		if (beanClass == null)
			throw(new OkapiException(String.format(MSG1, classRef.getName())));
		
		IPersistenceBean<T> bean = null; 
		try {
			bean = ClassUtil.instantiateClass(beanClass);
		} catch (Exception e) {
			throw new OkapiException(String.format(MSG2, beanClass.getName()), e);
		}
		
		return bean;
	}

	public void cacheBean(Object obj, IPersistenceBean<?> bean) {		
		beanCache.put(obj, bean);
	}
	
	public void cacheBean(IPersistenceBean<?> bean) {
		if (bean == null) return;
		beanCache2.put(bean.getRefId(), bean);
	}

	public IPersistenceBean<?> uncacheBean(Object obj) {
		IPersistenceBean<?> bean = beanCache.get(obj);
		beanCache.remove(obj); // The caller takes ownership of the object ref
		return bean;
	}
	
	public IPersistenceBean<?> uncacheBean(Long refId) {
		IPersistenceBean<?> bean = beanCache2.get(refId);
		beanCache2.remove(refId);
		return bean;
	}

	public List<List<Long>> getFrames() {
		List<List<Long>> frames = new ArrayList<List<Long>>();
		
		for (Set<Long> frame : this.frames) {
			List<Long> frameList = new ArrayList<Long>(frame);
			frames.add(frameList);
		}
		return (List<List<Long>>) frames;
	}

	public void setFrames(List<List<?>> frames) {
		frameLookup.clear();
		
		for (List<?> frame : frames) {
			Set<Long> newFrame = new TreeSet<Long>();
			this.frames.add(newFrame);
			Long rid = null;
			
			// The actual framework can read the frames as list of int of long 
			for (Object refId : frame) {
				if (refId instanceof Long) 
					rid = (Long) refId;
				else if (refId instanceof Integer)
					rid = new Long((Integer) refId);
				newFrame.add(rid);
				frameLookup.put(rid, newFrame);
			}				
		}
	}

	/**
	 * Checks if all beans in a given frame have been processed and their core objects are found in the cache. 
	 * @param frame the given frame
	 * @return true if all beans of the frame are processed
	 */
	public boolean isFrameAvailable(Set<Long> frame) {
		for (Long refId : frame)
			if (!beanCache2.containsKey(refId)) return false;
		
		return true;
	}

	/**
	 * Anti-beans are used to serialize a reference to a root object that has already been serialized as part of another bean.
	 * The anti-beans are instances of the right bean class corresponding to the given object, which refId is the inverted value
	 * of the given bean's refId. Fields or the resulting anti-bean contain default values. 
	 * @param objClassRef
	 * @param refId
	 * @return
	 */
	public <T> IPersistenceBean<T> createAntiBean(Class<T> objClassRef, long refId) {
		if (objClassRef == null)
			throw new IllegalArgumentException(MSG4);
		if (refId == 0)
			throw new IllegalArgumentException(MSG5);
		
		updateRefIdGenerator(refId);
		IPersistenceBean<T> res = createBean(objClassRef);
		if (refId > 0) refId = -refId;
		
		res.setRefId(refId);
		setReference(refId, -refId);
		return res;
	}

	public boolean isSerialized(Object obj) {
		return serialized.contains(obj);
	}

	public void setSerialized(Object obj) {
		serialized.add(obj);
	}
}
