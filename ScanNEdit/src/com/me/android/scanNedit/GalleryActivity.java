package com.me.android.scanNedit;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlSerializer;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.markupartist.android.widget.ActionBar_three;
import com.markupartist.android.widget.ActionBar_two;
import com.markupartist.android.widget.ActionBar_two.Action_two;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class GalleryActivity extends Activity {
	
	private Uri mImageCaptureUri;
	private ImageView mImageView;	
	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;
	public static int count=1;
	public static String textPath=null;
	public static String date=null;
	public static String time=null;
	Bitmap cropped_bitmap = null;
	Bitmap changed_bitmap = null;
	int event_counter = 0;
	public static String Croped_PATH = Environment.getExternalStorageDirectory().getPath() + "/ScanNEdit/Croped_Pictures";
	public static String Picture_PATH = Environment.getExternalStorageDirectory().getPath() + "/ScanNEdit/Pictures";
	public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/ScanNEdit/";
	public static String Picture_uri=null;	//for original selected picture
	public static String Croped_uri=null;		//for croped selected picture
	public static String Picture_Name=null;	//for orignal selected picture	
	public String textFileName = "";
	public static final String lang = "eng";
	private static final String TAG = "GalleryActivity.java";
	public String recognizedText = "";
	
	
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.gallery);        
        
        //activity's upper action bar including its actions        
        final ActionBar_three actionBar_upp = (ActionBar_three) findViewById(R.id.actionbar5);       
        actionBar_upp.setTitle_three("Cropped Image");            
        
        //activity's lower action bar including its actions        
        final ActionBar_two actionBar_low = (ActionBar_two) findViewById(R.id.actionbar6);               
        actionBar_low.addAction_two(new SaveAction());
        actionBar_low.addAction_two(new ScanAction());        
        actionBar_low.addAction_two(new BrightnessAction());
        actionBar_low.addAction_two(new BackAction());          
        
        AlertDialog.Builder builder	= new AlertDialog.Builder(this);		
		
		//pick from file
		Intent intent = new Intent();					
	    intent.setType("image/*");
	    intent.setAction(Intent.ACTION_GET_CONTENT);	                
	    startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);
					
		final AlertDialog dialog = builder.create();				
		mImageView	= (ImageView) findViewById(R.id.iv_photo);	
		
		//Brightness Seek Bar start
        SeekBar seekbarbrightness=(SeekBar)findViewById(R.id.seekBar1); 

        seekbarbrightness.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                public void onStopTrackingTouch(SeekBar arg0) {
                 // TODO Auto-generated method stub
                	
                }

                public void onStartTrackingTouch(SeekBar arg0) {
                 // TODO Auto-generated method stub

                }

                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {

                    // TODO Auto-generated method stub
                	BitmapFactory.Options options = new BitmapFactory.Options();
            		options.inPreferredConfig = Bitmap.Config.ARGB_8888;    
            		changed_bitmap = BitmapFactory.decodeFile(Croped_PATH + File.separator + Picture_Name + ".jpg", options);
            		TextView seekbar_prog = (TextView)findViewById(R.id.textView3);
            		seekbar_prog.setText(Integer.toString(progress));
            		changed_bitmap = doBrightness(changed_bitmap, progress);
            		mImageView.setImageBitmap(changed_bitmap);            		
                }                
               });
        			
        //Brightness Seek Bar end 
        			
    }
    //onCreate end
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode != RESULT_OK) return;
	   
	    switch (requestCode) {
		    case PICK_FROM_CAMERA:
		    	
		    	doCrop();		    	
		    	break;
		    	
		    case PICK_FROM_FILE: 
		    	
		    	mImageCaptureUri = data.getData	();		    	
		    	Picture_uri = getRealPathFromURI(mImageCaptureUri);
		    	time = new SimpleDateFormat("HHmmss").format(new Date());
				date = new SimpleDateFormat("ddMMyyyy").format(new Date());
		    	Picture_Name = "Selected_" + date + "_" + time; 
		    	Bitmap bitmap = BitmapFactory.decodeFile(Picture_uri);
		    	
		    	//saving a selected image to a Pictures folder
		    	saveImage(bitmap, Picture_Name, Picture_PATH);		    	
			
		    	doCrop();	    
		    	break;	    	
	    
		    case CROP_FROM_CAMERA:	    	
		        Bundle extras = data.getExtras();
	
		        if (extras != null) {	        	
		            Bitmap photo = extras.getParcelable("data");
		            cropped_bitmap = photo;
		            		            
		          //saving a selected croped image to a Croped Pictures folder
		            saveImage(photo, Picture_Name, Croped_PATH);
		            
		            mImageView.setImageBitmap(photo);
		        }		
		        break;
	    }
	}
    
    private String getRealPathFromURI(Uri contentUri) {
        int columnIndex = 0;

        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);

        try {
            columnIndex = cursor.getColumnIndexOrThrow
                           (MediaStore.Images.Media.DATA);
        } catch (Exception e) {
            Toast.makeText(GalleryActivity.this, "Exception in getRealPathFromURI",
                           Toast.LENGTH_SHORT).show();
            finish();  
            return null;
        }       
        cursor.moveToFirst();
        return cursor.getString(columnIndex);               
    }
    
    
    private void doCrop() {
		
    	final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();    	
    	Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        
        List<ResolveInfo> list = getPackageManager().queryIntentActivities( intent, 0 );
        int size = list.size();
        
        if (size == 0) {	
        	
        	Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();        	
            return;           
        } 
        
        else {
        	
        	intent.setData(mImageCaptureUri);            
            intent.putExtra("outputX", 380);
            intent.putExtra("outputY", 380);
            intent.putExtra("aspectX", 0);
            intent.putExtra("aspectY", 0);
            intent.putExtra("crop", true);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);
            
        	if (size == 1) {
        		Intent i 		= new Intent(intent);        		
	        	ResolveInfo res	= list.get(0);	        	
	        	i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));	        	
	        	startActivityForResult(i, CROP_FROM_CAMERA);
        	} 
        	else {
		        for (ResolveInfo res : list) {
		        	final CropOption co = new CropOption();
		        	
		        	co.title 	= getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
		        	co.icon		= getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
		        	co.appIntent= new Intent(intent);		        	
		        	co.appIntent.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));		        	
		            cropOptions.add(co);
		        }
	        
		        CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(), cropOptions);		        
		        AlertDialog.Builder builder = new AlertDialog.Builder(this);
		        builder.setTitle("Choose Crop App");
		        builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
		            public void onClick( DialogInterface dialog, int item ) {
		                startActivityForResult( cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
		            }
		        });
	        
		        builder.setOnCancelListener( new DialogInterface.OnCancelListener() {
		            @Override
		            public void onCancel( DialogInterface dialog ) {
		               
		                if (mImageCaptureUri != null ) {
		                    getContentResolver().delete(mImageCaptureUri, null, null );
		                    mImageCaptureUri = null;
		                }
		            }
		        } );
		        
		        AlertDialog alert = builder.create();		        
		        alert.show();
        	}
        }
	}

    private class SaveAction implements Action_two {

      	@Override
    	public int getDrawable_two() {
    		// TODO Auto-generated method stub
    		return R.drawable.ic_action_save;
    	}

    	@Override
    	public void performAction_two(View view) {
    		// TODO Auto-generated method stub
    		if(changed_bitmap != null)
    		{
    		saveImage(changed_bitmap, Picture_Name, Croped_PATH);	//saved the last changed croped image
    		}
    		
    		File myFile = new File(Environment.getExternalStorageDirectory().getPath() + "/ScanNEdit/Notes/camera.xml");
    		String data="";
    		String buffer="";
    		
    		if(myFile.exists()==false)
    		{
    			try {
    				myFile.createNewFile();
    				FileOutputStream fOut = new FileOutputStream(myFile);
    				OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
    				
    				//initialization
    				EditText title = (EditText)findViewById(R.id.editText1);
        			EditText desc = (EditText)findViewById(R.id.editText2);        			
        			String date1 = date.substring(0, 2);
        			String date2 = date.substring(2, 4);		
        			String date3 = date.substring(4, 8);        			
        			Picture pict=new Picture();
        			
        			pict.setId(count);
        			pict.setTitle(title.getText().toString());
        			pict.setDesc(desc.getText().toString());
        			pict.setDate(date1 + "/" + date2 + "/" + date3);
        			pict.setUrl(Picture_PATH + "/" + Picture_Name + ".jpg");						//Picture_Path instead of Croped_Path
        			data = writeFirstinXML(pict); 	
    				myOutWriter.append(data);			
    				myOutWriter.close();
    				fOut.close();
    				
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    					
    		}
    		
    		else{
    			try {
    			
    			//read xml to string	
    			FileInputStream fIn = new FileInputStream(myFile);
    			BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
    			while ((data = myReader.readLine()) != null) {
                buffer += data + "\n";
            }		
    			myReader.close();
    			
    			//initialization
    			EditText title = (EditText)findViewById(R.id.editText1);
    			EditText desc = (EditText)findViewById(R.id.editText2);
    			
    			String date1 = date.substring(0, 2);
    			String date2 = date.substring(2, 4);		
    			String date3 = date.substring(4, 8);
    			    			
    			Picture pic=new Picture();
    			pic.setId(count);
    			pic.setTitle(title.getText().toString());
    			pic.setDesc(desc.getText().toString());
    			pic.setDate(date1 + "/" + date2 + "/" + date3);
    			pic.setUrl(Picture_PATH + "/" + Picture_Name + ".jpg");							//Picture_Path instead of Croped_Path
    			data = writeSecondinXML(pic);
    			int length = buffer.length();
    			buffer = buffer.substring(0, length-10);
    			
    			FileOutputStream fOut = new FileOutputStream(myFile);
    			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
    			myOutWriter.append(buffer+data+"</Camera>");			
    			myOutWriter.close();
    			fOut.close();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    							
    		}
    		count++;
    		Toast.makeText(GalleryActivity.this,
                    "Picture Saved", Toast.LENGTH_LONG).show();
    		
    		    		
    	}

    }
    
    private class ScanAction implements Action_two {

      	@Override
    	public int getDrawable_two() {
    		// TODO Auto-generated method stub
    		return R.drawable.ic_action_scan;
    	}

    	@Override
    	public void performAction_two(View view) {
    		// TODO Auto-generated method stub    		
    	
    		final AlertDialog.Builder alert = new AlertDialog.Builder(GalleryActivity.this);
    		
    		if(changed_bitmap != null)
    		{
    		saveImage(changed_bitmap, Picture_Name, Croped_PATH);	//saved the last changed croped image	
    		}
    		
    		//input text file name dialog	
        	alert.setTitle("Saving..");
        	alert.setMessage("Please enter Text file name without (.txt)");
        	
        	// Set an EditText view to get user input 
        	final EditText input = new EditText(GalleryActivity.this);
        	alert.setView(input);

        	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int whichButton) {
        		
        		textFileName = input.getText().toString();
        		
        		//start of progress dialog
        		final ProgressDialog myPd_ring=ProgressDialog.show(GalleryActivity.this, "Please wait", "Scanning...", true);
        		
        		myPd_ring.setCancelable(true);
                new Thread(new Runnable() {  
                      @Override
                      public void run() {
                            // TODO Auto-generated method stub
                            try
                            {
                                  Thread.sleep(3000);
                            	
         			BitmapFactory.Options options = new BitmapFactory.Options();
         			options.inSampleSize = 4;
         			Bitmap bitmap = BitmapFactory.decodeFile(Picture_PATH + "/" + Picture_Name + ".jpg" , options);		//Picture_Path instead of Croped_Path
         		
         			try {
         				ExifInterface exif = new ExifInterface(Picture_PATH + "/" + Picture_Name + ".jpg");				//Picture_Path instead of Croped_Path
         				int exifOrientation = exif.getAttributeInt(
         						ExifInterface.TAG_ORIENTATION,
         						ExifInterface.ORIENTATION_NORMAL);

         				Log.v(TAG, "Orient: " + exifOrientation);

         				int rotate = 0;

         				switch (exifOrientation) {
         				case ExifInterface.ORIENTATION_ROTATE_90:
         					rotate = 90;
         					break;
         				case ExifInterface.ORIENTATION_ROTATE_180:
         					rotate = 180;
         					break;
         				case ExifInterface.ORIENTATION_ROTATE_270:
         					rotate = 270;
         					break;
         				}

         				Log.v(TAG, "Rotation: " + rotate);

         				if (rotate != 0) {

         					// Getting width & height of the given image.
         					int w = bitmap.getWidth();
         					int h = bitmap.getHeight();

         					// Setting pre rotate
         					Matrix mtx = new Matrix();
         					mtx.preRotate(rotate);

         					// Rotating Bitmap
         					bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
         				}

         				// Convert to ARGB_8888, required by tess
         				bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

         			} catch (IOException e) {
         				Log.e(TAG, "Couldn't correct orientation: " + e.toString());
         			}

         			// _image.setImageBitmap( bitmap );
         			
         			Log.v(TAG, "Before baseApi");

         			TessBaseAPI baseApi = new TessBaseAPI();
         			baseApi.setDebug(true);
         			baseApi.init(DATA_PATH + "/Tesseract", lang);
         			baseApi.setImage(bitmap);
         			
         			recognizedText = baseApi.getUTF8Text();
         			
         			baseApi.end();

         			// You now have the text in recognizedText var, you can do anything with it.
         			// We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
         			// so that garbage doesn't make it to the display.

         			Log.v(TAG, "OCRED TEXT: " + recognizedText);

         			if ( lang.equalsIgnoreCase("eng") ) {
         				recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
         			}
         			
         			recognizedText = recognizedText.trim(); 
         			
         			generateNoteOnSD(textFileName + ".txt", recognizedText); 			
         			// Cycle done.
      
                
                            }catch(Exception e){}
                            myPd_ring.dismiss();
                              
                      }
                }).start();   
                
        	  }
        	});

        	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	  public void onClick(DialogInterface dialog, int whichButton) {
        	    // Canceled.    		  
        	  }
        	});

        	alert.show();
		     	
	}
    	
    	
    	

    }
    
    private class BrightnessAction implements Action_two {

      	@Override
    	public int getDrawable_two() {
    		// TODO Auto-generated method stub
    		return R.drawable.ic_action_brightness;
    	}

    	@Override
    	public void performAction_two(View view) {
    		// TODO Auto-generated method stub
    		SeekBar brightness = (SeekBar)findViewById(R.id.seekBar1);
    		
    		if(event_counter == 0)
    		{
    		event_counter++;    		
    		brightness.setVisibility(0);
    		}
    		else if(event_counter != 0)
    		{    			
    			brightness.setVisibility(8);
    			event_counter --;
    		}
    	}

    }
    
    private class BackAction implements Action_two {

      	@Override
    	public int getDrawable_two() {
    		// TODO Auto-generated method stub
    		return R.drawable.ic_action_cancel;
    	}

    	@Override
    	public void performAction_two(View view) {
    		// TODO Auto-generated method stub
    		finish();
    	}

    }
    

