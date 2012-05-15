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
import java.net.URL;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.openremote.android.console.Constants;
import org.openremote.android.console.model.AppSettingsModel;
import org.openremote.android.console.net.ORConnection;
import org.openremote.android.console.net.ORConnectionDelegate;
import org.openremote.android.console.net.ORHttpMethod;
import org.openremote.android.console.net.SelfCertificateSSLSocketFactory;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

/**
 * It's responsible for downloading resources in backgroud and updte progress in text.
 * 
 * @author handy 2010-05-10
 * @author Dan Cong
 *
 */
public class AsyncGroupLoader extends Thread {
    private static final String LOG_CATEGORY = Constants.LOG_CATEGORY + AsyncGroupLoader.class.getName();
    private Context context;
    private boolean done = false; 
	
    public AsyncGroupLoader(Context context) {
    	this.context = context;
    }
    
    @Override
    public void run() {
    	synchronized (this) {	
			String server = AppSettingsModel.getSecuredServer(context);
			if(!TextUtils.isEmpty(server)) {
		    	HttpResponse response = null;
		    	try {
					URL url = new URL(server + "/rest/device/group");
			    	HttpGet request = new HttpGet(url.toString());
			    	HttpClient client = new DefaultHttpClient();
		            Scheme sch = new Scheme(url.getProtocol(), new SelfCertificateSSLSocketFactory(context), url.getPort());
		            client.getConnectionManager().getSchemeRegistry().register(sch);
			       
					response = client.execute(request);
				} catch (ClientProtocolException e) {
					Log.e(LOG_CATEGORY + " run:", e.getMessage());
				} catch (IOException e) {
					Log.e(LOG_CATEGORY + " run:", e.getMessage());
				}
	
		    	String group = "";
		    	if(response != null && response.getStatusLine().getStatusCode() == 200) {
		    		String tmp;
		    		try {
			    		BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		    			while((tmp = in.readLine()) != null) {
		    				group += tmp;
		    			}
		    		} catch (IOException e) {
		    			Log.e(LOG_CATEGORY + " urlConnectionDidReceiveData:", e.getMessage());
		    		}	
		    		
		    	}
	    		AppSettingsModel.setGroup(context, group);
			}	
			done = true;
			notify();
		}
    }
	
    public boolean isDone() {
    	return done;
    }
    
	/*	
	@Override
	public void urlConnectionDidReceiveResponse(HttpResponse httpResponse) {
		if(httpResponse.getStatusLine().getStatusCode() == 404) {
			AppSettingsModel.setGroup(context, "");
		} else if(httpResponse.getStatusLine().getStatusCode() != 200) {
			Log.e(LOG_CATEGORY + " urlConnectionDidFailWithException", 
					httpResponse.getStatusLine().getStatusCode() + ", reason:" 
							+ httpResponse.getStatusLine().getStatusCode());
		}
		resourceLoader.setWaitingForGroup(false);
	}

	@Override
	public void urlConnectionDidReceiveData(InputStream data) {
		String group = "", tmp;
		BufferedReader in = new BufferedReader(new InputStreamReader(data));
		try {
			while((tmp = in.readLine()) != null) {
				group += tmp;
			}
		} catch (IOException e) {
			Log.e(LOG_CATEGORY + " urlConnectionDidReceiveData:", e.getMessage());
		}	
		
		AppSettingsModel.setGroup(context, group);
	}
	*/
   
}