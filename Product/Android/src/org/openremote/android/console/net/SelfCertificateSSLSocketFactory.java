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
package org.openremote.android.console.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.openremote.android.console.ssl.MyKeyStore;

import android.content.Context;

/**
 * This socket factory will create ssl socket that accepts self signed certificate.
 * 
 */
public class SelfCertificateSSLSocketFactory implements LayeredSocketFactory {

   private SSLContext sslcontext = null;
   private Context context;

   public SelfCertificateSSLSocketFactory(Context context)
   {
	   this.context = context;
   }
   
   /**
    * Creates a new SelfCertificateSSLSocket object.
    * 
    * @return the SSL context
    * 
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private static SSLContext createEasySSLContext(Context context) throws IOException {
      TrustManager easyTrustManager = new X509TrustManager() {
         @Override
         public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
         }

         @Override
         public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
         }

         @Override
         public X509Certificate[] getAcceptedIssuers() {
            return null;
         }
      };

      
      try {
         MyKeyStore keystore = MyKeyStore.getInstance(context);
         KeyManager[] managers = null;
         
         keystore.fillKeyStore();
         //keystore.saveKeyStore();
         
         if(!keystore.isEmpty())
         {
	         KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
	         keyManagerFactory.init(keystore.getKeyStore(), "password".toCharArray());
	    	 
	         managers = keyManagerFactory.getKeyManagers();
         }
	         
         
         SSLContext sslcontext = SSLContext.getInstance("TLS");
         sslcontext.init(managers, new TrustManager[] { easyTrustManager }, null);
         return sslcontext;
      } catch (Exception e) {
         throw new IOException(e.getMessage());
      }
   }

   private SSLContext getSSLContext() throws IOException {
      if (this.sslcontext == null) {
         this.sslcontext = createEasySSLContext(context);
      }
      return this.sslcontext;
   }

   /**
    * @see org.apache.http.conn.scheme.SocketFactory#connectSocket(java.net.Socket, java.lang.String, int,
    *      java.net.InetAddress, int, org.apache.http.params.HttpParams)
    */
   public Socket connectSocket(Socket sock, String host, int port, InetAddress localAddress, int localPort,
         HttpParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
      int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
      int soTimeout = HttpConnectionParams.getSoTimeout(params);
      InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
      SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock : createSocket());

      if ((localAddress != null) || (localPort > 0)) {
         // we need to bind explicitly
         if (localPort < 0) {
            localPort = 0; // indicates "any"
         }
         InetSocketAddress isa = new InetSocketAddress(localAddress, localPort);
         sslsock.bind(isa);
      }

      sslsock.connect(remoteAddress, connTimeout);
      sslsock.setSoTimeout(soTimeout);
      return sslsock;

   }

   /**
    * @see org.apache.http.conn.scheme.SocketFactory#createSocket()
    */
   public Socket createSocket() throws IOException {
      return getSSLContext().getSocketFactory().createSocket();
   }

   /**
    * @see org.apache.http.conn.scheme.SocketFactory#isSecure(java.net.Socket)
    */
   public boolean isSecure(Socket socket) throws IllegalArgumentException {
      return true;
   }

   /**
    * @see org.apache.http.conn.scheme.LayeredSocketFactory#createSocket(java.net.Socket, java.lang.String, int,
    *      boolean)
    */
   public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
         UnknownHostException {
      return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
   }

}