public void generateNoteOnSD(String sFileName, String sBody)
{
    try
    {
        File root = new File(Environment.getExternalStorageDirectory(), "ScanNEdit/Notes");
        if (!root.exists()) 
        {
            root.mkdirs();  //makes directory
        }

        File gpxfile = new File(root, sFileName);
        FileWriter writer = new FileWriter(gpxfile);
        writer.append(sBody);
        writer.flush();
        writer.close();

        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }
    catch(IOException e)
    {
         e.printStackTrace();
         
    }
}   


public static String writeFirstinXML(Picture picture) throws Exception {
	XmlSerializer xmlSerializer = Xml.newSerializer();
	StringWriter writer = new StringWriter();

	xmlSerializer.setOutput(writer);
	// start DOCUMENT
	xmlSerializer.startDocument("UTF-8", true);
	    	// open tag: <study>    

	xmlSerializer.startTag("", "Camera");
	xmlSerializer.startTag("", "picture");
	
	xmlSerializer.startTag("", "id");
	xmlSerializer.text(String.valueOf(picture.getId()));
	xmlSerializer.endTag("", "id");
	// open tag: <topic>
	xmlSerializer.startTag("", "title");
	xmlSerializer.text(picture.getTitle());
	// close tag: </topic>
	xmlSerializer.endTag("", "title");

	// open tag: <content>
	xmlSerializer.startTag("", "desc");
	xmlSerializer.text(picture.getDesc());
	// close tag: </content>
	xmlSerializer.endTag("", "desc");

	// open tag: <author>
	xmlSerializer.startTag("", "date");
	xmlSerializer.text(picture.getDate());
	// close tag: </author>
	xmlSerializer.endTag("", "date");

	// open tag: <date>
	xmlSerializer.startTag("", "uri");
	xmlSerializer.text(picture.getUrl());
	// close tag: </date>
	xmlSerializer.endTag("", "uri");

	// close tag: </study>
	xmlSerializer.endTag("", "picture");
	xmlSerializer.endTag("", "Camera");


	// end DOCUMENT
	xmlSerializer.endDocument();

	return writer.toString();
}


