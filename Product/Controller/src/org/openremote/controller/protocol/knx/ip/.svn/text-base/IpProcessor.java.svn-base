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
package org.openremote.controller.protocol.knx.ip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.openremote.controller.protocol.knx.ip.message.IpConnectResp;
import org.openremote.controller.protocol.knx.ip.message.IpConnectionStateResp;
import org.openremote.controller.protocol.knx.ip.message.IpDisconnectResp;
import org.openremote.controller.protocol.knx.ip.message.IpDiscoverResp;
import org.openremote.controller.protocol.knx.ip.message.IpMessage;
import org.openremote.controller.protocol.knx.ip.message.IpMessage.Primitive;
import org.openremote.controller.protocol.knx.ip.message.IpTunnelingAck;
import org.openremote.controller.protocol.knx.ip.message.IpTunnelingReq;

/**
 * IP message processor, able to :
 * <ul>
 * <li>send requests,</li>
 * <li>synchronize requests and responses,</li>
 * <li>handle incoming requests.</li>
 * </ul>
 */
class IpProcessor {
   private DatagramSocket socket;
   private Object syncLock;
   private IpMessage con;
   private IpSocketListener socketListener;
   private InetSocketAddress srcAddr;
   private IpProcessorListener listener;

   private class IpSocketListener extends Thread {
      private byte[] buffer;

      public IpSocketListener() {
         super("IpListener for " + IpProcessor.this.srcAddr);
         this.buffer = new byte[1024];
      }

      @Override
      public void run() {
         DatagramPacket p = new DatagramPacket(this.buffer, this.buffer.length);
         synchronized (this) {
            this.notify();
         }
         while (!this.isInterrupted()) {
            try {
               IpProcessor.this.socket.receive(p);
               // TODO Check sender address?

               // Create an IpMessage from received data
               IpMessage m = IpProcessor.this.create(new ByteArrayInputStream(p.getData()));

               // Handle Discovery responses specifically
               if (m instanceof IpDiscoverResp) {
                  IpProcessor.this.listener.notifyMessage(m);
               } else {
                  // Handle other messages
                  if (m.getPrimitive() == Primitive.RESP) {

                     // Handle responses
                     synchronized (IpProcessor.this.syncLock) {
                        IpProcessor.this.con = m;
                        IpProcessor.this.syncLock.notify();
                     }
                  } else {
                     // Handle requests
                     IpProcessor.this.listener.notifyMessage(m);
                  }
               }
            } catch (IOException x) {
               // Socket read error, ignore
               Thread.currentThread().interrupt();
            } catch (KnxIpException x) {
               // Invalid message, ignore
            } catch (Throwable t) {
               // TODO Ignore?
            }
         }
      }
   }

   IpProcessor(InetAddress srcAddr, IpProcessorListener listener) {
      this.syncLock = new Object();
      this.listener = listener;
      this.srcAddr = new InetSocketAddress(srcAddr, 0);
   }

   void start() throws KnxIpException, IOException, InterruptedException {
      this.socket = new DatagramSocket();
      this.srcAddr = new InetSocketAddress(this.srcAddr.getAddress(), this.socket.getLocalPort());
      this.socketListener = new IpSocketListener();
      synchronized (this.socketListener) {
         this.socketListener.start();
         this.socketListener.wait();
      }
   }

   void stop() throws InterruptedException {
      // Close socket
      this.socket.close();

      // Stop IpListener
      this.socketListener.interrupt();
      this.socketListener.join();
   }

   InetSocketAddress getSrcAddr() {
      return this.srcAddr;
   }

   synchronized IpMessage service(IpMessage message, InetSocketAddress destAddr) throws InterruptedException,
         IOException, KnxIpException {
      IpMessage out = null;
      synchronized (this.syncLock) {
         this.con = null;
         this.send(message, destAddr);
         long dt = 0;
         while (out == null && dt < message.getSyncSendTimeout()) {
            long st = System.currentTimeMillis();
            this.syncLock.wait(message.getSyncSendTimeout() - dt);
            dt += System.currentTimeMillis() - st;
            if (this.con != null && !(con instanceof IpDiscoverResp)) out = this.con;
         }
      }
      return out;
   }

   void send(IpMessage message, InetSocketAddress destAddr) throws IOException {
      this.send(message, destAddr, this.socket);
   }

   void send(IpMessage message, InetSocketAddress destAddr, DatagramSocket socket) throws IOException {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      message.write(os);
      byte[] b = os.toByteArray();
      socket.send(new DatagramPacket(b, b.length, destAddr));
   }

   private IpMessage create(InputStream is) throws IOException, KnxIpException {
      IpMessage out = null;

      // Check header is 0x06 0x10
      if ((is.read() != 0x06) || (is.read() != 0x10)) throw new KnxIpException("Invalid message header");

      // Extract Service Type Identifier
      int sti = (is.read() << 8) + is.read();

      // Extract message length
      int l = ((is.read() << 8) + is.read()) - 6;

      // Instantiate message
      switch (sti) {
      case IpConnectResp.STI:
         out = new IpConnectResp(is, l);
         break;
      case IpDisconnectResp.STI:
         out = new IpDisconnectResp(is, l);
         break;
      case IpDiscoverResp.STI:
         out = new IpDiscoverResp(is, l);
         break;
      case IpTunnelingAck.STI:
         out = new IpTunnelingAck(is, l);
         break;
      case IpTunnelingReq.STI:
         out = new IpTunnelingReq(is, l);
         break;
      case IpConnectionStateResp.STI:
         out = new IpConnectionStateResp(is, l);
         break;
      default:
         throw new KnxIpException("Unexpected message service type");
      }
      return out;
   }
}
