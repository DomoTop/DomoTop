package org.openremote.android.console.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.openremote.android.console.Constants;
import org.spongycastle.jce.provider.JDKMessageDigest.MD5;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;
import org.spongycastle.util.encoders.HexEncoder;

import android.content.Context;
import android.util.Log;

public class ORKeyPair {

	// Constants ------------------------------------------------------------------------------------
	public final static String LOG_CATEGORY = Constants.LOG_CATEGORY + ORKeyPair.class.getName();
	
	private static final String KEYPAIR_FILE = "keypair";
	private static final int KEYPAIR_SIZE = 2048;
	private static final String KEYPAIR_ALGORITHM = "RSA";
	
	private KeyPair keypair;
	
	private static ORKeyPair instance = null;
	
	public static ORKeyPair getInstance() 
	{
		if(instance == null)
		{
			instance = new ORKeyPair();
		}
		return instance;
	}
	
	/**
	 * Generates a KeyPair to be used in a certificate, this KeyPair is generated the first time 
	 * and then serialized. It will return the just one KeyPair, which is never deleted.
	 * @param context The current application context
	 * @return A KeyPair 
	 */
	public KeyPair getKeyPair(Context context)
	{
		if(keypair == null)
		{
			keypair = deserializeKeypair(context);
		}

		if(keypair == null)
		{		
			KeyPairGenerator keyGen = null;
			try {
				keyGen = KeyPairGenerator.getInstance(KEYPAIR_ALGORITHM);
			} catch (NoSuchAlgorithmException e) {
				Log.e(LOG_CATEGORY, "KeyPairGenerator could not be initialized with algorithm: " + KEYPAIR_ALGORITHM);
			}
			
			keyGen.initialize(KEYPAIR_SIZE);
			
			keypair = keyGen.generateKeyPair(); 
			serializeKeypair(context, keypair);
		}
		
		return keypair;
	}
	
	/**
	 * Returns a PIN code which is also generated on the server to verify a users authenticity
	 * @param context The current application context
	 * @return A four character string containing the PIN
	 */
	public String getPIN(Context context)
	{
		String public64 = new String(Base64.encode(getKeyPair(context).getPublic().getEncoded()));
		String formatted = "";
		for(int i = 0; i < public64.length(); i++)
		{
			if(i % 64 == 0 && i != 0)
			{
				formatted += "\n";
			}
			formatted += public64.charAt(i);
		}
		Log.d(LOG_CATEGORY, public64);

		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m.update(formatted.getBytes());

		byte[] s = Hex.encode(m.digest());
	
		return new String(s).substring(s.length - 4);
	}
	
	/**
	 * Serializes a KeyPair to the file specified in KEYPAIR_FILE
	 * @param context The current application context
	 * @param keypair The KeyPair that has to be serialized
	 */
	private void serializeKeypair(Context context, KeyPair keypair) 
	{	
		try { 
			File dir = context.getFilesDir();
			File file = new File(dir, KEYPAIR_FILE);
			
			if(file != null)
			{
				boolean deleted = file.delete();
				Log.d(LOG_CATEGORY, "Keypair file deleted: " + deleted);
			}
			
			FileOutputStream output = context.openFileOutput(KEYPAIR_FILE, Context.MODE_PRIVATE);

			ObjectOutput out = new ObjectOutputStream(output); 
			out.writeObject(keypair); 
			out.close(); 
	 
	    } catch(IOException ioe) { 
	    	Log.e(LOG_CATEGORY, "Error writing keypair to file: ", ioe); 
	    }
	
	}
	
	/**
	 * Read a serialized KeyPair from the filesystem
	 * @param context The current application context
	 * @return The "de"serialized KeyPair
	 */
	private KeyPair deserializeKeypair(Context context) {
		KeyPair keypair = null;
		try {
			FileInputStream input = context.openFileInput(KEYPAIR_FILE);
			ObjectInputStream in = new ObjectInputStream(input);
			
			keypair = (KeyPair) in.readObject();
		} catch (IOException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		} catch (ClassNotFoundException e) {
			Log.e(LOG_CATEGORY, e.getMessage());
		}
		
		return keypair;
	}
}
