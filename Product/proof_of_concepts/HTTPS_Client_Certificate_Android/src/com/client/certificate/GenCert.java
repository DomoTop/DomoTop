package com.client.certificate;

import java.io.IOException;
import java.math.BigInteger;
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
import java.util.Date;
import java.util.logging.Logger;

import javax.security.auth.x500.X500Principal;

import org.spongycastle.asn1.DERSet;
import org.spongycastle.jce.PKCS10CertificationRequest;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.x509.X509V1CertificateGenerator;

import android.util.Log;

public class GenCert {

	static {
		Security.addProvider(new BouncyCastleProvider());
		}
	
	@SuppressWarnings("deprecation")
	public static String generateCertificate() throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, NoSuchProviderException, SignatureException, KeyStoreException, CertificateException, IOException
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
		X500Principal              dnName = new X500Principal("CN=Test CA Certificate");

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
		return new String(Base64.encode(kpGen.getDEREncoded()));
	}


}
