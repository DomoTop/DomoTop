package org.openremote.android.console.ssl;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;

import javax.security.auth.x500.X500Principal;

import org.openremote.android.console.Constants;
import org.spongycastle.jce.PKCS10CertificationRequest;
import org.spongycastle.util.encoders.Base64;

import android.content.Context;
import android.util.Log;

public class CertificationRequest {
	
	// Constants ------------------------------------------------------------------------------------
	public final static String LOG_CATEGORY = Constants.LOG_CATEGORY + MyKeyPair.class.getName();
	 
	private static final String CSR_ALGORITHM = "SHA1WithRSA";
	
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
}
