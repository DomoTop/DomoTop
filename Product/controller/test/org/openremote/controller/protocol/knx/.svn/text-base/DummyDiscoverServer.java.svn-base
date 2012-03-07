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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class DummyDiscoverServer extends Thread {
   public static final int PORT = 3671;
   private static final byte[] R1 = { 0x06, 0x10, 0x02, 0x02, 0x00, 0x0E, 0x08, 0x01, 127, 0, 0, 1, 0x0E, 0x56, 0, 0 };
   private MulticastSocket discoverSocket;
   private byte[] buffer;

   public DummyDiscoverServer() throws IOException {
      super("DummyDiscoverServer");
      this.buffer = new byte[1024];
      this.discoverSocket = new MulticastSocket(PORT);
      this.discoverSocket.joinGroup(InetAddress.getByName("224.0.23.12"));
   }

   @Override
   public void run() {
      synchronized (this) {
         this.notify();
      }
      while (!this.isInterrupted()) {
         DatagramPacket p = new DatagramPacket(this.buffer, this.buffer.length);
         try {
            this.discoverSocket.receive(p);
            byte[] req = p.getData();
            InetAddress rAddr = InetAddress.getByAddress(new byte[] { req[8], req[9], req[10], req[11] });
            int rPort = ((req[12] & 0xFF) << 8) + (req[13] & 0xFF);
            System.out.println("Discover server received request from " + rAddr + ":" + rPort);
            DatagramSocket s = new DatagramSocket(3672);
            s.send(new DatagramPacket(R1, R1.length, rAddr, rPort));
         } catch (IOException e) {
            System.out.println("Discover server caught " + e.getMessage());
         }
      }
   }

   @Override
   public void interrupt() {
      this.discoverSocket.close();
      super.interrupt();
   }
}
