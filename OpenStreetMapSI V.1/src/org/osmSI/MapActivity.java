// Created by plusminus on 00:23:14 - 03.10.2008
package org.osmSI;





import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmSI.ResourceProxyImpl;
import org.osmSI.OpenStreetMapConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;


public class MapActivity extends Activity implements OpenStreetMapConstants {

	// ===========================================================
	// Constants
	// ===========================================================

	
	
	private static final int MENU_ZOOMIN_ID = Menu.FIRST;
	private static final int MENU_ZOOMOUT_ID = MENU_ZOOMIN_ID + 1;
	private static final int MENU_UPLOAD_ID = MENU_ZOOMOUT_ID + 1;

	// ===========================================================
	// Fields
	// ===========================================================

	private MapView mOsmv;
	private SharedPreferences mPrefs;
	private ItemizedOverlay<OverlayItem> mMyLocationOverlay;
	MyLocationOverlay myLocationOverlay;
	private ResourceProxy mResourceProxy;
	 GeoPoint mygeopoint;

	// ===========================================================
	// Constructors
	// ===========================================================
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mResourceProxy = new ResourceProxyImpl(getApplicationContext());
		mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		final RelativeLayout rl = new RelativeLayout(this);

		CloudmadeUtil.retrieveCloudmadeKey(getApplicationContext());

		this.mOsmv = new MapView(this, 256,mResourceProxy);
		rl.addView(this.mOsmv, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
 				LayoutParams.FILL_PARENT));
		this.mOsmv.setMultiTouchControls(true);
		/* Itemized Overlay */
		{
			/* Create a static ItemizedOverlay showing a some Markers on some cities. */
			 ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
			SqlBaza markeri=new SqlBaza();
			markeri.GetValues();
			items=markeri.getMarks();
			 myLocationOverlay = new MyLocationOverlay(this.getBaseContext(), this.mOsmv,
					mResourceProxy);
			mOsmv.getOverlays().add(myLocationOverlay);


			/* OnTapListener for the Markers, shows a simple Toast. */
			this.mMyLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items,
					new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
						@Override
						public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
							
							Intent myIntent = new Intent(MapActivity.this, SlikaActivity.class);
							Bundle bundle = new Bundle();

							bundle.putString("key1", item.mDescription);
							myIntent.putExtras(bundle);
							myIntent.putExtra("url", item.mDescription);
							myIntent.putExtra("caption", item.mTitle);
							startActivityForResult(myIntent, 0);
							return true; // We 'handled' this event.
							
						}

						@Override
						public boolean onItemLongPress(final int index, final OverlayItem item) {
							Toast.makeText(
									MapActivity.this,
									"Item '" + item.mTitle + "' (index=" + index
											+ ") got long pressed", Toast.LENGTH_LONG).show();
							return false;
						}
					}, mResourceProxy);
			this.mOsmv.getOverlays().add(this.mMyLocationOverlay);
		}

		/* MiniMap */
		{
			MinimapOverlay miniMapOverlay = new MinimapOverlay(this,
					mOsmv.getTileRequestCompleteHandler());
			this.mOsmv.getOverlays().add(miniMapOverlay);
		}

		this.setContentView(rl);
		mOsmv.getController().setZoom(mPrefs.getInt(PREFS_ZOOM_LEVEL, 1));
		mOsmv.scrollTo(mPrefs.getInt(PREFS_SCROLL_X, 0), mPrefs.getInt(PREFS_SCROLL_Y, 0));
		
			}
	
	@Override
	protected void onPause() {
		if(myLocationOverlay.isCompassEnabled())
		  myLocationOverlay.disableCompass();
		if(myLocationOverlay.isMyLocationEnabled())
		myLocationOverlay.disableMyLocation();
		final SharedPreferences.Editor edit = mPrefs.edit();
		edit.putString(PREFS_TILE_SOURCE, mOsmv.getTileProvider().getTileSource().name());
		edit.putInt(PREFS_SCROLL_X, mOsmv.getScrollX());	
		edit.putInt(PREFS_SCROLL_Y, mOsmv.getScrollY());
		edit.putInt(PREFS_ZOOM_LEVEL, mOsmv.getZoomLevel());
		edit.commit();

		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		final String tileSourceName = mPrefs.getString(PREFS_TILE_SOURCE,
				TileSourceFactory.DEFAULT_TILE_SOURCE.name());
        myLocationOverlay.enableCompass();
        myLocationOverlay.enableMyLocation();
      
		try {
			final ITileSource tileSource = TileSourceFactory.getTileSource(tileSourceName);
			mOsmv.setTileSource(tileSource);
		} catch (final IllegalArgumentException ignore) {
		}

	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	
	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
		pMenu.add(0, MENU_ZOOMIN_ID, Menu.NONE, "ZoomIn");
		pMenu.add(0, MENU_ZOOMOUT_ID, Menu.NONE, "ZoomOut");
		pMenu.add(0, MENU_UPLOAD_ID, Menu.NONE, "Прикачи слика");

		return true;
	}

	@Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ZOOMIN_ID:
			this.mOsmv.getController().zoomIn();
			return true;

		case MENU_ZOOMOUT_ID:
			this.mOsmv.getController().zoomOut();
			return true;
		case MENU_UPLOAD_ID:
			Intent myIntent = new Intent(getBaseContext(), ImageUpload.class);
			Bundle bundle = new Bundle();
			myIntent.putExtras(bundle);
			try{
			  GeoPoint mygeopointa=new GeoPoint(myLocationOverlay.getMyLocation());
			  myIntent.putExtra("x",mygeopointa.getLatitudeE6());
			  myIntent.putExtra("y",mygeopointa.getLongitudeE6());
			}
			  catch(NullPointerException e){
				  myIntent.putExtra("x",0);
					myIntent.putExtra("y",0);
			  }
            startActivityForResult(myIntent, 0);
			return true;
		}
		return false;
	}	
}

