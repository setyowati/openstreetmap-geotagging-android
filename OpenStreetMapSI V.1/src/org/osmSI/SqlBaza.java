package org.osmSI;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import android.util.Log;

public class SqlBaza {
	ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
	void GetValues(){
	String result = "";
	 InputStream is=null;

	try{
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpPost httppost = new HttpPost("http://trolaj.me/osmSI/getMarkers.php");
	       
	        HttpResponse response = httpclient.execute(httppost);
	        HttpEntity entity = response.getEntity();
	       is = entity.getContent();
	}catch(Exception e){
	        Log.e("log_tag", "Грешка при конекција со http "+e.toString());
	}
	//convert response to string
	try{
		
	        BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
	        StringBuilder sb = new StringBuilder();
	        String line = null;
	        while ((line = reader.readLine()) != null) {
	                sb.append(line + "\n");
	        }
	        is.close();
	 
	        result=sb.toString();
	}catch(Exception e){
	        Log.e("log_tag", "Error converting result "+e.toString());
	}
	 
	//parse json data
	try{
	        JSONArray jArray = new JSONArray(result);
	        for(int i=0;i<jArray.length();i++){
	                JSONObject json_data = jArray.getJSONObject(i);
	                
	                items.add(new OverlayItem(json_data.getString("opis"),
	                		json_data.getString("adresa"), new GeoPoint(+json_data.getInt("x"),
	                				+json_data.getInt("y")))); 
	                
	                Log.i("log_tag","id: "+json_data.getInt("id")+
	                		", opis: "+json_data.getString("opis")+
	                		", adresa: "+json_data.getString("adresa")+
	                        ", x: "+json_data.getInt("x")+
	                        ", y: "+json_data.getInt("y")
	                );
	        }
	}
	catch(JSONException e){
	        Log.e("log_tag", "Error parsing data "+e.toString());
	}
	
}
	ArrayList<OverlayItem> getMarks(){
		return items;
	}
}


