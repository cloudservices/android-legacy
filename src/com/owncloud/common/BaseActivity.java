package com.owncloud.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.view.KeyEvent;
import android.widget.Button;

import com.owncloud.login.LoginActivity;

public class BaseActivity extends Activity {
	private ProgressDialog progressDialog;
	protected Thread t;
	
	public static final String PREFS_NAME = "MyPrefsFile";
	public  static final String PREF_USERNAME = "username";
	public static final String PREF_PASSWORD = "password";
	public static final String PREF_SERVERURL = "serverurl";
	public static final String PREF_BASEURL = "baseUrl";
	public static final String PREF_UNLINK = "false";
	
	public static final String PREF_PASS_TXT_FIRST = "passtextfirst";
	public static final String PREF_PASS_TXT_SECOND = "passtextsecond";
	public static final String PREF_PASS_TXT_THIRD = "passtextthird";
	public static final String PREF_PASS_TXT_FOUR = "passtextfour";
	
	public static final String PREF_PASSCODE = "pass";
	
	public static HttpClient httpClient;
	public static SharedPreferences pref;
	
	public static String url;
	public static String baseUrl;
	public static String mainUrl;
	public static String fileLocation="";
	
	public static String mDownloadDest = Environment.getExternalStorageDirectory() + "/ownCloud";
	public static Button mOwnCloud;
	
	public Bitmap bitmap; 
	public boolean mUploadFlag = false;
	public boolean mUploadCnt = true;
	
	public static List<String> mListFile = new ArrayList<String>();
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		 pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		finish();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {

		case 0: {
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage("Loading . . .");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			return dialog;
		}case 1:{
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage("Uploading files . . .");
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			return dialog;
		}

		default:
			return null;
		}
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			// Try This
			int i = netInfo.getType();
			System.out.println("Net Type =" + i);

			return true;
		}
		return false;

	}

	public void WebNetworkAlert() {
		
		new AlertDialog.Builder(this).setTitle("Network Error")
				.setMessage("Internet connection not available.")
				.setPositiveButton("Ok", new OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						// do stuff onclick of YES
						startActivity(new Intent(getApplicationContext(),LoginActivity.class));
						finish();
						return;
					}
				}).show();
	}

	public boolean serverCheck(String serverUrl) {
		boolean isOK = false;
		try {
		    URL url = new URL(serverUrl);
		    HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
		    urlcon.connect();
		    if (urlcon.getResponseCode() == 200) {
		            InputStream in = new BufferedInputStream(urlcon.getInputStream());
		            String serverStatus = readStream(in); //assuming that "http://yourserverurl/yourstatusmethod" returns OK or ERROR depending on your server status check         
		            isOK = (serverStatus.equalsIgnoreCase("OK"));
		    }else{
		      isOK = false;
		    }

		    urlcon.disconnect();

		} catch (MalformedURLException e1) {
		            isOK = false;
		            e1.printStackTrace();
		} catch (IOException e) {
		            isOK = false;
		            e.printStackTrace();
		}
		return isOK;
	}
	
	public static String readStream (InputStream in) throws IOException {
	    StringBuffer out = new StringBuffer();
	    byte[] b = new byte[4096];
	    for (int n; (n = in.read(b)) != -1;) {
	        out.append(new String(b, 0, n));
	    }
	    return out.toString();
	}
	
	public void webServerAlert() {
		new AlertDialog.Builder(this).setTitle("Server Error")
				.setMessage("There is a problem connecting to the server.  Please try again later.")
				.setPositiveButton("Ok", new OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						// do stuff onclick of YES
						getSharedPreferences(PREFS_NAME,
								MODE_PRIVATE)
								.edit()
								.putString(PREF_USERNAME, null)
								.putString(PREF_PASSWORD, null)
								.putString(PREF_SERVERURL, null)
								.putString(PREF_BASEURL, null)
								.putString(PREF_UNLINK, null)
								.commit();
						startActivity(new Intent(getApplicationContext(),LoginActivity.class));	
						finish();
						return;
					}
				}).show();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			
			finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	public void mailToFriend(String message) {

		final Intent emailIntent = new Intent(
				android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				"");
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				"ownCloud App ");
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,Html.fromHtml(message));

		startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	}
	
	public String getRealPathFromURI(Uri contentUri) {
      
		if(contentUri.toString().contains("video")){

			String[] proj = { MediaStore.Video.Media.DATA };
	        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
	        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
	        cursor.moveToFirst();
	        return cursor.getString(column_index);
		}else{
		
		String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
		}
    }
	 public static boolean trimCache(Context context) 
	 {
	        try {
	           File dir = context.getCacheDir();
	           if (dir != null && dir.isDirectory()) 
	           {
	              return (deleteDir(dir));
	           }
	        } catch (Exception e) {
	           return false;
	        }
	        return false;
	 }

	 private static boolean deleteDir(File dir) 
	 {
	        if (dir != null && dir.isDirectory()) 
	        {
	           String[] children = dir.list();
	           for (int i = 0; i < children.length; i++) 
	           {
	              boolean success = deleteDir(new File(dir, children[i]));
	              if (!success) 
	              {
	                 return false;
	              }
	           }
	        }

	        // The directory is now empty so delete it
	        return dir.delete();
	 }

	 
}
