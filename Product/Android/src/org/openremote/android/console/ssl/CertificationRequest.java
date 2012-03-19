package org.openremote.android.console.ssl;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.openremote.android.console.Constants;
import org.spongycastle.jce.PKCS10CertificationRequest;
import org.spongycastle.util.encoders.Base64;

import android.content.Context;
import android.util.Log;

public class CertificationRequest {
	
	// Constants ------------------------------------------------------------------------------------
	public final static String LOG_CATEGORY = Constants.LOG_CATEGORY + MyKeyPair.class.getName();
	 
	private static final String CSR_ALGORITHM = "SHA1WithRSA";
	
	private static final String TEMP_CN = "Vinnie";
	
	/**
	 * Generate a PKCS10 Certification Request. This request could be used to sing a certificate
	 * which can be used to authenticate to the server.
	 * 
	 * This method returns it Base64 encoded
	 * @param context The current application context
	 * @param commonName The Common Name to be included in the certificate, should be human readable
	 * @return The Certification Request in Base64
	 */
	public static String getCertificationRequestAsBase64(Context context, String commonName)
	{
		String basecert = new String(Base64.encode(getCertificationRequest(context, commonName).getDEREncoded()));
		
		//TODO NewLine every 65 characters
		
		return basecert;
	}
	
	/**
	 * Generate a PKCS10 Certification Request. This request could be used to sing a certificate
	 * which can be used to authenticate to the server.
	 * @param context The current application context
	 * @param commonName The Common Name to be included in the certificate, should be human readable
	 * @return The Certification Request
	 */
	public static PKCS10CertificationRequest getCertificationRequest(Context context, String commonName)
	{
		KeyPair keypair = MyKeyPair.getKeyPair(context);
		
		X500Principal              dnName = new X500Principal("CN=" + commonName);
		PKCS10CertificationRequest kpGen = null;
		try {
			kpGen = new PKCS10CertificationRequest(
	                                  CSR_ALGORITHM,
	                                  dnName,
	                                  keypair.getPublic(),
	                                  null,
	                                  keypair.getPrivate());
		} catch (InvalidKeyException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		} catch (NoSuchProviderException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		} catch (SignatureException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		}
		
		return kpGen;
	}
	
	/**
	 * Generate a certification request and submit it to the server, where it can be approved or disproved.
	 * @param context The current application context
	 * @param host The host to send CSR to
	 * @param port The port to send CSR to 
	 * @return The HTTP status code of the request or -1 if something local has gone wrong
	 */
	public static int submitCertificationRequest(Context context, String host, int port)
	{
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(host + ":" + port + "/controller/rest/cert/put/" + TEMP_CN);

	    try {
	    	String csr = getCertificationRequestAsBase64(context, TEMP_CN);
	    	
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
	        nameValuePairs.add(new BasicNameValuePair("csr", URLEncoder.encode(csr)));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        return response.getStatusLine().getStatusCode();
	    } catch (ClientProtocolException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
	    } catch (IOException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
	    }
	    return -1;
	}
}
