/*===========================================================================
  Copyright (C) 2010-2012 by the Okapi Framework contributors
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

import net.sf.okapi.common.exceptions.OkapiException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

public class TextOptions {

	public Font font;
	public Color foreground;
	public Color background;
	public boolean isBidirectional;
	
	private static final String SEPARATOR = "\t";

	/**
	 * Creates a new object based on the data saved in the given string.
	 * The string is expected to have been created to {@link #toString()}.
	 * @param info the information to use to create the new object.
	 */
	public TextOptions (Device device,
		String info)
	{
		try {
			String[] parts = info.split(SEPARATOR);
			// Font data
			FontData fd = new FontData(parts[0]);
			font = new Font(device, fd);
			// Orientation
			isBidirectional = (parts[1].charAt(0)=='1');
			// Foreground color
			String[] values = parts[2].split(",");
			foreground = new Color(device, Integer.valueOf(values[0]),
				Integer.valueOf(values[1]), Integer.valueOf(values[2]));
			// Background color
			values = parts[3].split(",");
			background = new Color(device, Integer.valueOf(values[0]),
				Integer.valueOf(values[1]), Integer.valueOf(values[2]));
		}
		catch ( Throwable e ) {
			throw new OkapiException("Error creation text options from string.");
		}
	}
	
	/**
	 * Creates a new set of options by copying the information defined
	 * for a given StyledText control.
	 * @param device the device for this context.
	 * @param control the StyledText control where to copy the data from.
	 * @param sizeIncrease the size increase to apply (use 0 for none)
	 */
	public TextOptions (Device device,
		StyledText control,
		int sizeIncrease)
	{
		Font tmp = control.getFont();
		// Make the font a bit larger by default
		FontData[] fontData = tmp.getFontData();
		fontData[0].setHeight(fontData[0].getHeight()+sizeIncrease);
		font = new Font(device, fontData[0]);

		isBidirectional = (control.getOrientation() == SWT.RIGHT_TO_LEFT);
		foreground = new Color(device, control.getForeground().getRGB());
		background = new Color(device, control.getBackground().getRGB());
	}

	/**
	 * Creates a new set of options by copying the information defined
	 * for a given TextOptions object. This is a copy constructor.
	 * @param device the device for this context.
	 * @param options the TextOptions object to duplicate.
	 */
	public TextOptions (Device device,
		TextOptions options)
	{
		font = new Font(device, options.font.getFontData());
		isBidirectional = options.isBidirectional;
		foreground = new Color(device, options.foreground.getRGB());
		background = new Color(device, options.background.getRGB());
	}
	
	@Override
	protected void finalize () {
		dispose();
	}

	public void dispose () {
		if ( font != null ) font.dispose();
		if ( foreground != null ) foreground.dispose();
		if ( background != null ) background.dispose();
	}

	@Override
	public String toString () {
		StringBuilder tmp = new StringBuilder();
		// Save the font data
		FontData[] data = font.getFontData();
		tmp.append(data[0].toString());
		tmp.append(SEPARATOR);
		// Save the orientation
		tmp.append(isBidirectional ? "1" : "0");
		tmp.append(SEPARATOR);
		// Save foreground color
		RGB rgb = foreground.getRGB();
		tmp.append(String.format("%d,%d,%d", rgb.red, rgb.green, rgb.blue));
		tmp.append(SEPARATOR);
		// Save background color
		rgb = background.getRGB();
		tmp.append(String.format("%d,%d,%d", rgb.red, rgb.green, rgb.blue));
		// Return the full string
		return tmp.toString();
	}

	/**
	 * Applies this set of options to a given StyledText control.
	 * It is the responsibility of the caller to dispose of the previous font and color
	 * resources if the given control uses some that were not predefined.
	 * @param control the StyledText control to which to apply the options.
	 */
	public void applyTo (StyledText control) {
		control.setBackground(background);
		control.setForeground(foreground);
		control.setFont(font);
		control.setOrientation(isBidirectional ? SWT.RIGHT_TO_LEFT : SWT.LEFT_TO_RIGHT);
	}

}