public static String writeSecondinXML(Picture picture) throws Exception {
	XmlSerializer xmlSerializer = Xml.newSerializer();
	StringWriter writer = new StringWriter();

	xmlSerializer.setOutput(writer);
	// start DOCUMENT
	
	xmlSerializer.startTag("", "picture");
	
	xmlSerializer.startTag("", "id");
	xmlSerializer.text(String.valueOf(picture.getId()));
	xmlSerializer.endTag("", "id");
	// open tag: <topic>
	xmlSerializer.startTag("", "title");
	xmlSerializer.text(picture.getTitle());
	// close tag: </topic>
	xmlSerializer.endTag("", "title");

	// open tag: <content>
	xmlSerializer.startTag("", "desc");
	xmlSerializer.text(picture.getDesc());
	// close tag: </content>
	xmlSerializer.endTag("", "desc");

	// open tag: <author>
	xmlSerializer.startTag("", "date");
	xmlSerializer.text(picture.getDate());
	// close tag: </author>
	xmlSerializer.endTag("", "date");

	// open tag: <date>
	xmlSerializer.startTag("", "uri");
	xmlSerializer.text(picture.getUrl());
	// close tag: </date>
	xmlSerializer.endTag("", "uri");

	// close tag: </study>
	xmlSerializer.endTag("", "picture");
	xmlSerializer.endDocument();


	// end DOCUMENT


	return writer.toString();
}


