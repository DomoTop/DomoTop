package com.client.certificate;


import java.io.BufferedReader; 
import java.io.IOException; 
import java.io.InputStreamReader; 
import java.net.URI; 
import java.security.KeyStore; 
import java.util.List;

import org.apache.http.HttpResponse; 
import org.apache.http.HttpVersion; 
import org.apache.http.NameValuePair; 
import org.apache.http.client.HttpClient; 
import org.apache.http.client.entity.UrlEncodedFormEntity; 
import org.apache.http.client.methods.HttpGet; 
import org.apache.http.client.methods.HttpPost; 
import org.apache.http.conn.ClientConnectionManager; 
import org.apache.http.conn.scheme.PlainSocketFactory; 
import org.apache.http.conn.scheme.Scheme; 
import org.apache.http.conn.scheme.SchemeRegistry; 
import org.apache.http.conn.ssl.SSLSocketFactory; 
import org.apache.http.impl.client.DefaultHttpClient; 
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager; 
import org.apache.http.params.BasicHttpParams; 
import org.apache.http.params.HttpParams; 
import org.apache.http.params.HttpProtocolParams; 
import org.apache.http.protocol.HTTP; 

import android.util.Log;

import com.client.certificate.AndroidSSLSocketFactory;

public class AndroidHttpClient 
{ 
	public static final int HTTP_TIMEOUT = 30 * 1000; // milliseconds 
	
	public static HttpClient getHttpClient() 
	{ 
		try 
		{ 
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType()); 
			trustStore.load(null, null); 
			
			SSLSocketFactory sf = new AndroidSSLSocketFactory(trustStore); 
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); 
			
			HttpParams params = new BasicHttpParams(); 
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1); 
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8); 
			
			SchemeRegistry registry = new SchemeRegistry(); 
			//registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 8080));  //80
			registry.register(new Scheme("https", sf, 8444)); // port 443
			
			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry); 
			return new DefaultHttpClient(ccm, params); 
		} catch (Exception e) { 
			return new DefaultHttpClient(); 
		} 
	}
	
	public static String executeHttpPost(String url, List<NameValuePair> nameValuePairs) throws Exception 
	{ 
		BufferedReader in = null; 
		
		try 
		{ 
			HttpClient client = getHttpClient();             
			HttpPost request = new HttpPost(url); 
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairs); 
			request.setEntity(formEntity); 
			HttpResponse response = client.execute(request); 
			Log.d("client", "Params: " + client.getParams().toString());
			
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent())); 
			StringBuffer sb = new StringBuffer(""); 
			String line = ""; 
			String NL = System.getProperty("line.separator"); 
			
			while ((line = in.readLine()) != null) 
			{ 
				sb.append(line + NL); 
			} 
			in.close();           
			
			String result = sb.toString(); 
			return result; 
		} 
		finally 
		{ 
			if (in != null)
			{ 
				try { 
					in.close(); 
				} catch (IOException e) { 
					e.printStackTrace(); 
				} 
			} 
		} 
	}
	
	public static String executeHttpGet(String url) throws Exception
	{ 
		BufferedReader in = null; 

		try { 
			HttpClient client = getHttpClient(); 
			HttpGet request = new HttpGet(); 
			request.setURI(new URI(url)); 
			HttpResponse response = client.execute(request); 
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent())); 
			StringBuffer sb = new StringBuffer(""); 
			String line = ""; 
			String NL = System.getProperty("line.separator"); 

			while ((line = in.readLine()) != null) { 
				sb.append(line + NL); 
			} 
			in.close();        
			String result = sb.toString(); 
			return result;  
		}
		finally 
		{ 
			if (in != null) 
			{ 
				try { 
					in.close(); 
				} catch (IOException e) { 
					e.printStackTrace(); 
				} 
			} 
		} 
	}    
}  