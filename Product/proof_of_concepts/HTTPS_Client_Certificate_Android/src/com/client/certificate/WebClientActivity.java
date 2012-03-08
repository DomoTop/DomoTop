package com.client.certificate;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class WebClientActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.testPost();
    }
    
    public void testPost()
    {    
    	
    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("id", "12345"));
        nameValuePairs.add(new BasicNameValuePair("testnamae", "melroy"));
    	
    	String response = "";
    	
		try {
			response = AndroidHttpClient.executeHttpGet("https://192.168.1.1");
		} catch (Exception e) {
			Log.e("client", "Exception #1: " + e.getMessage());
		}
		Log.i("client", "Response #1: " + response);

		String response2 = "";
		try {
			response2 = AndroidHttpClientCertificate.executeHttpGet(this.getApplicationContext(), "https://192.168.1.1");
		} catch (Exception e) {
			Log.e("client", "Exception #2: " + e.getMessage());
		}
		Log.i("client", "Response #2: " + response2);
    }    
}