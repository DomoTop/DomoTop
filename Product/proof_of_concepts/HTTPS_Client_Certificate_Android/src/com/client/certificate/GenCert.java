package com.client.certificate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.x500.X500Principal;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.spongycastle.jce.PKCS10CertificationRequest;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Base64;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

public class GenCert {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	public static String generateCertificate(Context context) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, NoSuchProviderException, SignatureException, KeyStoreException, CertificateException, IOException
	{
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);

		KeyPair keypair = deserializeKeypair(context);
		if(keypair == null)
		{
			keypair = keyGen.generateKeyPair(); // public/private key pair that we are creating certificate for
			serializeKeypair(context, keypair);
		}

		X500Principal              dnName = new X500Principal("CN=" + getAccount(context));
		PKCS10CertificationRequest kpGen = new PKCS10CertificationRequest(
		                                                      "SHA1WithRSA",
		                                                      dnName,
		                                                      keypair.getPublic(),
		                                                      null,
		                                                      keypair.getPrivate());
		
		Log.d("client", "Private key CSR: " + new String(keypair.getPrivate().getEncoded()));
		
		String basecert = new String(Base64.encode(kpGen.getDEREncoded()));
		return postData(basecert);
	}
	
	public static boolean serializeKeypair(Context context, KeyPair keypair) 
	{	
		try { 
			File dir = context.getFilesDir();
			File file = new File(dir, "keypair");
			
			if(file != null)
			{
				boolean deleted = file.delete();
				Log.d("client", "Cert file deleted:" + deleted);
			}
			
	      FileOutputStream output = context.openFileOutput("keypair", Context.MODE_PRIVATE);

	      ObjectOutput out = new ObjectOutputStream(output); 
	      out.writeObject(keypair); 
	      out.close(); 
	 
	      // Get the bytes of the serialized object 
	 
	      return true; 
	    } catch(IOException ioe) { 
	      Log.e("serializeObject", "error", ioe); 
	
	      return false; 
	    } 		
	}
	
	public static KeyPair deserializeKeypair(Context context) {
		KeyPair keypair = null;
		try {
			FileInputStream input = context.openFileInput("keypair");
			ObjectInputStream in = new ObjectInputStream(input);
			
			keypair = (KeyPair) in.readObject();
		} catch (IOException e) {
			Log.e("client", e.getMessage());
		} catch (ClassNotFoundException e) {
			Log.e("client", e.getMessage());
		}
		
		return keypair;
	}
   
	public static String getAccount(Context context)
    {
    	AccountManager manager = AccountManager.get(context);
    	Account acc = manager.getAccounts()[0];
    	return acc.name;
    }
   
	public static String postData(String csr) {
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("http://10.10.4.31:8080/controller/rest/cert/put/vincent6");

	    try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("csr", URLEncoder.encode(csr)));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        return response.getStatusLine().getStatusCode() + "\n" + csr;
	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    }
	    return csr;
	} 
	
	public static KeyStore getData(Context context) throws IllegalStateException, IOException, DOMException, CertificateException, KeyStoreException, NoSuchAlgorithmException 
	{
		KeyStore keystore = KeyStore.getInstance("BKS");
		File dir = context.getFilesDir();
		File file = new File(dir, "keystore.bks");
		if(file.exists()) {
			keystore.load(context.openFileInput("keystore.bks"), "password".toCharArray());
		} else {
			Certificate[] chain = new X509Certificate[2];
			
		    HttpClient httpclient = new DefaultHttpClient();
		    HttpGet httpget = new HttpGet("http://10.10.4.31:8080/controller/rest/cert/get/vincent6");
		      
		    HttpResponse response = null;
		    try {
				response = httpclient.execute(httpget);
			} catch (ClientProtocolException e) {
				Log.e("client", e.getMessage());
			} catch (IOException e) {
				Log.e("client", e.getMessage());
			}
		    
		    Document doc = XMLfromIS(response.getEntity().getContent());
		    
		    chain[0] = certificateFromDocument(doc, "client");
		    chain[1] = certificateFromDocument(doc, "server");
		    keystore = KeyStore.getInstance("BKS");
		    keystore.load(null, null);
		    KeyPair kp = deserializeKeypair(context);
		    
			Log.d("client", "Private key CRT: " + new String(kp.getPrivate().getEncoded()));
		    
		    keystore.setKeyEntry("user", 
		    		kp.getPrivate(),
		    		"password".toCharArray(),
		    		chain);
		    
		    keystore.store(context.openFileOutput("keystore.bks", Context.MODE_PRIVATE), "password".toCharArray());
		}
		return keystore;
	}
	
	public static X509Certificate certificateFromDocument(Document doc, String tagname) throws CertificateException, IOException 
	{
	    String servercert = doc.getElementsByTagName(tagname).item(0).getTextContent();
	    servercert = servercert.replace("-----BEGIN CERTIFICATE-----", "");
	    servercert = servercert.replace("-----END CERTIFICATE-----","");
	    
		InputStream is = new ByteArrayInputStream(Base64.decode(servercert));
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
	    X509Certificate cert = (X509Certificate)cf.generateCertificate(is);
	    is.close();
	    
	    return cert;
	    //return X509Certificate.getInstance(Base64.decode(servercert));
	}
	
	public static Document XMLfromIS(InputStream is){
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	
        try {
			DocumentBuilder db = dbf.newDocumentBuilder();
		    doc = db.parse(is); 
		} catch (ParserConfigurationException e) {
			System.out.println("XML parse error: " + e.getMessage());
			return null;
		} catch (SAXException e) {
			System.out.println("Wrong XML file structure: " + e.getMessage());
            return null;
		} catch (IOException e) {
			System.out.println("I/O exeption: " + e.getMessage());
			return null;
		}
        return doc;
	}

}
