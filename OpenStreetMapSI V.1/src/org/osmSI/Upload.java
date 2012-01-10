package org.osmSI;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class Upload extends AsyncTask<Void , Void, String>  {
	private String filePath,caption;
	private int x,y;
	Context context;
	ProgressDialog dialog;
	public int status;
	 public Upload(String filePath,int x,int y,String caption,Context context,ProgressDialog dialog)
     {
		 this.filePath = filePath;
		 this.caption = caption;
         this.x = x;
         this.y = y;
         this.context=context;
         this.dialog=dialog;
     }
	@Override
	protected String doInBackground(Void... unused) {
		//Upload(String filePath,int x,int y,String caption) {
			HttpURLConnection conn = null;
			DataOutputStream dos = null;
			DataInputStream inStream = null;
			String lineEnd = "\r\n";
			String twoHyphens = "--";
			String boundary = "*****";
			int bytesRead, bytesAvailable, bufferSize;
			byte[] buffer;
			int maxBufferSize = 1 * 1024 * 1024;
			String urlString = "http://trolaj.me/osmSI/upload.php";
			int ii;
			for( ii =filePath.length()-1;ii>0;ii--){
				if(filePath.charAt(ii)=='/'){
				break;
				
				}
			}
			String imgName=new String();
			for(int j =ii+1;j<filePath.length();j++){
				imgName+=filePath.charAt(j);
				Log.e("Debug", imgName);
				}
			
			try {
				// ------------------ CLIENT REQUEST
				FileInputStream fileInputStream = new FileInputStream(new File(
						filePath));
				// open a URL connection to the Servlet
				URL url = new URL(urlString);
				// Open a HTTP connection to the URL
				conn = (HttpURLConnection) url.openConnection();
				// Allow Inputs
				conn.setDoInput(true);
				// Allow Outputs
				conn.setDoOutput(true);
				// Don't use a cached copy.
				conn.setUseCaches(false);
				// Use a post method.
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("Content-Type",
						"multipart/form-data;boundary=" + boundary);
				dos = new DataOutputStream(conn.getOutputStream());
				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\"" + imgName + "\"" + lineEnd);
				dos.writeBytes(lineEnd);
				// create a buffer of maximum size
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];
				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				while (bytesRead > 0) {
					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}
				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
				// close streams
				Log.e("Debug", "Сликата е прикачена");
				fileInputStream.close();
				dos.flush();
				dos.close();
			} catch (MalformedURLException ex) {
				Log.e("Debug", "error: " + ex.getMessage(), ex);
			} catch (IOException ioe) {
				Log.e("Debug", "error: " + ioe.getMessage(), ioe);
			}
			// ------------------ read the SERVER RESPONSE
			try {
				inStream = new DataInputStream(conn.getInputStream());
				String str;

				while ((str = inStream.readLine()) != null) {
					Log.e("Debug", "Одговор од серверот " + str);
				}
				inStream.close();

			} catch (IOException ioex) {
				Log.e("Debug", "грешка: " + ioex.getMessage(), ioex);
			}
			
			////za baza////////////////////////////////////////////////////////////////////
			
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			String aString = Integer.toString(x);
			String bString = Integer.toString(y);
			nameValuePairs.add(new BasicNameValuePair("x",aString));
			nameValuePairs.add(new BasicNameValuePair("y",bString));
			nameValuePairs.add(new BasicNameValuePair("opis",caption));
			nameValuePairs.add(new BasicNameValuePair("adresa",imgName));
					
					
			String result=null;
			 InputStream is=null;
			try {     
			HttpClient httpclient = new DefaultHttpClient();

		     HttpPost httppost = new HttpPost("http://www.trolaj.me/osmSI/baza.php");
		     httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		     HttpResponse response = httpclient.execute(httppost);
		     HttpEntity entity = response.getEntity();
		     is = entity.getContent();
		     }
			catch(Exception e){
		         Log.e("log_tag", "Грешка при конекција со http "+e.toString());
		    }
		//convert response to stringtry
		try{
		      BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
		      StringBuilder sb = new StringBuilder();
		       sb.append(reader.readLine() + "\n");
		       String line="0";
		       while ((line = reader.readLine()) != null) {
		                      sb.append(line + "\n");        }
		        is.close();
		         result=sb.toString();
		        }
		catch(Exception e){
		              Log.e("log_tag", "Error converting result "+e.toString());
		        }//paring datatry
		try {
			 JSONArray jArray = new JSONArray(result);
		      @SuppressWarnings("unused")
			JSONObject json_data=null;
		      for(int i=0;i<jArray.length();i++){
		             json_data = jArray.getJSONObject(i);
		                      }
	}      
		catch(JSONException e1){
		    	       } catch (ParseException e1) {
					e1.printStackTrace();	}
		return null;
	}
	@Override
	protected void onPostExecute(String sResponse) {
		try {
			if (dialog.isShowing())
				dialog.dismiss();

			if (sResponse != null) {
				JSONObject JResponse = new JSONObject(sResponse);
				int success = JResponse.getInt("SUCCESS");
				status=success;
				String message = JResponse.getString("MESSAGE");
				if (success == 0) {
					Toast.makeText(context, message,
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(context,
							"Photo uploaded successfully",
							Toast.LENGTH_SHORT).show();
					
				}
			}
		} catch (Exception e) {
			Toast.makeText(context,
					e.getMessage(),
					Toast.LENGTH_LONG).show();
			Log.e(e.getClass().getName(), e.getMessage(), e);
		}
	}
}
	

	

