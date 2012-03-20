package org.openremote.android.console.util;

import android.accounts.AccountManager;
import android.accounts.Account;
import android.content.Context;


public class PhoneInformationAccountManager extends PhoneInformation {
	
	/**
	 * Retrieves registered e-mail address from AccountManager
	 * @param context The current application context
	 * @return e-mail address as a string
	 */
	@Override
	public String getEmailAddress(Context context) 
	{
    	AccountManager manager = AccountManager.get(context);
    	Account acc = manager.getAccounts()[0];
    	return acc.name;
	}
	
}
