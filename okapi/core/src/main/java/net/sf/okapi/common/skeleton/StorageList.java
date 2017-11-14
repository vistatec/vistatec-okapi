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

package net.sf.okapi.common.skeleton;

import java.util.ArrayList;
import java.util.Set;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.resource.INameable;
import net.sf.okapi.common.resource.IReferenceable;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.StartGroup;

/**
 * provides a way to store a list of resources and implements the different
 * interfaces of the resources.
 * <p>This class is designed to be used with {@link GenericSkeletonWriter} and derived classes.
 */
public class StorageList extends ArrayList<IResource>
	implements IResource, INameable, IReferenceable {

	private static final long serialVersionUID = 1L;
	
	private StartGroup startGroup;

	/**
	 * Creates a new StorageList object with w StartGroup.
	 * @param startGroup StartGroup to start with.
	 */
	public StorageList (StartGroup startGroup) {
		this.startGroup = startGroup;
	}
	
	@Override
	public String getId () {
		return startGroup.getId();
	}

	@Override
	public void setId (String id) {
		// Not implemented: read-only info
	}

	@Override
	public ISkeleton getSkeleton () {
		return startGroup.getSkeleton();
	}

	@Override
	public void setSkeleton (ISkeleton skeleton) {
		// Not implemented: read-only info
	}

	@Override
	public boolean isReferent () {
		return startGroup.isReferent();
	}

	@Override
	public void setIsReferent (boolean value) {
		// Not implemented: read-only info
	}
	
	@Override
	public int getReferenceCount () {
		return startGroup.getReferenceCount();
	}
	
	@Override
	public void setReferenceCount (int value) {
		// Not implemented: read-only info
	}

	@Override
	public String getName () {
		return startGroup.getName();
	}

	@Override
	public Property getProperty (String name) {
		return startGroup.getProperty(name);
	}

	@Override
	public void setName (String name) {
		// Not implemented: read-only info
	}

	@Override
	public Property setProperty (Property property) {
		// Not implemented: read-only info
		return null;
	}

	@Override
	public <A extends IAnnotation> A getAnnotation (Class<A> annotationType) {
		return startGroup.getAnnotation(annotationType);
	}

	@Override
	public void setAnnotation (IAnnotation annotation) {
		startGroup.setAnnotation(annotation);
	}

	@Override
	public Property createTargetProperty(LocaleId locId,
		String name,
		boolean overwrite,
		int creationOptions)
	{
		return startGroup.createTargetProperty(locId, name, overwrite, creationOptions);
	}

	@Override
	public Property setTargetProperty (LocaleId language,
		Property property)
	{
		// Not implemented: read-only info
		return null;
	}

	@Override
	public Property getTargetProperty (LocaleId language,
		String name)
	{
		return startGroup.getTargetProperty(language, name);
	}

	@Override
	public boolean hasTargetProperty (LocaleId language,
		String name)
	{
		return startGroup.hasTargetProperty(language, name);
	}

	@Override
	public Set<String> getPropertyNames () {
		return startGroup.getPropertyNames();
	}

	@Override
	public Set<String> getTargetPropertyNames (LocaleId language) {
		return startGroup.getTargetPropertyNames(language);
	}

	@Override
	public Property getSourceProperty (String name) {
		return startGroup.getSourceProperty(name);
	}

	@Override
	public Set<String> getSourcePropertyNames () {
		return startGroup.getSourcePropertyNames();
	}

	@Override
	public Set<LocaleId> getTargetLocales () {
		return startGroup.getTargetLocales();
	}

	@Override
	public Property setSourceProperty (Property property) {
		// Not implemented: read-only info
		return null;
	}

	@Override
	public boolean isTranslatable () {
		return startGroup.isTranslatable();
	}

	@Override
	public void setIsTranslatable (boolean value) {
		// Not implemented: read-only info
	}

	@Override
	public String getType () {
		return startGroup.getType();
	}

	@Override
	public void setType (String value) {
		// Not implemented: read-only info
	}

	@Override
	public boolean preserveWhitespaces () {
		return startGroup.preserveWhitespaces();
	}

	@Override
	public void setPreserveWhitespaces (boolean value) {
		// Not implemented: read-only info
	}

	@Override
	public String getMimeType () {
		return startGroup.getMimeType();
	}

	@Override
	public void setMimeType (String value) {
		// Not implemented: read-only info		
	}

	@Override
	public boolean hasProperty (String name) {
		return startGroup.hasProperty(name);
	}

	@Override
	public boolean hasSourceProperty (String name) {
		return startGroup.hasSourceProperty(name);
	}

	@Override
	public void removeProperty (String name) {
		startGroup.removeProperty(name);
	}

	@Override
	public void removeSourceProperty (String name) {
		startGroup.removeSourceProperty(name);
	}

	@Override
	public void removeTargetProperty (LocaleId language,
		String name)
	{
		startGroup.removeTargetProperty(language, name);
	}

	@Override
	public Iterable<IAnnotation> getAnnotations () {
		return startGroup.getAnnotations();
	}

	public StartGroup getStartGroup() {
		return startGroup;
	}
}
