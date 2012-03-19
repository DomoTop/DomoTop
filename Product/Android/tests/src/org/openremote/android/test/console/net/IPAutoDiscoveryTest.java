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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.openremote.android.console.AppSettingsActivity;
import org.openremote.android.console.Constants;
import org.openremote.android.console.net.IPAutoDiscoveryClient;
import org.openremote.android.console.net.IPAutoDiscoveryServer;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

/**
 * Test for auto discovering servers.
 * 
 * @author Tomsky Wang
 */
public class IPAutoDiscoveryTest extends ActivityInstrumentationTestCase2<AppSettingsActivity> {

   public IPAutoDiscoveryTest() {
      super("org.openremote.android.console", AppSettingsActivity.class);
   }

   public void setUp() {
      IPAutoDiscoveryClient.isNetworkTypeWIFI = true;
   }
   public void tearDown() {
      IPAutoDiscoveryClient.isNetworkTypeWIFI = false;
   }
   
   public void testAutoDiscoveryServers() {
	   TelephonyManager telmgr = (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
       boolean isEmulator = "000000000000000".equalsIgnoreCase(telmgr.getDeviceId()); 
	   if (!isEmulator) {
		   getActivity().finish();
		   return;
	   }
      // Initialize the IPAutoDiscoveryServer.
      final IPAutoDiscoveryServer auto = new IPAutoDiscoveryServer();
      
      // Start the mock controller.
      new Thread(new IPAutoDiscoveryControllerServer()).start();
      
      // Run the IPAutoDiscoveryServer in a UI thread.
      getActivity().runOnUiThread(new Runnable() {
         @Override
         public void run() {
            // Run the IPAutoDiscoveryServer.
            auto.execute((Void) null);
         }
      });
      
      try {
         // Get the auto discovered servers.
         List<String> servers = auto.get();
         
         assertEquals(1, servers.size());
         assertEquals("http://127.0.0.1:8080/controller", servers.get(0));
      } catch (InterruptedException e) {
         fail("Can't auto discovery servers!");
      } catch (ExecutionException e) {
         fail("Can't auto discovery servers!");
      } finally {
    	  // Close the AppSettingsActivity.
    	  getActivity().finish();
      }
      
   }
   
}

/**
 * The class to mock the controller server.
 */
class IPAutoDiscoveryControllerServer implements Runnable {
   
   public void run() {
      final int MULTICAST_PORT = Constants.MULTICAST_PORT;
      final String MULTICAST_ADDRESS = Constants.MULTICAST_ADDRESS;
      String multicastLocation = MULTICAST_ADDRESS + ":" + MULTICAST_PORT;
      MulticastSocket socket = null;
      InetAddress address = null;
      try {
         socket = new MulticastSocket(MULTICAST_PORT);
         address = InetAddress.getByName(MULTICAST_ADDRESS);
         Log.i("OpenRemote/IPAutoDiscoveryControllerServer", "Created IP discover multicast server !");
      } catch (IOException e) {
         Log.e("OPENREMOTE/IPAutoDiscoveryControllerServer", "Can't create multicast socket on " + multicastLocation);
      }
      try {
         socket.joinGroup(address);
         Log.i("OpenRemote/IPAutoDiscoveryControllerServer", "Joined a group : "+multicastLocation);
      } catch (IOException e) {
         Log.e("OpenRemote/IPAutoDiscoveryControllerServer", "Can't join group of " + multicastLocation);
      }
      byte[] buf = new byte[512];
      DatagramPacket packet = new DatagramPacket(buf, buf.length);
      while (true) {
         try {
            Log.i("OpenRemote/IPAutoDiscoveryControllerServer", "Listening on  " + multicastLocation);
            socket.receive(packet);
            Log.i("OpenRemote/IPAutoDiscoveryControllerServer", "Received an IP auto-discovery request from " + packet.getAddress().getHostAddress());
         } catch (IOException e) {
            Log.e("OpenRemote/IPAutoDiscoveryControllerServer", "Can't receive packet on " + MULTICAST_ADDRESS + ":" + MULTICAST_PORT);
         }
         sendLocalIPBack(packet); 
      }
   }

   /**
    * Send local ip back.
    * 
    * @param packet the packet
    */
   private void sendLocalIPBack(DatagramPacket packet) {
      new Thread(new IPResponseTCPClient(packet.getAddress())).start();
   }
}

/**
 * The Class to mock controller response, it include a server string "http://127.0.0.1:8080/controller".
 */
class IPResponseTCPClient implements Runnable {
   
   /** The target ip. */
   private InetAddress targetIP;
   
   /**
    * Instantiates a new tCP client.
    * 
    * @param targetIP the target ip
    */
   public IPResponseTCPClient(InetAddress targetIP) {
      super();
      this.targetIP = targetIP;
   }

   @Override
   public void run() {
      sendTcp();
   }
   
   /**
    * Send tcp.
    */
   public void sendTcp() {
      String targetIPStr = targetIP.getHostAddress();
      String data = "http://127.0.0.1:8080/controller";
      Log.i("OpenRemote/IPResponseTCPClient", "Sending server IP '" + data + "' to " + targetIPStr);
      Socket skt = null;
      PrintWriter out = null;
      try {
         skt = new Socket(targetIP, Constants.LOCAL_SERVER_PORT);
         out = new PrintWriter(skt.getOutputStream(), true);
      } catch (IOException e) {
         Log.e("OpenRemote/IPResponseTCPClient", "Response failed! Can't create TCP socket on " + targetIPStr);
      } finally {
         out.print(data);
         out.close();
         try {
            skt.close();
         } catch (IOException e) {
            Log.e("OpenRemote/IPResponseTCPClient", "Can't close socket");
         }
      }

   }
}