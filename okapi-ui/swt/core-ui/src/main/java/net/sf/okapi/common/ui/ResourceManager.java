/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.common.ui;

import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.Vector;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.Util.SUPPORTED_OS;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MenuItem;

public class ResourceManager {

	@SuppressWarnings("rawtypes")
	private Class cls;
	private Display display;
	private Hashtable<String, Image> images;
	private Hashtable<String, Color> colors1;
	private Hashtable<Integer, Color> colors2;
	private String ext = ".png";
	private String subdir = "images";
	private Hashtable<String, CommandItem> commands;
	private Hashtable<String, Image[]> imageLists;

	/**
	 * Creates a ResourceManager object.
	 * @param p_Class Class to use to load the resources. This class must be in the 
	 * root location of the place where the resources are.
	 * @param p_Display Display to associate with the resources.
	 */
	public ResourceManager (@SuppressWarnings("rawtypes") Class p_Class,
		Display p_Display)
	{
		display = p_Display;
		cls = p_Class;
		images = new Hashtable<String, Image>();
		colors1 = new Hashtable<String, Color>();
		colors2 = new Hashtable<Integer, Color>();
		commands = new Hashtable<String, CommandItem>();
		imageLists = new Hashtable<String, Image[]>();
	}
	
	/**
	 * Disposes of all the resources.
	 */
	public void dispose () {
		Enumeration<Image> e1 = images.elements();
		while ( e1.hasMoreElements() ) {
			e1.nextElement().dispose();
		}
		images.clear();
		
		Enumeration<Image[]> e2 = imageLists.elements();
		Image[] list;
		while ( e2.hasMoreElements() ) {
			list = e2.nextElement();
			for ( Image image : list ) {
				image.dispose();
			}
		}
		imageLists.clear();
		
		Enumeration<Color> e3 = colors1.elements();
		while ( e3.hasMoreElements() ) {
			e3.nextElement().dispose();
		}
		colors1.clear();
		
		e3 = colors2.elements();
		while ( e3.hasMoreElements() ) {
			e3.nextElement().dispose();
		}
		colors2.clear();

		if ( commands != null )
			commands.clear();
	}
	
	/**
	 * Sets the default extension.
	 * @param p_sValue the extension, with its leading period.
	 */
	public void setDefaultExtension (String p_sValue) {
		ext = p_sValue;
	}
	
	/**
	 * Sets the default sub-directory.
	 * @param p_sValue The sub-directory, without leading or trailing
	 * separators. Make sure to use '/' for internal separators.
	 */
	public void setSubDirectory (String p_sValue) {
		subdir = p_sValue;
	}

	/**
	 * Adds an image to this resource manager.
	 * @param name Name of the image to load. This name is also the key name
	 * to retrieve the resource later on and should be unique. If the name has 
	 * no extension or no sub-directory, the default extension and sub-directory
	 * will be added automatically for the load (but the key name stays as you 
	 * defines it).
	 * The default extension is ".png", and the default sub-directory is "images".
	 * For example:
	 * m_RM.add("myImage"); loads "images/myImage.png" and the key is "myImage".
	 * m_RM.add("myImage.gif"); loads "images/myImage.gif" and the key is "myImage.gif".
	 * m_RM.add("res/myImage.gif"); loads "res/myImage.gif" and the key is "res/myImage.gif".
	 */
	public void addImage (String name) {
		String sKey = name;
		if ( name.lastIndexOf('.') == -1 ) {
			name += ext;
		}
		if (( name.indexOf(File.separatorChar) == -1 ) && ( subdir.length() != 0 )) {
			name = subdir + "/" + name; // Use '/' not File.separatorChar!
		}
		images.put(sKey, new Image(display, cls.getResourceAsStream(name)));
	}
	
	/**
	 * Adds a list of two images to this resource manager.
	 * @param listName Name of the list.
	 * @param name1 Name of the first image to load. If the name has 
	 * no extension or no sub-directory, the default extension and sub-directory
	 * will be added automatically for the load.
	 * @param name2 Name of the second image to load. If the name has 
	 * no extension or no sub-directory, the default extension and sub-directory
	 * will be added automatically for the load.
	 */
	public void addImages (String listName,
		String name1,
		String name2)
	{
		// Adjust image 1 name
		if ( name1.lastIndexOf('.') == -1 ) {
			name1 += ext;
		}
		if (( name1.indexOf(File.separatorChar) == -1 ) && ( subdir.length() != 0 )) {
			name1 = subdir + "/" + name1; // Use '/' not File.separatorChar!
		}
		Image image1 = new Image(display, cls.getResourceAsStream(name1));

		// Adjust image 2 name
		if ( name2.lastIndexOf('.') == -1 ) {
			name2 += ext;
		}
		if (( name2.indexOf(File.separatorChar) == -1 ) && ( subdir.length() != 0 )) {
			name2 = subdir + "/" + name2; // Use '/' not File.separatorChar!
		}
		Image image2 = new Image(display, cls.getResourceAsStream(name2));
	
		// Add the list
		imageLists.put(listName, new Image[]{image1, image2});
	}
	
	/**
	 * Gets the list of images for a given key.
	 * @param name The name of the list.
	 * @return The list of images for the given name.
	 */
	public Image[] getImages (String name) {
		return imageLists.get(name);
	}

	public void addColor (String name,
		int p_nRed,
		int p_nGreen,
		int p_nBlue)
	{
		colors1.put(name, new Color(display, p_nRed, p_nGreen, p_nBlue));	
	}
		
	public void addColor (int id,
		int red,
		int green,
		int blue)
	{
		colors2.put(id, new Color(display, red, green, blue));	
	}
	
