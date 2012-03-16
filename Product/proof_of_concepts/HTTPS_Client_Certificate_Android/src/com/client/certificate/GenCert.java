package com.client.certificate;

import java.io.IOException;
import java.math.BigInteger;
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
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.security.auth.x500.X500Principal;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.spongycastle.asn1.DERSet;
import org.spongycastle.jce.PKCS10CertificationRequest;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.x509.X509V1CertificateGenerator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

public class GenCert {

	static {
		Security.addProvider(new BouncyCastleProvider());
		}
	
	@SuppressWarnings("deprecation")
	public static String generateCertificate(Context context) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, NoSuchProviderException, SignatureException, KeyStoreException, CertificateException, IOException
	{
		Date startDate = new Date();                // time from which certificate is valid
		Date expiryDate = new Date();               // time after which certificate is not valid
		BigInteger serialNumber = new BigInteger("10");       // serial number for certificate
		//PrivateKey caKey = ...;              // private key of the certifying authority (ca) certificate
		//X509Certificate caCert = ...;        // public key certificate of the certifying authority
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);

		KeyPair keypair = keyGen.generateKeyPair();              // public/private key pair that we are creating certificate for

		X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
		X500Principal              dnName = new X500Principal("CN=" + getAccount(context));

		certGen.setSerialNumber(serialNumber);
		certGen.setIssuerDN(dnName);
		certGen.setNotBefore(startDate);
		certGen.setNotAfter(expiryDate);
		certGen.setSubjectDN(dnName);                       // note: same as issuer
		certGen.setPublicKey(keypair.getPublic());
		certGen.setSignatureAlgorithm("SHA1withRSA");

		X509Certificate[] chain = new X509Certificate[1];
		chain[0] = certGen.generate(keypair.getPrivate(), "BC");

		KeyStore ks = KeyStore.getInstance("BKS");
		ks.load(null, null);
		ks.setKeyEntry("user", keypair.getPrivate().getEncoded(), chain);
		

		PKCS10CertificationRequest kpGen = new PKCS10CertificationRequest(
		                                                      "SHA1WithRSA",
		                                                      dnName,
		                                                      keypair.getPublic(),
		                                                      null,
		                                                      keypair.getPrivate());
		
		Log.d("client", new String(Base64.encode(kpGen.getDEREncoded())));
		
		String basecert = new String(Base64.encode(kpGen.getDEREncoded()));
		return postData(basecert);
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

}
