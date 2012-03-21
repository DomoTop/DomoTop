package org.openremote.android.console.ssl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.openremote.android.console.Constants;
import org.openremote.android.console.model.AppSettingsModel;
import org.spongycastle.util.encoders.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;

public class MyKeyStore {

	public final static String LOG_CATEGORY = Constants.LOG_CATEGORY + MyKeyPair.class.getName();
	private final static String KEYSTORE_FILE = "keystore.bks";
	
	private KeyStore keystore = null;
		
	/**
	 * Instantiates the KeyStore with either one found on the filesytem or create a new empty one
	 * @param context The current application context
	 */
	public MyKeyStore(Context context)
	{
		File dir = context.getFilesDir();
		File file = new File(dir, KEYSTORE_FILE);
		try{
			keystore = KeyStore.getInstance("BKS");
			if(file.exists()) {
				keystore.load(context.openFileInput("keystore.bks"), "password".toCharArray());
			} else {
				keystore.load(null, null);
			}
		} catch(KeyStoreException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		} catch (CertificateException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		} catch (FileNotFoundException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		} catch (IOException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		} 
	}
	
	/**
	 * Write the current loaded KeyStore to file, filename declared in KEYSTORE_FILE
	 * @param context The current application context
	 */
	public void saveKeyStore(Context context)
	{
		FileOutputStream out;
		try {
			out = context.openFileOutput(KEYSTORE_FILE, Context.MODE_PRIVATE);
		    keystore.store(out, "password".toCharArray());
		} catch (FileNotFoundException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		} catch (KeyStoreException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		} catch (CertificateException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		} catch (IOException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		}
	}
	
	/**
	 * Return the KeyStore. If the KeyStore is still empty, it will check if the client is approved
	 * and fill the KeyStore. If it is not yet approved, it will return an empty KeyStore
	 * @param context The current application context
	 * @return A KeyStore with 1 alias or an empty KeyStore if the client is not yet approved
	 */
	public KeyStore getKeyStore(Context context) 
	{
		try {
			if(keystore.size() <= 0) {
				fillKeyStore(context);
			}
		} catch (KeyStoreException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		}
		return keystore;
	}
	
	/**
	 * Fill the KeyStore. First, download the signed certificate and public key of the 
	 * Certificate Authority. If that is available it will import it into the KeyStore 
	 * with the private key
	 * @param context The current application context
	 */
	private void fillKeyStore(Context context)
	{
		Certificate[] chain = getSignedChain(context);
		
		if(chain != null)
		{
		    KeyPair kp = MyKeyPair.getKeyPair(context);
		    	    
		    try {
				keystore.setKeyEntry("user", 
						kp.getPrivate(),
						"password".toCharArray(),
						chain);
			} catch (KeyStoreException e) {
				Log.e(LOG_CATEGORY, e.getMessage());
			}
		}
	}
	
	/**
	 * Download the signed chain from the Controller. If there is a signed chain, it means 
	 * that the client is approved. It will then parse it into a chain. The first element in 
	 * the chain is the clients public certificate, the second one is the certificate of the 
	 * Certificate Authority
	 * @param context The current application context
	 * @return A chain with the client certificate and the CA certificate, or null if not yet
	 * approved
	 */
	public Certificate[] getSignedChain(Context context)
	{
		Certificate[] chain = null;

		String host = AppSettingsModel.getCurrentServer(context);
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpGet httpget = new HttpGet(host + "/rest/cert/get/Vega");
	    
	    HttpResponse response = null;
	    try {	
			response = httpclient.execute(httpget);
			
			Document doc = XMLfromIS(response.getEntity().getContent());
		    
			chain = new X509Certificate[2];
		    chain[0] = certificateFromDocument(doc.getElementsByTagName("client").item(0));
		    chain[1] = certificateFromDocument(doc.getElementsByTagName("server").item(0));
			
		} catch (ClientProtocolException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		} catch (IOException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		}
	    
		return chain;
	}
	
	/**
	 * Parse the node into just BASE65 (strip header and footer). After that parse it into a X509Certificate 
	 * and return that certificate.
	 * @param node The node containing the BASE64 encoded certificate
	 * @return The certificate
	 */
	private X509Certificate certificateFromDocument(Node node)
	{
	    String servercert = node.getChildNodes().item(0).getNodeValue();
	    servercert = servercert.replace("-----BEGIN CERTIFICATE-----", "");
	    servercert = servercert.replace("-----END CERTIFICATE-----","");
	    X509Certificate cert = null;
	   
	    try {
			InputStream is = new ByteArrayInputStream(Base64.decode(servercert));
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
		    cert = (X509Certificate)cf.generateCertificate(is);
			is.close();
		} catch (IOException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		} catch (CertificateException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		}
	    
	    return cert;
	}
	
	/**
	 * Parse the InputStream received from the Controller into a XML Document
	 * @param is The InputStream containing XML
	 * @return The XML Document
	 */
	private Document XMLfromIS(InputStream is){
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	
        try {
			DocumentBuilder db = dbf.newDocumentBuilder();
		    doc = db.parse(is); 
		} catch (ParserConfigurationException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
			return null;
		} catch (SAXException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
            return null;
		} catch (IOException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
			return null;
		}
        return doc;
	}
}