	/**
	 * Retrieves a loaded images from the resource list.
	 * @param name Key name of the resource. This name is the same you used to
	 * add the resource to the list.
	 * @return The image.
	 */
	public Image getImage (String name) {
		return images.get(name);
	}
	
	public Color getColor (String name) {
		return colors1.get(name);
	}

	public Color getColor (int id) {
		return colors2.get(id);
	}

	public void setCommand (MenuItem menuItem,
		String resName) {
		CommandItem cmd = commands.get(resName);
		menuItem.setText(cmd.label);
		if ( cmd.accelerator != 0 )
			menuItem.setAccelerator(cmd.accelerator);
	}
	
	public String getCommandLabel (String resName) {
		CommandItem cmd = commands.get(resName);
		if ( cmd == null ) return "!"+resName+"!";
		return cmd.label;
	}

	/**
	 * Loads menu commands from a properties file.
	 * @param baseName the base name to use, for example "net.sf.okapi.ui.app.Commands"
	 * to load the Commands.properties file in net.sf.okapi.ui.app.
	 */
	public void loadCommands (String baseName) {
		try {
			ResourceBundle bundle = ResourceBundle.getBundle(baseName);
			commands.clear();

			Enumeration<String> enumKeys = bundle.getKeys();
			Vector<String> vect = new Vector<String>(Collections.list(enumKeys));
			Collections.sort(vect);
			
			String rootKey = "";
			CommandItem item = null;
			for ( String key : vect ) {
				// Check if it is the same root, if not add the command if we have one
				int n = key.lastIndexOf('.');
				if ( !key.substring(0, n).equals(rootKey) ) {
					if ( item != null ) {
						commands.put(rootKey, item);
					}
					// Create the new item
					item = new CommandItem();
					rootKey = key.substring(0, n);
				}
				// Fill the command item
				if ( key.endsWith(".text") ) {
					item.label = bundle.getString(key);
				}
				else if ( key.endsWith(".alt") ) {
					if ( bundle.getString(key).equals("1") ) {
						item.accelerator |= SWT.ALT;
					}
				}
				else if ( key.endsWith(".ctrl") ) {
					if ( bundle.getString(key).equals("1") ) {
						item.accelerator |= Util.getOS() == SUPPORTED_OS.MAC ? SWT.COMMAND : SWT.CONTROL;
					}
				}
				else if ( key.endsWith(".shift") ) {
					if ( bundle.getString(key).equals("1") ) {
						item.accelerator |= SWT.SHIFT;
					}
				}
				else if ( key.endsWith(".cmd") ) {
					if ( bundle.getString(key).equals("1") ) {
						item.accelerator |= SWT.COMMAND;
					}
				}
				else if ( key.endsWith(".key") ) {
					String tmp = bundle.getString(key);
					if ( tmp.equals("F1") ) item.accelerator |= SWT.F1;
					else if ( tmp.equals("F2") ) item.accelerator |= SWT.F2;
					else if ( tmp.equals("F3") ) item.accelerator |= SWT.F3;
					else if ( tmp.equals("F4") ) item.accelerator |= SWT.F4;
					else if ( tmp.equals("F5") ) item.accelerator |= SWT.F5;
					else if ( tmp.equals("F6") ) item.accelerator |= SWT.F6;
					else if ( tmp.equals("F7") ) item.accelerator |= SWT.F7;
					else if ( tmp.equals("F8") ) item.accelerator |= SWT.F8;
					else if ( tmp.equals("F9") ) item.accelerator |= SWT.F9;
					else if ( tmp.equals("F10") ) item.accelerator |= SWT.F10;
					else if ( tmp.equals("F11") ) item.accelerator |= SWT.F11;
					else if ( tmp.equals("F12") ) item.accelerator |= SWT.F12;
					else if ( tmp.equals("F13") ) item.accelerator |= SWT.F13;
					else if ( tmp.equals("F14") ) item.accelerator |= SWT.F14;
					else if ( tmp.equals("F15") ) item.accelerator |= SWT.F15;
					else if ( tmp.equals("Up") ) item.accelerator |= SWT.ARROW_UP;
					else if ( tmp.equals("Down") ) item.accelerator |= SWT.ARROW_DOWN;
					else if ( tmp.equals("Left") ) item.accelerator |= SWT.ARROW_LEFT;
					else if ( tmp.equals("Right") ) item.accelerator |= SWT.ARROW_RIGHT;
					else if ( tmp.equals("PageUp") ) item.accelerator |= SWT.PAGE_UP;
					else if ( tmp.equals("PageDown") ) item.accelerator |= SWT.PAGE_DOWN;
					else if ( tmp.equals("Home") ) item.accelerator |= SWT.HOME;
					else if ( tmp.equals("End") ) item.accelerator |= SWT.END;
					else if ( tmp.equals("Insert") ) item.accelerator |= SWT.INSERT;
					else if ( tmp.equals("Enter") ) item.accelerator |= SWT.CR;
					else if ( tmp.equals("Delete") ) item.accelerator |= SWT.DEL;
					else {
						if ( tmp.length() == 1 ) {
							item.accelerator |= tmp.codePointAt(0);
						}
						else {
							throw new OkapiIOException("Invalid key in command: "+tmp);
						}
					}
				}
				else {
					throw new OkapiIOException("Invalid keyword in command: "+key);
				}
			}
			// Add the last item
			if ( rootKey.length() > 0 ) {
				commands.put(rootKey, item);
			}
        }
		catch ( Exception e ) {
			throw new OkapiException(e);
		}
	}

}
