package org.openremote.android.console.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;
import android.os.Build;
import android.util.Log;

/**
 * Retrieve information stored on the Android device
 * @author <a href="mailto:vincent.kriek@tass.nl">Vincent Kriek</a>
 */
public abstract class PhoneInformation {
	private static PhoneInformation instance;
	
	/**
	 * Returns the name of the current device name
	 * @return The device name as a String
	 */
	public String getDeviceName()
	{
		return android.os.Build.MODEL;
	}
	
	/**
	 * Returns the name of the current device name as an url encoded string
	 * @return The device name as an url encoded String
	 */
	public String getUrlEncodedDeviceName()
	{
		try {
			return URLEncoder.encode(getDeviceName(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.e("OpenRemote/PhoneInformation", e.getMessage());
			return "";
		}
	}
	
	/**
	 * Return the email address of the registered user. 
	 * @param context The current application context
	 * @return The e-mail address
	 */
	public abstract String getEmailAddress(Context context);
	
	/**
	 * Return a PhoneInformation instance based on Android version. Used because Android supports the
	 * AccountManager from API version 5 and up
	 * @return A PhoneInformation implementation based on the Android version, either PhoneInformationAccountManager or PhoneInformationBase
	 */
	public static PhoneInformation getInstance() 
	{
		if (instance == null) {
            String className;

            /*
             * Check the version of the SDK we are running on. Choose an
             * implementation class designed for that version of the SDK.
             *
             * Unfortunately we have to use strings to represent the class
             * names. If we used the conventional ContactAccessorSdk5.class.getName()
             * syntax, we would get a ClassNotFoundException at runtime on pre-Eclair SDKs.
             * Using the above syntax would force Dalvik to load the class and try to
             * resolve references to all other classes it uses. Since the pre-Eclair
             * does not have those classes, the loading of ContactAccessorSdk5 would fail.
             */
            int sdkVersion = Integer.parseInt(Build.VERSION.SDK);       // Cupcake style
            if (sdkVersion < 5) {
                className = "org.openremote.android.console.util.PhoneInformationBase";
            } else {
            	className = "org.openremote.android.console.util.PhoneInformationAccountManager";
            }

            /*
             * Find the required class by name and instantiate it.
             */
            try {
                Class<? extends PhoneInformation> clazz =
                        Class.forName(className).asSubclass(PhoneInformation.class);
                instance = clazz.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        return instance;

	}
	

}
