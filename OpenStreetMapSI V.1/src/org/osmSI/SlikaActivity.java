package org.osmSI;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class SlikaActivity extends Activity implements asyncListener{
	private ImageView imgView;
	private TextView txtView;
	private String url;
	private String caption;
	private ProgressDialog dialog;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imageviewer);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		imgView = (ImageView) findViewById(R.id.ImageViewer);
		txtView = (TextView) findViewById(R.id.textView1);
		Bundle extras = getIntent().getExtras(); 
         
		if (extras != null) {
             url = extras.getString("url");
             caption=extras.getString("caption");
         }
		
		try{
		dialog = ProgressDialog.show(SlikaActivity.this, "Сликата се симнува",
				"Ве молиме почекајте...", true);
		 getBitmapFromURL getBitmapFromURL=new getBitmapFromURL(url,dialog,this);
          getBitmapFromURL.execute();
	      
	  
		}
		catch(Exception e){
			Toast.makeText(getApplicationContext(), "Грешка при вчитување на сликата.",
					Toast.LENGTH_LONG).show();
		}
		
	}
	@Override
	public void onAsymcCallComplete(Bitmap bimage) {
		imgView.setImageBitmap(bimage);
        txtView.setText(caption);
        Toast.makeText(getApplicationContext(), caption,
				Toast.LENGTH_LONG).show();
		
	}
	@Override
	public void onAsyncCallCancel() {
		// TODO Auto-generated method stub
		
	}
	

}
	 class  getBitmapFromURL extends AsyncTask<Void , Void, Bitmap> {
		String src;
		ProgressDialog dialog;
		asyncListener listener =null;
		public getBitmapFromURL(String src,ProgressDialog dialog,asyncListener listener){	
			this.src=src;
			this.dialog=dialog;
			this.listener=listener;
		}
		
       
        
		@Override
		protected Bitmap doInBackground(Void... params) {
			 try {
		            Log.e("src",src);
		            URL url = new URL("http://trolaj.me/osmSI/uploads/"+src);
		            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		            connection.setDoInput(true);
		            connection.connect();
		            InputStream input = connection.getInputStream();
		            Bitmap myBitmap = BitmapFactory.decodeStream(input);
		            Log.e("Bitmap","returned");
		            return myBitmap;
		        } catch (IOException e) {
		            e.printStackTrace();
		            Log.e("Exception",e.getMessage());
			return null;
		}
    }
		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			if (dialog.isShowing())
					dialog.dismiss();
			listener.onAsymcCallComplete(result);
		}
	 }
	  interface asyncListener { 
		 
		  public void onAsymcCallComplete(Bitmap bimage); 
		 
		  public void onAsyncCallCancel();
		 
		}
