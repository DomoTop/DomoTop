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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openremote.controller.command.CommandParameter;
import org.openremote.controller.exception.ConversionException;
import org.openremote.controller.protocol.knx.datatype.DataPointType;

public class KNXIpConnectionManagerTest {
   DummyServer server;

   @Before
   public void setUp() throws IOException, InterruptedException {
      this.server = new DummyServer();
      synchronized (this.server) {
         this.server.start();
         this.server.wait();
      }
   }

   @After
   public void tearDown() throws InterruptedException {
      this.server.interrupt();
      this.server.join();
   }

   @Test
   public void testDiscover() throws ConnectionException, ConversionException, IOException, InterruptedException {
      KNXIpConnectionManager mgr = new KNXIpConnectionManager();
      DummyDiscoverServer discoverServer = new DummyDiscoverServer();
      synchronized (discoverServer) {
         discoverServer.start();
         discoverServer.wait();
      }
      mgr.start();
      KNXConnection c = mgr.getConnection();
      c.send(GroupValueWrite.createCommand("on", DataPointType.BINARY_VALUE, mgr, new GroupAddress((byte) 0x80,
            (byte) 0x00), new CommandParameter("0")));
      c.send(GroupValueWrite.createCommand("off", DataPointType.BINARY_VALUE, mgr, new GroupAddress((byte) 0x80,
            (byte) 0x00), new CommandParameter("0")));
      System.out.println("error = " + this.server.getError());
      Assert.assertNull(this.server.getError());
      discoverServer.interrupt();
      discoverServer.join();
   }

   // @Test
   // public void testConnect() throws ConnectionException, ConversionException, KnxIpException, IOException,
   // InterruptedException {
   // KNXIpConnectionManager mgr = new KNXIpConnectionManager(InetAddress.getByName("127.0.0.1"), new InetSocketAddress(
   // "127.0.0.1", DummyServer.PORT));
   // mgr.start();
   // KNXConnection c = mgr.getConnection();
   // c.send(GroupValueWrite.createCommand("on", DataPointType.BINARY_VALUE, mgr, new GroupAddress((byte) 0x80,
   // (byte) 0x00), new CommandParameter("0")));
   // c.send(GroupValueWrite.createCommand("off", DataPointType.BINARY_VALUE, mgr, new GroupAddress((byte) 0x80,
   // (byte) 0x00), new CommandParameter("0")));
   // mgr.stop();
   // Assert.assertNull(this.server.getError());
   // }
}
