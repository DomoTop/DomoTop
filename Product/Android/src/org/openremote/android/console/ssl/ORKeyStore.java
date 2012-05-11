package org.openremote.android.console.ssl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.openremote.android.console.Constants;
import org.openremote.android.console.net.ORConnection;
import org.openremote.android.console.net.ORConnectionDelegate;
import org.openremote.android.console.net.ORHttpMethod;
import org.openremote.android.console.util.PhoneInformation;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
 
/** 
 * Generates and manages the keystore used to authenticate in OpenRemote
 * @author <a href="mailto:vincent.kriek@tass.nl">Vincent Kriek</a>
 */
public class ORKeyStore implements ORConnectionDelegate {
	
	/**
	 * Register the SpongyCastle Provider
	 */
	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	public final static String LOG_CATEGORY = Constants.LOG_CATEGORY + ORKeyStore.class.getName();
	private final static String KEYSTORE_FILE = "keystore.bks";
	private final static String KEYSTORE_PASSWORD = "password";
	
	private static ORKeyStore instance = null;
	private boolean isCalled = false;
	
	private KeyStore keystore = null;
	private Context context = null;
	
	//To hand data to the delegate functions
	private Handler fetchHandler = null;
	private String host = null;
	
	/**
	 * Returns a MyKeyStore instance. This instance is a singleton so every call should 
	 * return the same MyKeyStore
	 * @param context The current application context
	 * @return The MyKeyStore instance
	 */
	public static ORKeyStore getInstance(Context context)
	{
		if(instance == null)
		{
			instance = new ORKeyStore(context);
		}
		return instance;
	}
	
