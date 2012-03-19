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

package org.openremote.android.test.console.net;

import org.openremote.android.console.AppSettingsActivity;
import org.openremote.android.console.model.AppSettingsModel;
import org.openremote.android.console.net.ORControllerServerSwitcher;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;

/**
 * 
 * @author handy 2010-04-29
 *
 */
public class ORControllerServerSwitcherTest extends ActivityInstrumentationTestCase2<AppSettingsActivity> {
	
	public ORControllerServerSwitcherTest() {
		super("org.openremote.android.console", AppSettingsActivity.class);
	}

//	@MediumTest
//	public void testDetectGroupMembers() {
//		AppSettingsModel.setCurrentServer(getActivity(), "http://192.168.100.113:8080/controller");
//		ORControllerServerSwitcher.detectGroupMembers(getActivity());
//		Log.i("TEST INFO", ORControllerServerSwitcher.findAllGroupMembersFromFile(getActivity()).toString());
//	}
//
//	public void testDoSwitch() {
//		ORControllerServerSwitcher.doSwitch(getActivity());
//	}

}
