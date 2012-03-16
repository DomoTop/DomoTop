package com.client.certificate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.security.auth.x500.X500Principal;
import javax.security.cert.X509Certificate;
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
import org.spongycastle.asn1.ASN1Set;
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
import android.widget.Toast;

public class GenCert {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	public static String generateCertificate(Context context) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, NoSuchProviderException, SignatureException, KeyStoreException, CertificateException, IOException
	{
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);

		KeyPair keypair = keyGen.generateKeyPair();              // public/private key pair that we are creating certificate for

		X500Principal              dnName = new X500Principal("CN=" + getAccount(context));
		
		PKCS10CertificationRequest kpGen = new PKCS10CertificationRequest(
		                                                      "SHA1WithRSA",
		                                                      dnName,
		                                                      keypair.getPublic(),
		                                                      null,
		                                                      keypair.getPrivate());
		
		Log.d("client", new String(Base64.encode(kpGen.getDEREncoded())));
		
		serializeKeypair(context, keypair);
		
		String basecert = new String(Base64.encode(kpGen.getDEREncoded()));
		return postData(basecert);
	}
	
	public static boolean serializeKeypair(Context context, KeyPair keypair) 
	{	
		try { 
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
	    HttpPost httppost = new HttpPost("http://10.10.4.40:8080/controller/rest/cert/put/melroy");

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
	
	public static X509Certificate[] getData() throws IllegalStateException, IOException, DOMException, javax.security.cert.CertificateException {
		X509Certificate[] chain = new X509Certificate[2];
		
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpGet httpget = new HttpGet("http://10.10.4.40:8080/controller/rest/cert/get/melroy");
	      
	    HttpResponse response = null;
	    try {
			response = httpclient.execute(httpget);
		} catch (ClientProtocolException e) {
			Log.e("client", e.getMessage());
		} catch (IOException e) {
			Log.e("client", e.getMessage());
		}
	    
	    Document doc = XMLfromIS(response.getEntity().getContent());
	    String data = doc.toString();
	    String servercert = doc.getElementsByTagName("server").item(0).getTextContent();
	    servercert = servercert.replace("-----BEGIN CERTIFICATE-----", "");
	    servercert = servercert.replace("-----END CERTIFICATE-----","");

	    chain[0] = certificateFromDocument(doc, "server");
	    chain[1] = certificateFromDocument(doc, "client");
	    
		return chain;
	}
	
	public static X509Certificate certificateFromDocument(Document doc, String tagname) throws javax.security.cert.CertificateException 
	{
	    String data = doc.toString();
	    String servercert = doc.getElementsByTagName(tagname).item(0).getTextContent();
	    servercert = servercert.replace("-----BEGIN CERTIFICATE-----", "");
	    servercert = servercert.replace("-----END CERTIFICATE-----","");

	    return X509Certificate.getInstance(Base64.decode(servercert));
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