public static Bitmap doBrightness(Bitmap src, int value) {
	// image size
	int width = src.getWidth();
	int height = src.getHeight();
	// create output bitmap
	Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
	// color information
	int A, R, G, B;
	int pixel;

	// scan through all pixels
	for(int x = 0; x < width; ++x) {
		for(int y = 0; y < height; ++y) {
			// get pixel color
			pixel = src.getPixel(x, y);
			A = Color.alpha(pixel);
			R = Color.red(pixel);
			G = Color.green(pixel);
			B = Color.blue(pixel);

			// increase/decrease each channel
			R += value;
			if(R > 255) { R = 255; }
			else if(R < 0) { R = 0; }

			G += value;
			if(G > 255) { G = 255; }
			else if(G < 0) { G = 0; }

			B += value;
			if(B > 255) { B = 255; }
			else if(B < 0) { B = 0; }

			// apply new pixel color to output bitmap
			bmOut.setPixel(x, y, Color.argb(A, R, G, B));
		}
	}

	// return final image
	return bmOut;
}

public void saveImage(Bitmap bitmap, String name, String path)
{
	//saving a selected image to SD card
	ByteArrayOutputStream bytes2 = new ByteArrayOutputStream();			    	
	bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes2);
	File f2 = new File(path + File.separator + name + ".jpg");		    	
try {
	f2.createNewFile();			
	//write the bytes in file
	FileOutputStream fo2 = new FileOutputStream(f2);
	fo2.write(bytes2.toByteArray());
	fo2.close();
} catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}	
}


}















