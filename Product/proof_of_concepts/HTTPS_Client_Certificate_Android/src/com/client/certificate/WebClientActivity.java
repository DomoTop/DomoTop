package com.client.certificate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

public class WebClientActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        TextView text = new TextView(this);
       
        
        /*
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("id", "12345"));
        nameValuePairs.add(new BasicNameValuePair("testnamae", "melroy"));
         */            
        
        
        //this.getCertificate();
        //this.openConnection();
        
//        try {
//        	text.setText("Succes:\n");
//        	text.append(GenCert.generateCertificate(this));
//        } catch (Exception e) {
//        	text.setText("Failure:\n");
//
//        	text.append(e.getClass().toString() + "\n");
//        	text.append(e.getMessage() + "\n");
//        }
        
        try {
        	KeyStore ks = GenCert.getData(this);
        	
        	openConnection();
		} catch (Exception e) {
			text.setText(e.getClass().getName());
		}
        
        setContentView(text);
    }
    

    public void getCertificate()
    {
    	String certificateContent = "";
    	
		try 
		{			
			certificateContent = AndroidHttpClient.executeHttpGet("https://192.168.1.1/controller/rest/cert/create/");
		} 
		catch (Exception e)
		{
			Log.e("client", "Exception while getting certificate: " + e.getMessage());
		}
		Log.i("client", "Certificate Base64: " + certificateContent);
		
		
		// Saving the certificate to the internal storage
		try 
		{
			this.saveFile("client_certificate.bks", Base64.decode(certificateContent, Base64.DEFAULT));
		} 
		catch(IllegalArgumentException e)
		{
			Log.e("client", "Cert error: " + e.getMessage());
		}
		catch (IOException e)
		{
			Log.e("client", "Cert error: " + e.getMessage());
		}
		Log.i("client", "File certificate done");
    }
    
    public void openConnection()
    {
		String response = "";
		try 
		{
			response = AndroidHttpClientCertificate.executeHttpGet(this.getApplicationContext(), "https://192.168.1.1");
		} 
		catch (Exception e) 
		{
			Log.e("client", "Open Connection exception: " + e.getMessage());
		}
		
		Log.i("client", "Response connection: " + response);
    }
    
	private void saveFile(String fileName, byte[]  fileContent) throws IOException
	{
		File dir = getFilesDir();
		File file = new File(dir, fileName);
		
		if(file != null)
		{
			boolean deleted = file.delete();
			Log.d("client", "Cert file deleted:" + deleted);
		}
		
		FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
		fos.write(fileContent);
		fos.close();
	}
}