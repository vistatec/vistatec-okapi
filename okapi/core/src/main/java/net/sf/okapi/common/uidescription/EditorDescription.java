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

package net.sf.okapi.common.uidescription;

import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.okapi.common.ParameterDescriptor;

/**
 * Describes the different UI parts and the layout to use for a generic editor.
 */
public class EditorDescription {
	
	private String caption;
	private LinkedHashMap<String, AbstractPart> descriptors;
	private boolean defaultLabelFlushed = false;
	private boolean defaultVertical = false;
	
	/**
	 * Creates a new EditorDescription object.
	 */
	public EditorDescription () {
		descriptors = new LinkedHashMap<String, AbstractPart>();
	}
	
	/**
	 * Creates a new EditorDescription object with a given caption.
	 * @param caption the caption of the editor.
	 */
	public EditorDescription (String caption) {
		descriptors = new LinkedHashMap<String, AbstractPart>();
		setCaption(caption);
	}
	
	/**
	 * Creates a new EditorDescription object with a given caption and
	 * given default options.
	 * @param caption the caption of the editor.
	 * @param defaultVertical default value for this option.
	 * @param defaultLabelFlushed default value for this option.
	 */
	public EditorDescription (String caption,
		boolean defaultVertical,
		boolean defaultLabelFlushed)
	{
		descriptors = new LinkedHashMap<String, AbstractPart>();
		setCaption(caption);
		this.defaultVertical = defaultVertical;
		this.defaultLabelFlushed = defaultLabelFlushed;
	}
	
	/**
	 * Gets the caption for this editor.
	 * @return the caption for this editor.
	 */
	public String getCaption () {
		return caption;
	}

	/**
	 * Sets the caption for this editor.
	 * @param caption the caption for this editor.
	 */
	public void setCaption (String caption) {
		this.caption = caption;
	}
	
	/**
	 * Gets a map of the descriptor of all UI parts for this editor.
	 * @return a map of all descriptor of the UI parts.
	 */
	public Map<String, AbstractPart> getDescriptors () {
		return descriptors;
	}
	
	/**
	 * Gets the descriptor for a given UI part. 
	 * @param name the name of the UI part to lookup.
	 * @return the descriptor for the given UI part.
	 */
	public AbstractPart getDescriptor (String name) {
		return descriptors.get(name);
	}
	
	/**
	 * Adds a default text input UI part to this editor description.
	 * @param paramDescriptor the parameter descriptor for this UI part.
	 * @return the UI part created by this call.
	 */
	public TextInputPart addTextInputPart (ParameterDescriptor paramDescriptor) {
		TextInputPart desc = new TextInputPart(paramDescriptor);
		desc.setVertical(defaultVertical);
		desc.setLabelFlushed(defaultLabelFlushed);
		descriptors.put(desc.getName(), desc);
		return desc;
	}
	
	/**
	 * Adds a check box UI part to this editor description.
	 * @param paramDescriptor the parameter descriptor for this UI part.
	 * @return the UI part created by this call.
	 */
	public CheckboxPart addCheckboxPart (ParameterDescriptor paramDescriptor) {
		CheckboxPart desc = new CheckboxPart(paramDescriptor);
		desc.setVertical(defaultVertical);
		desc.setLabelFlushed(defaultLabelFlushed);
		descriptors.put(desc.getName(), desc);
		return desc;
	}
	
	/**
	 * Adds a check list UI part to this editor description.
	 * @param label the text of the label above the list
	 * @param heightHint the suggested height of the list.
	 * @return the UI part created by this call.
	 */
	public CheckListPart addCheckListPart (String label,
		int heightHint)
	{
		CheckListPart clp = new CheckListPart(label, heightHint);
		clp.setVertical(defaultVertical);
		clp.setLabelFlushed(defaultLabelFlushed);
		descriptors.put(clp.getName(), clp);
		return clp;
	}

	/**
	 * Adds a selection list UI part to this editor description.
	 * @param paramDescriptor the parameter descriptor for this UI part.
	 * @param choices the list of items values that can be selected.
	 * @return the UI part created by this call.
	 */
	public ListSelectionPart addListSelectionPart (ParameterDescriptor paramDescriptor,
		String[] choices)
	{
		ListSelectionPart desc = new ListSelectionPart(paramDescriptor, choices);
		desc.setVertical(defaultVertical);
		desc.setLabelFlushed(defaultLabelFlushed);
		descriptors.put(desc.getName(), desc);
		return desc;
	}

	/**
	 * Adds a path input field UI part to this editor description.
	 * @param paramDescriptor the parameter descriptor for this UI part.
	 * @param browseTitle the title to use for the path browsing dialog.
	 * @param forSaveAs true if the path is to save a file (vs to open one).
	 * @return the UI part created by this call.
	 */
	public PathInputPart addPathInputPart (ParameterDescriptor paramDescriptor,
		String browseTitle,
		boolean forSaveAs)
	{
		PathInputPart desc = new PathInputPart(paramDescriptor, browseTitle, forSaveAs);
		desc.setVertical(defaultVertical);
		desc.setLabelFlushed(defaultLabelFlushed);
		descriptors.put(desc.getName(), desc);
		return desc;
	}

	/**
	 * Adds a folder input field UI part to this editor description.
	 * @param paramDescriptor the parameter descriptor for this UI part.
	 * @param browseTitle the title to use for the directory browsing dialog.
	 * @return the UI part created by this call.
	 */
	public FolderInputPart addFolderInputPart (ParameterDescriptor paramDescriptor,
		String browseTitle)
	{
		FolderInputPart desc = new FolderInputPart(paramDescriptor, browseTitle);
		desc.setVertical(defaultVertical);
		desc.setLabelFlushed(defaultLabelFlushed);
		descriptors.put(desc.getName(), desc);
		return desc;
	}

	/**
	 * Adds a code-finder panel part to this editor description.
	 * @param paramDescriptor the parameter descriptor for this UI part.
	 * @return the UI part created by this call.
	 */
	public CodeFinderPart addCodeFinderPart (ParameterDescriptor paramDescriptor) {
		CodeFinderPart desc = new CodeFinderPart(paramDescriptor);
		desc.setVertical(defaultVertical);
		desc.setLabelFlushed(defaultLabelFlushed);
		descriptors.put(desc.getName(), desc);
		return desc;
	}
	
	/**
	 * Adds a spin part to this editor description.
	 * @param paramDescriptor the parameter descriptor for this UI part.
	 * @return the UI part created by this call.
	 */
	public SpinInputPart addSpinInputPart (ParameterDescriptor paramDescriptor) {
		SpinInputPart desc = new SpinInputPart(paramDescriptor);
		desc.setVertical(defaultVertical);
		desc.setLabelFlushed(defaultLabelFlushed);
		descriptors.put(desc.getName(), desc);
		return desc;
	}
	
	/**
	 * Adds a separator part to this editor description.
	 * @return the UI part created by this call.
	 */
	public SeparatorPart addSeparatorPart () {
		SeparatorPart desc = new SeparatorPart();
		desc.setVertical(defaultVertical);
		desc.setLabelFlushed(defaultLabelFlushed);
		descriptors.put(desc.getName(), desc);
		return desc;
	}
	
	public TextLabelPart addTextLabelPart (String text) {
		TextLabelPart desc = new TextLabelPart(text);
		desc.setVertical(defaultVertical);
		desc.setLabelFlushed(defaultLabelFlushed);
		descriptors.put(desc.getName(), desc);
		return desc;
	}
	
}
