/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2010, OpenRemote Inc.
*
* See the contributors.txt file in the distribution for a
* full listing of individual contributors.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package org.openremote.android.console.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.openremote.android.console.Constants;
import org.openremote.android.console.ssl.ORKeyPair;

import android.util.Log;

/**
 * 
 * @author handy 2010-04-29
 *
 */
public class StringUtil {
	public final static String LOG_CATEGORY = Constants.LOG_CATEGORY + StringUtil.class.getName();

	/** Marks the specified controllerServerURL selected. */
	public static String markControllerServerURLSelected(String controllerServerURL) {
		return "+" + controllerServerURL;
	}
	
	/** Removes the specified url selected. */
	public static String removeControllerServerURLSelected(String url) {
		return url.replaceAll("+", "");
	}
	
	public static String stringFromInputStream(InputStream is) {
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		StringBuilder total = new StringBuilder();
		try {
			String line;
			while ((line = r.readLine()) != null) {
			    total.append(line);
			}
		} catch (IOException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		}
		
		return total.toString();
	}
	
}
