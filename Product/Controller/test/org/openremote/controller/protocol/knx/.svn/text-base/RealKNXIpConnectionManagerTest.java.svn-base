/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2011, OpenRemote Inc.
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
package org.openremote.controller.protocol.knx;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openremote.controller.command.CommandParameter;
import org.openremote.controller.exception.ConversionException;
import org.openremote.controller.protocol.knx.datatype.DataPointType;

public class RealKNXIpConnectionManagerTest {

   @Before
   public void setUp() throws IOException, InterruptedException {
   }

   @Test
   public void testDiscover() throws ConnectionException, IOException, InterruptedException, ConversionException {
      KNXIpConnectionManager mgr = new KNXIpConnectionManager();
      mgr.start();
      KNXConnection c = mgr.getConnection();
      c.send(GroupValueWrite.createCommand("on", DataPointType.BINARY_VALUE, mgr, new GroupAddress((byte) 0x80,
            (byte) 0x01), new CommandParameter("0")));
      c.send(GroupValueWrite.createCommand("off", DataPointType.BINARY_VALUE, mgr, new GroupAddress((byte) 0x80,
            (byte) 0x01), new CommandParameter("0")));
      mgr.stop();
   }
}