	/**
	 * Instantiates the KeyStore with either one found on the filesystem or create a new empty one
	 * @param context The current application context
	 */
	private ORKeyStore(Context context)
	{
		this.context = context;
		File dir = context.getFilesDir();
		File file = new File(dir, KEYSTORE_FILE);
		try{
			keystore = KeyStore.getInstance("BKS");
			if(file.exists()) {
				keystore.load(context.openFileInput(KEYSTORE_FILE), "password".toCharArray());
			} else {
				keystore.load(null, null);
			}

		} catch(KeyStoreException e) {
			Log.e(LOG_CATEGORY, "ORKeyStore:" + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			Log.e(LOG_CATEGORY, "ORKeyStore:" + e.getMessage());
		} catch (CertificateException e) {
			Log.e(LOG_CATEGORY, "ORKeyStore:" + e.getMessage());
		} catch (FileNotFoundException e) {
			Log.e(LOG_CATEGORY, "ORKeyStore:" + e.getMessage());
		} catch (IOException e) {
			Log.e(LOG_CATEGORY, "Loading keystore" + e.getMessage());
			file.delete();
		} 
	}
	
	/**
	 * Write the current loaded KeyStore to file, filename declared in KEYSTORE_FILE
	 */
	private void saveKeyStore()
	{
		FileOutputStream out = null;
		try {
			out = context.openFileOutput(KEYSTORE_FILE, Context.MODE_PRIVATE);
			keystore.store(out, KEYSTORE_PASSWORD.toCharArray());
		} catch (FileNotFoundException e) {
			Log.e(LOG_CATEGORY, "keystore:" + e.getMessage());
		} catch (KeyStoreException e) {
			Log.e(LOG_CATEGORY, "keystore:" + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			Log.e(LOG_CATEGORY, "keystore:" + e.getMessage());
		} catch (CertificateException e) {
			Log.e(LOG_CATEGORY, "keystore:" + e.getMessage());
		} catch (IOException e) {
			Log.e(LOG_CATEGORY, "Saving keystore: " + e.getMessage());
		}
	}
	
	/**
	 * Check if the keystore is empty
	 * @return true if empty
	 */
	public boolean isEmpty() {
		try {
			return keystore.size() <= 0;
		} catch (KeyStoreException e) {
			Log.e(LOG_CATEGORY, "isEmpty: " + e.getMessage());
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
	 * @param host The host from which we want to fetch our certificate
	 * @param context The current application context
	 */
	private boolean addCertificate(String host, Certificate[] chain)
	{		
		if(chain != null)
		{
		    KeyPair kp = ORKeyPair.getInstance().getKeyPair(context);
		    if(kp == null) 	  
		    {
		    	return false;
		    }
		    
		    try {
				keystore.setKeyEntry(host, 
						kp.getPrivate(),
						KEYSTORE_PASSWORD.toCharArray(),
						chain);
				
				saveKeyStore();
				return true;
			} catch (KeyStoreException e) {
				Log.e(LOG_CATEGORY, "addCertificate: " + e.getMessage());
			}
		}
		return false;
	}
	
	/**
	 * Download the signed chain from the Controller. If there is a signed chain, it means 
	 * that the client is approved. It will then parse it into a chain. The first element in 
	 * the chain is the clients public certificate, the second one is the certificate of the 
	 * Certificate Authority
	 * @param host The host from which we want to fetch our certificate
	 * @param handler The handler to be called when retrieval is done
	 * @return A chain with the client certificate and the CA certificate, or null if not yet
	 * approved
	 */
	public void getSignedChain(String host, Handler handler)
	{
		//Save pointer so we can call it later
		this.fetchHandler = handler;
		this.host = host;
		
		String url = host;
		url += "/rest/cert/get/";
		url += PhoneInformation.getInstance().getUrlEncodedDeviceName();

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					context.openFileInput(URLEncoder.encode(host) + ORPKCS10CertificationRequest.TIMESTAMP_FILE)));
			String timestamp = in.readLine();
			in.close();
			
			url += timestamp;
			
			Log.d(LOG_CATEGORY, "Fetching certificate from: " + url);
			
			isCalled = false;
			ORConnection connection = new ORConnection(context,
														ORHttpMethod.GET,
														false,
														url,
														this);											
		} catch (IOException e) {
			handler.sendEmptyMessage(1);
			Log.e(LOG_CATEGORY, "getSignedChain: " + e.getMessage());
		}		
	}
	
	/**
	 * Parse the node into just BASE64 (strip header and footer). After that parse it into a X509Certificate 
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
	   
	    if(servercert.length() <= 1) {
	    	return null;
	    }
	    
	    try {
			InputStream is = new ByteArrayInputStream(Base64.decode(servercert));
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
		    cert = (X509Certificate)cf.generateCertificate(is);
			is.close();
		} catch (IOException e) {
			Log.e(LOG_CATEGORY, "certificateFromDocument: " + e.getMessage());
		} catch (CertificateException e) {
			Log.e(LOG_CATEGORY, "certificateFromDocument: " + e.getMessage());
		}
	    
	    return cert;
	}
	
	/**
	 * Parse the InputStream received from the Controller into a XML Document
	 * @param is The InputStream containing XML
	 * @return The XML Document
	 */
	private Document XMLfromIS(InputStream is)
	{
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	
        try {
			DocumentBuilder db = dbf.newDocumentBuilder();
		    doc = db.parse(is); 
		} catch (ParserConfigurationException e) {
			Log.e(LOG_CATEGORY, "XMLfromIS: " + e.getMessage());
			return null;
		} catch (SAXException e) {
			Log.e(LOG_CATEGORY, "XMLfromIS: " + e.getMessage());
            return null;
		} catch (IOException e) {
			Log.e(LOG_CATEGORY, "XMLfromIS: " + e.getMessage());
			return null;
		}
        return doc;
	}

	/**
	 * Delete the current KeyStore saved in KEYSTORE_FILE
	 */
	public void delete() 
	{
		File dir = context.getFilesDir();
		File file = new File(dir, KEYSTORE_FILE);
		file.delete();
	}
	
	/**
	 * Delete a host from the KeyStore
	 * @param host The hostname that is to be deleted
	 */
	public void deleteHost(String host) 
	{
		try {
			keystore.deleteEntry(host);
			saveKeyStore();
		} catch (KeyStoreException e) {
			Log.e(LOG_CATEGORY, "deleteHost: " + e.getMessage());
		}
	}

	/**
	 * Check if a alias is in the keystore
	 * @param alias The alias to check
	 * @return if a alias is in the keystore
	 */
	public boolean contains(String alias)
	{
		try {
			return keystore.containsAlias(alias);
		} catch (KeyStoreException e) {
			Log.e(LOG_CATEGORY, "contains: " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * This method gets called when an ORConnection fails
	 * @param e The exception that occurred
	 */
	@Override
	public void urlConnectionDidFailWithException(Exception e) {
	    if(fetchHandler != null) {
	    	if(!isCalled)
	    	{
	    		fetchHandler.sendEmptyMessage(2);
		    	isCalled = true;
		    	Log.d(LOG_CATEGORY, "urlConnectionDidFailWithException: " + e.getMessage());
	    	}
	    }
	}

	/**
	 * This method gets called when an ORConnection get's a response
	 * @param httpResponse the HttpResponse object associated with this request
	 */
	@Override
	public void urlConnectionDidReceiveResponse(HttpResponse httpResponse) {
		if(httpResponse.getStatusLine().getStatusCode() != 200) {
		    if(fetchHandler != null) {
		    	if(!isCalled)
		    	{
		    		fetchHandler.sendEmptyMessage(1);
			    	isCalled = true;
		    	}
		    }
		}
	}

	/**
	 * This method gets called when an ORConnection finished successfully
	 * @param data The InputStream containing the body of the request 
	 */
	@Override
	public void urlConnectionDidReceiveData(InputStream data) {
		Document doc = XMLfromIS(data);
	    
		Certificate[] chain = new X509Certificate[2];
	    chain[0] = certificateFromDocument(doc.getElementsByTagName("client").item(0));   
	    chain[1] = certificateFromDocument(doc.getElementsByTagName("server").item(0));
	    
	    int what = 1;
	    if(chain[0] != null && chain[1] != null) {
		    what = addCertificate(host, chain) ? 0 : 1;
	    }
	    
	    if(fetchHandler != null) {
	    	if(!isCalled)
	    	{
	    		fetchHandler.sendEmptyMessage(what);
		    	isCalled = true;
	    	}
	    }
	}
	
	/**
	 * Return a string with information about an alias found in the keystore
	 * @param alias The alias to check
	 * @return string with information about an alias found in the keystore
	 */
	public String aliasInformation(String alias)
	{
		StringBuilder info = new StringBuilder("Certificate information:\n");
		try {
			Certificate[] certs = keystore.getCertificateChain(alias);
			if(certs != null)
			{
				X509Certificate cert = (X509Certificate) certs[0];
				info.append("Subject: " + cert.getSubjectDN().getName().replace(",", "\n\t\t\t") + "\n");
				info.append("Issuer: \t" + cert.getIssuerDN().getName().replace(",", "\n\t\t\t") + "\n");
				info.append("Valid from: " + cert.getNotBefore() + "\n");
				info.append("Valid till: " + cert.getNotAfter());
			}
			else
			{
				info.append("No certificate available.");
			}
		} catch (KeyStoreException e) {
			info.append("Can't get certificate.\nKeyStore is corrupt.");
			Log.e(LOG_CATEGORY, "aliasInformation: " + e.getMessage());
		}
		return info.toString();
	}
	
	public void checkCertificateChain(String currentServer, Handler handler) throws Exception 
	{
		boolean valid = false;
		Certificate[] chain = null;
		X509Certificate[] x509certs = null;
		String dname = null;
		
		try {
			chain = keystore.getCertificateChain(currentServer);
			
			if(chain == null)
			{
				throw new Exception("No certificate chain found for server: " + currentServer);				
			}
		 	x509certs = new X509Certificate[chain.length];
		 	
		 	for (int i = 0; i < x509certs.length; i++) {
		 		x509certs[i] = (X509Certificate)chain[i];
		 	}			
		} catch (KeyStoreException e) {
			Log.e(LOG_CATEGORY, "checkCertificateChain keystore: " + e.getMessage());
		} catch(ClassCastException e) {
			Log.e(LOG_CATEGORY, "checkCertificateChain classcast: " + e.getMessage());
		}
		
		if(x509certs != null && x509certs.length >= 1) {
			dname = x509certs[0].getSubjectDN().getName().replace(",", ", ");
			Log.d(LOG_CATEGORY, dname);
			
			try {
				
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(currentServer + "/rest/cert/check/" + URLEncoder.encode(dname, "UTF-8"));
				HttpResponse response = client.execute(request);
				
				valid = response.getStatusLine().getStatusCode() == 200;
			} catch (IOException e) {
				Log.e(LOG_CATEGORY, "checkCertificateChain io: " + e.getMessage());
			}
		}

		if(!valid) {
			getSignedChain(currentServer, handler);
		} else {
			handler.sendEmptyMessage(0);
		}
	}
}
