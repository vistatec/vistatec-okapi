/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiUserCanceledException;

/**
 * A CLI implementation of {@link IUserPrompt}.
 */
public class UserPrompt implements IUserPrompt {

	@Override
	public void initialize(Object uiParent, String title) {
		// No initialization required.
	}

	@Override
	public boolean promptYesNoCancel(String message)
		throws OkapiUserCanceledException {
		
		System.out.println(message);
		System.out.print("[Y]es/[N]o/[C]ancel: ");
		InputStreamReader stream = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(stream);;
		while (true) {
			try {
				String input = reader.readLine().toLowerCase();
				if (input.equals("yes") || input.equals("y")) return true;
				if (input.equals("no") || input.equals("n")) return false;
				if (input.equals("cancel") || input.equals("c"))
					throw new OkapiUserCanceledException("Operation was canceled by user.");
			} catch (IOException e) {
				throw new OkapiException(e);
			}
		}
	}

	@Override
	public boolean promptOKCancel(String message)
			throws OkapiUserCanceledException {
		
		System.out.println(message);
		System.out.print("[O]K/[C]ancel: ");
		InputStreamReader stream = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(stream);;
		while (true) {
			try {
				String input = reader.readLine().toLowerCase();
				if (input.equals("ok") || input.equals("o")) return true;
				if (input.equals("cancel") || input.equals("c"))
					throw new OkapiUserCanceledException("Operation was canceled by user.");
			} catch (IOException e) {
				throw new OkapiException(e);
			}
		}
	}
}
