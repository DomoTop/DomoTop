package org.openremote.android.console.util;

import android.accounts.AccountManager;
import android.accounts.Account;
import android.content.Context;

/**
 * Retrieve information stored on the Android device
 * @author <a href="mailto:vincent.kriek@tass.nl">Vincent Kriek</a>
 */
public class PhoneInformationAccountManager extends PhoneInformation {
	
	/**
	 * Retrieves registered e-mail address from AccountManager, preferably the GMail address
	 * @param context The current application context
	 * @return e-mail address as a string
	 */
	@Override
	public String getEmailAddress(Context context) 
	{
    	AccountManager manager = AccountManager.get(context);
    	Account[] accounts = manager.getAccountsByType("com.google");
    	if(accounts.length <= 0) {
    		accounts = manager.getAccounts();
    	}
    	return accounts[0].name;
	}
	
}
