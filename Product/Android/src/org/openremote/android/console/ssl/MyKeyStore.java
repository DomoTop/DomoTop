package org.openremote.android.console.ssl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
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
	private final static String KEYSTORE_PASSWORD = "password";
	
	private static MyKeyStore instance = null;
	
	private KeyStore keystore = null;
	private Context context = null;
		
	/**
	 * Returns a MyKeyStore instance. This instance is a singleton so every call should 
	 * return the same MyKeyStore
	 * @param context The current application context
	 * @return The MyKeyStore instance
	 */
	public static MyKeyStore getInstance(Context context)
	{
		if(instance == null)
		{
			instance = new MyKeyStore(context);
		}
		return instance;
	}
	
	/**
	 * Instantiates the KeyStore with either one found on the filesystem or create a new empty one
	 * @param context The current application context
	 */
	private MyKeyStore(Context context)
	{
		this.context = context;
		File dir = context.getFilesDir();
		File file = new File(dir, KEYSTORE_FILE);
		try{
			keystore = KeyStore.getInstance("BKS");
			if(file.exists()) {
				keystore.load(context.openFileInput("keystore.bks"), "password".toCharArray());
				//keystore.load(null, null);
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
	public void saveKeyStore()
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
	
	public boolean isEmpty() {
		try {
			return keystore.size() <= 0;
		} catch (KeyStoreException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
			return false;
		}
	}
	
	/**
	 * Return the KeyStore, could be empty, but should never be 'null'
	 * @param context The current application context
	 * @return A KeyStore with 1 alias or an empty KeyStore if the client is not yet approved
	 */
	public KeyStore getKeyStore() 
	{
		return keystore;
	}
	
	/**
	 * Fill the KeyStore. First, download the signed certificate and public key of the 
	 * Certificate Authority. If that is available it will import it into the KeyStore 
	 * with the private key
	 * @param context The current application context
	 */
	public void fillKeyStore()
	{
		Certificate[] chain = getSignedChain();
		
		if(chain != null)
		{
		    KeyPair kp = MyKeyPair.getInstance().getKeyPair(context);
		    	    
		    try {
				keystore.setKeyEntry("user", 
						kp.getPrivate(),
						"password".toCharArray(),
						chain);
				
				keystore.store(context.openFileOutput(KEYSTORE_FILE, Context.MODE_PRIVATE), KEYSTORE_PASSWORD.toCharArray());
			} catch (KeyStoreException e) {
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
	public Certificate[] getSignedChain()
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
		    
		    if(!verifyCertificate(chain[0]))
		    {
		    	Log.d(LOG_CATEGORY, "certificate invalid");
		    	return null;
		    }
		    
		    chain[1] = certificateFromDocument(doc.getElementsByTagName("server").item(0));
			
		} catch (ClientProtocolException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		} catch (IOException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		}
	    
		return chain;
	}
	
	/**
	 * Verify if the certificate matches with the public key which is saved on the system. 
	 * @param cert The certificate to check
	 * @param context The current application context
	 * @return If the certificate matches with the public key
	 */
	private boolean verifyCertificate(Certificate cert)
	{
		boolean valid = false;
		KeyPair kp = MyKeyPair.getInstance().getKeyPair(context);
		try {
			cert.verify(kp.getPublic(), "SHA1WithRSA");
			valid = true;
		} catch (InvalidKeyException e) {
			Log.d(LOG_CATEGORY, "Invalid key detected");
			valid = false;
		} catch (CertificateException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
			valid = false;
		} catch (NoSuchAlgorithmException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
			valid = false;
		} catch (NoSuchProviderException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
			valid = false;
		} catch (SignatureException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
			valid = true;
		}
		
		return valid;
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
