package org.osmSI;


import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageUpload extends Activity {
	private static final int PICK_IMAGE = 1;
	private static final int TAKE_PICTURE = 2;
	private ImageView imgView;
	private Button upload;
	private EditText caption;
	private Bitmap bitmap;
	private ProgressDialog dialog;
	private Uri imageUri;
	String filePath = null;
	int x,y;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imageupload);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		imgView = (ImageView) findViewById(R.id.ImageView);
		upload = (Button) findViewById(R.id.Upload);
		caption = (EditText) findViewById(R.id.Caption);
		Bundle extras = getIntent().getExtras(); 
        
		if (extras != null) {
			x = extras.getInt("x");
			y = extras.getInt("y");
         }
		upload.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (bitmap == null) {
					Toast.makeText(getApplicationContext(),
							"Одберете слика",  Toast.LENGTH_SHORT).show();
					
					}
					else if (caption.getText().toString().length() == 0) {
						Toast.makeText(getApplicationContext(),
								"Внесете опис за локацијата", Toast.LENGTH_SHORT).show();
				} else {
					dialog = ProgressDialog.show(ImageUpload.this, "Сликата се прикачува",
							"Ве молиме почекајте...", true);			
					Context context;
					context=getApplicationContext();
					Upload upload = new Upload(filePath,x,y,caption.getText().toString(),context,dialog);
					upload.execute();
				}
				if ((bitmap != null)&&(caption.getText().toString().length() != 0))
					Toast.makeText(getApplicationContext(),
							"Сликата е успешно прикачена", Toast.LENGTH_SHORT).show();
					finish();
			}
			
		});
		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.imageupload_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.ic_menu_gallery:
			try {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Одбери слика"),
						PICK_IMAGE);
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(),
						e.getMessage(),
						Toast.LENGTH_LONG).show();
				Log.e(e.getClass().getName(), e.getMessage(), e);
			}
			return true;
		case R.id.ic_menu_camera:
			try {
				Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
				 int time = (int) (System.currentTimeMillis());
	                time = time*-1;
				File photo = new File(Environment.getExternalStorageDirectory(),  + time +".jpg");
			    intent.putExtra(MediaStore.EXTRA_OUTPUT,
			            Uri.fromFile(photo));
			   
			    imageUri = Uri.fromFile(photo);
			    startActivityForResult(intent, TAKE_PICTURE);
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(),
						e.getMessage(),
						Toast.LENGTH_LONG).show();
				Log.e(e.getClass().getName(), e.getMessage(), e);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case PICK_IMAGE:
			if (resultCode == Activity.RESULT_OK) {
Uri selectedImageUri = data.getData();
				

				try {
					// OI FILE Manager
					String filemanagerstring = selectedImageUri.getPath();

					// MEDIA GALLERY
					String selectedImagePath = getPath(selectedImageUri);

					if (selectedImagePath != null) {
						filePath = selectedImagePath;
					} else if (filemanagerstring != null) {
						filePath = filemanagerstring;
					} else {
						Toast.makeText(getApplicationContext(), "Unknown path",
								Toast.LENGTH_LONG).show();
						Log.e("Bitmap", "Unknown path");
					}

					if (filePath != null) {
						decodeFile(filePath);
					} else {
						bitmap = null;
					}
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "Internal error",
							Toast.LENGTH_LONG).show();
					Log.e(e.getClass().getName(), e.getMessage(), e);
				}

			}
			break;
		case TAKE_PICTURE:
	        if (resultCode == Activity.RESULT_OK) {
	            Uri selectedImage = imageUri;
	            String filemanagerstring = selectedImage.getPath();
	            try {
	            String selectedImagePath = getPath(selectedImage);
	            if (selectedImagePath != null) {
					filePath = selectedImagePath;
				} else if (filemanagerstring != null) {
					filePath = filemanagerstring;
				} else {
					Toast.makeText(getApplicationContext(), "Непозната патека",
							Toast.LENGTH_LONG).show();
					Log.e("Bitmap", "Грешка во патеката");
				}

				if (filePath != null) {
					decodeFile(filePath);
				} else {
					bitmap = null;
				}

	            } catch (Exception e) {
	                Toast.makeText(this, "Грешка при вчитување", Toast.LENGTH_SHORT)
	                        .show();
	                Log.e("Camera", e.toString());
	            }
			 }
	        break;
		default:
		}
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			// HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			// THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}

	public void decodeFile(String filePath) {
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, o);

		// The new size we want to scale to
		final int REQUIRED_SIZE = 1024;

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		bitmap = BitmapFactory.decodeFile(filePath, o2);

		imgView.setImageBitmap(bitmap);

	}
}
	
