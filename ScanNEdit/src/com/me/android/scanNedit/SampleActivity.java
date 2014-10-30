package com.me.android.scanNedit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;
import com.markupartist.android.widget.ActionBar_three;
import com.markupartist.android.widget.ActionBar_three.Action_three;
import com.markupartist.android.widget.ActionBar_two;
import com.markupartist.android.widget.ActionBar_two.Action_two;
import com.me.test.MainActivity;

public class SampleActivity extends Activity {
    
	// All static variables
		static final String URL = Environment.getExternalStorageDirectory().getPath()+"/ScanNEdit/Notes/camera.xml";
	// XML node keys
		static final String KEY_PICTURE = "picture"; // parent node
		static final String KEY_ID = "id";
		static final String KEY_TITLE = "title";
		static final String KEY_DESC = "desc";
		static final String KEY_DATE = "date";
		static final String KEY_URL = "uri";
		ListView list;
	    LazyAdapter adapter;
	    int count =0;		//for back key Overriding 
	    final CharSequence[] items = {"Scan","Delete"};		//List Item dialog box options
	    final CharSequence[] items_two = {"Clear List"};	//Edit dialog box options
	    final CharSequence[] items_three = {"List View","Grid View"};	//Settings dialog box options
	    public int share=0;
	    public static final String PACKAGE_NAME = "com.me.android.scanNedit";
		public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/ScanNEdit/";
		private static final String TAG = "SampleActivity.java";
		public static final String lang = "eng";
		public String recognizedText = "";
		public String textFileName = "";
		public String textFileName_two = "";
	
	private MainActivity fan;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        
        fan = (MainActivity) findViewById(R.id.activity_main);
        fan.setViews(R.layout.main, R.layout.fan);        
        fan.setFadeOnMenuToggle(true);
        fan.setAnimationDuration(1000);
        fan.setIncludeDropshadow(false);
         

       //first activity's upper action bar including its actions         
         final ActionBar actionBar_high_one = (ActionBar) findViewById(R.id.actionbar);         
         actionBar_high_one.setHomeAction(new IntentAction(this, SampleActivity.createIntent(this), R.drawable.ic_title_home_default));         
         actionBar_high_one.setTitle("Scan n Edit");         
         actionBar_high_one.addAction(new HelpAction());
         actionBar_high_one.addAction(new ShareAction());
         
         
       //second activity's upper action bar including its actions         
         final ActionBar_three actionBar_high_two = (ActionBar_three) findViewById(R.id.actionbar3);         
         actionBar_high_two.setHomeLogo_three(R.drawable.ic_menu_orig);         
         actionBar_high_two.setTitle_three("               Menu");
         
        
         
        //second activity's lower action bar including its actions         
         final ActionBar_two actionBar_low_two = (ActionBar_two) findViewById(R.id.actionbar4); 
         actionBar_low_two.addAction_two(new CameraAction());
         actionBar_low_two.addAction_two(new GalleryAction());
         actionBar_low_two.addAction_two(new RefreshAction());
         actionBar_low_two.addAction_two(new EditAction());
         actionBar_low_two.addAction_two(new SettingsAction());
              
        //Large Camera button listener         
         Button camera = (Button)findViewById(R.id.camera);
         camera.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getBaseContext(), CameraActivity.class);
				startActivity(intent);
			}
		});
         
        //Large Gallery button listener         
         Button gallery = (Button)findViewById(R.id.gallery);
         gallery.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getBaseContext(), GalleryActivity.class);
				startActivity(intent);
			}
		});
         
        //Large Scan button listener         
         Button Scan = (Button)findViewById(R.id.scan);
         Scan.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ScanLastImage();					
			}
		});
         
       //Large Text File button listener         
         Button TextFile = (Button)findViewById(R.id.adjustment);
         TextFile.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
					
				try{
					OpenTextFile();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
				
				
			}
		});
         
         //initial camera.xml creation without any data
         File myFile = new File("/sdcard/ScanNEdit/Notes/camera.xml");
         if(myFile.exists()==false)
         {
        	 try {
 				myFile.createNewFile();
 				FileOutputStream fOut = new FileOutputStream(myFile);
 				OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut); 				 				
     			String data = "<Camera></Camera>";  				
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
         
        //listview
        final AlertDialog.Builder builder	= new AlertDialog.Builder(this); 
        ArrayList<HashMap<String, String>> picturesList = new ArrayList<HashMap<String, String>>();

 		XMLParser parser = new XMLParser();
 		String xml = parser.getXmlFromUrl(URL); // getting XML from URL
 		Document doc = parser.getDomElement(xml); // getting DOM element
 		
 		NodeList nl = doc.getElementsByTagName(KEY_PICTURE);
 		// looping through all picture nodes <picture>
 		for (int i = 0; i < nl.getLength(); i++) {
 			// creating new HashMap
 			HashMap<String, String> map = new HashMap<String, String>();
 			Element e = (Element) nl.item(i);
 			// adding each child node to HashMap key => value
 			map.put(KEY_ID, parser.getValue(e, KEY_ID));
 			map.put(KEY_TITLE, parser.getValue(e, KEY_TITLE));
 			map.put(KEY_DESC, parser.getValue(e, KEY_DESC));
 			map.put(KEY_DATE, parser.getValue(e, KEY_DATE));
 			map.put(KEY_URL, parser.getValue(e, KEY_URL));

 			// adding HashList to ArrayList
 			picturesList.add(map); 			
 		}
         		
 		list=(ListView)findViewById(R.id.list); 		
 		// Getting adapter by passing xml data ArrayList
 	    adapter=new LazyAdapter(this, picturesList);        
 	    list.setAdapter(adapter);  	  

 	    // Click event for single list row
 	    list.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
 							
 				builder.setTitle("Choose an option");	
 				builder.setItems(items, new DialogInterface.OnClickListener() {
 		        	// Click listener
 		        	public void onClick(DialogInterface dialog, int item) {
 		        		if(item == 0)//Scan option
 		        		{
 		        	       	try {
								Scan_List_Item(position);
								//Toast.makeText(getApplicationContext(), "Scaned.!!", Toast.LENGTH_SHORT).show();
							} catch (TransformerException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}	        	        
 		        	        }
 		        		
 		        		else if(item == 1)//Delete option
 		        		{ 		        			
 		        			try {
								Delete_List_Item(position);
								Toast.makeText(getApplicationContext(), "Deleted.!!", Toast.LENGTH_SHORT).show();
							} catch (TransformerException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 		        			
 		        		}
 		        		}
 		        	});
 		        	AlertDialog alert = builder.create();
 		        	//display dialog box
 		        	alert.show();
 			}
 		});	
 	    
 	}
    
    
 		 
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && count == 0) {
        	count++;
        	fan.showMenu();        
            return true;
        }
        else if(keyCode == KeyEvent.KEYCODE_BACK && count != 0)
        {
        	finish();      	
        }
        return super.onKeyDown(keyCode, event);
    }    
    
    public void unclick(View v) {
    	System.out.println("CLOSE");
    	fan.showMenu();
    }
    
    public void click(View v) {
    	System.out.println("OPEN");
    	fan.showMenu();
    }
    
    public void start()
    {
    	int i=5;    	
    }
    
    public static Intent createIntent(Context context) {
        Intent i = new Intent(context, SampleActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }

    private Intent createShareIntent(String value) {
    	
    	final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, value);
        return Intent.createChooser(intent, "Share");
    }
   
    
    //second activity's lower action bar's action classes or triggers    
    private class CameraAction implements Action_two {

      		@Override
		public int getDrawable_two() {
			// TODO Auto-generated method stub
			return R.drawable.ic_action_camera;
		}

		@Override
		public void performAction_two(View view) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(getBaseContext(), CameraActivity.class);
			startActivity(intent);
		}

    }

    private class GalleryAction implements Action_two {

  		@Override
	public int getDrawable_two() {
		// TODO Auto-generated method stub
		return R.drawable.ic_action_gallery;
	}

	@Override
	public void performAction_two(View view) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(getBaseContext(), GalleryActivity.class);
		startActivity(intent);
	}

}
   
    private class RefreshAction implements Action_two {

  		@Override
	public int getDrawable_two() {
		// TODO Auto-generated method stub
		return R.drawable.ic_action_refresh;
	}

	@Override
	public void performAction_two(View view) {
		// TODO Auto-generated method stub
	    Refresh_List();
		
	}
}

    private class EditAction implements Action_two {

  		@Override
	public int getDrawable_two() {
		// TODO Auto-generated method stub
		return R.drawable.ic_action_edit;
	}

	@Override
	public void performAction_two(View view) {
		// TODO Auto-generated method stub
			AlertDialog.Builder builder	= new AlertDialog.Builder(SampleActivity.this); 
			builder.setTitle("Choose an option");	
			builder.setItems(items_two, new DialogInterface.OnClickListener() {
	        	// Click listener
	        	public void onClick(DialogInterface dialog, int item) {
	        		try {
						Clear_List();
						//Toast.makeText(getApplicationContext(), "Cleared.!!", Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	       	        
	        	              		
	        		}
	        	});
	        	AlertDialog alert = builder.create();
	        	//display dialog box
	        	alert.show();
}
    }

    private class SettingsAction implements Action_two {

  		@Override
	public int getDrawable_two() {
		// TODO Auto-generated method stub
		return R.drawable.ic_action_settings;
	}

	@Override
	public void performAction_two(View view) {
		// TODO Auto-generated method stub
			AlertDialog.Builder builder	= new AlertDialog.Builder(SampleActivity.this); 
			builder.setTitle("Choose Layout");	
			builder.setItems(items_three, new DialogInterface.OnClickListener() {
	        	// Click listener
	        	public void onClick(DialogInterface dialog, int item) {
	        		if(item == 0)//List View option
	        		{
	        	       	try {
						
	        	       		
						Toast.makeText(getApplicationContext(), "List View.!!", Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	        	        
	        	        }
	        		
	        		else if(item == 1)//Grid View option
	        		{ 		        			
	        			try {
						
	        				
						Toast.makeText(getApplicationContext(), "Grid View.!!", Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 		        			
	        		}
	        		}
	        	});
	        	AlertDialog alert = builder.create();
	        	//display dialog box
	        	alert.show();
		}


}

  //second activity's higher action bar's action classes or triggers    
    private class SettingsActions implements Action_three {

  	@Override
	public int getDrawable_three() {
		// TODO Auto-generated method stub
		return R.drawable.ic_action_settings;
	}

	@Override
	public void performAction_three(View view) {
		// TODO Auto-generated method stub
		Toast.makeText(SampleActivity.this,
                "Settings action", Toast.LENGTH_LONG).show();
	}

}

  //first activity's higher action bar's action classes or triggers
    
    private class HelpAction implements Action {

      	@Override
    	public int getDrawable() {
    		// TODO Auto-generated method stub
    		return R.drawable.ic_action_help;
    	}

    	@Override
    	public void performAction(View view) {
    		// TODO Auto-generated method stub
    		Intent intent = new Intent(getBaseContext(), HelpActivity.class);
			startActivity(intent);
    	}

    }
    
    private class ShareAction implements Action {

      	@Override
    	public int getDrawable() {
    		// TODO Auto-generated method stub
    		return R.drawable.ic_action_share;
    	}

    	@Override
    	public void performAction(View view) {
    		// TODO Auto-generated method stub
    		share();
    	}

    }
    
    public void Refresh_List()
    {
    	ArrayList<HashMap<String, String>> picturesList = new ArrayList<HashMap<String, String>>();
 		XMLParser parser = new XMLParser();
 		String xml = parser.getXmlFromUrl(URL); // getting XML from URL
 		Document doc = parser.getDomElement(xml); // getting DOM element
 		
 		NodeList nl = doc.getElementsByTagName(KEY_PICTURE);
 		// looping through all picture nodes <picture>
 		for (int i = 0; i < nl.getLength(); i++) {
 			// creating new HashMap
 			HashMap<String, String> map = new HashMap<String, String>();
 			Element e = (Element) nl.item(i);
 			// adding each child node to HashMap key => value
 			map.put(KEY_ID, parser.getValue(e, KEY_ID));
 			map.put(KEY_TITLE, parser.getValue(e, KEY_TITLE));
 			map.put(KEY_DESC, parser.getValue(e, KEY_DESC));
 			map.put(KEY_DATE, parser.getValue(e, KEY_DATE));
 			map.put(KEY_URL, parser.getValue(e, KEY_URL));

 			// adding HashList to ArrayList
 			picturesList.add(map);
 		}
         		
 		list=(ListView)findViewById(R.id.list); 		
 		// Getting adapter by passing xml data ArrayList
 	    adapter=new LazyAdapter(SampleActivity.this, picturesList);        
 	    list.setAdapter(adapter);    	
    }

    public void Delete_List_Item(int value) throws TransformerException
    {
    	ArrayList<HashMap<String, String>> picturesList = new ArrayList<HashMap<String, String>>();
 		XMLParser parser = new XMLParser();
 		String xml = parser.getXmlFromUrl(URL); // getting XML from URL
 		Document doc = parser.getDomElement(xml); // getting DOM element
 		
 		//Getting elemet by tag name and gets parent node (Camera) to delete any picture element by index 
 		Element element = (Element) doc.getElementsByTagName(KEY_PICTURE).item(value);
 		Node parent = element.getParentNode();
 		parent.removeChild(element);
 	    parent.normalize();
 	    
 	    //After removing a picture element trnsforms the xml file to get change or rewrite
  	   	TransformerFactory transformerFactory = TransformerFactory.newInstance();
  	    Transformer transformer = transformerFactory.newTransformer();
  	    DOMSource source = new DOMSource(doc);
  	    File myFile = new File("/sdcard/ScanNEdit/Notes/camera.xml");
  	    StreamResult result = new StreamResult(myFile);
  	    transformer.transform(source, result);
 		
 		NodeList nl = doc.getElementsByTagName(KEY_PICTURE);  	
 		
 		//after last list item deleted a new xml file generatin because of a tag problem(</camera>)
 		if(nl.getLength()==0)
 		{ 			
 			try { 				
 				File myFile1 = new File(Environment.getExternalStorageDirectory().getPath()+"/ScanNEdit/Notes/camera.xml");				
 				FileOutputStream fOut = new FileOutputStream(myFile1);
 				OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut); 				 				
     			String data = "<Camera></Camera>";  				
 				myOutWriter.append(data);			
 				myOutWriter.close();
 				fOut.close(); 				
			}
			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
 		}
 		
 		// looping through all picture nodes <picture>
 		for (int i = 0; i < nl.getLength(); i++) {
 			 			
 			
 			// creating new HashMap
 			HashMap<String, String> map = new HashMap<String, String>();
 			Element e = (Element) nl.item(i);
 			
 			// adding each child node to HashMap key => value
 			map.put(KEY_ID, parser.getValue(e, KEY_ID));
 			map.put(KEY_TITLE, parser.getValue(e, KEY_TITLE));
 			map.put(KEY_DESC, parser.getValue(e, KEY_DESC));
 			map.put(KEY_DATE, parser.getValue(e, KEY_DATE));
 			map.put(KEY_URL, parser.getValue(e, KEY_URL));

 			// adding HashList to ArrayList
 			picturesList.add(map);
 		}
         		
 		list=(ListView)findViewById(R.id.list); 		
 		// Getting adapter by passing xml data ArrayList
 	    adapter=new LazyAdapter(SampleActivity.this, picturesList);        
 	    list.setAdapter(adapter);
 	  
    	
    }

    public void Scan_List_Item(int value) throws TransformerException
    {
    	final int val = value;
		AlertDialog.Builder alert = new AlertDialog.Builder(this);    	
    	alert.setTitle("Saving..");
    	alert.setMessage("Please enter Text file name without (.txt)");

    	// Set an EditText view to get user input 
    	final EditText input = new EditText(this);
    	alert.setView(input);

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog, int whichButton) {
    	  
    		textFileName = input.getText().toString();
    		
    		//start of progress dialog
    		final ProgressDialog myPd_ring=ProgressDialog.show(SampleActivity.this, "Please wait", "Scanning...", true);
    		
    		myPd_ring.setCancelable(true);
            new Thread(new Runnable() {  
                  @Override
                  public void run() {
                        // TODO Auto-generated method stub
                        try
                        {
                              Thread.sleep(3000);
                        		
     		XMLParser parser = new XMLParser();
     		String xml = parser.getXmlFromUrl(URL); // getting XML from URL
     		Document doc = parser.getDomElement(xml); // getting DOM element
     		
     		//getting the node value of uri
     		Element element = (Element) doc.getElementsByTagName(KEY_PICTURE).item(val);
     		String s = element.getLastChild().getTextContent();
     		if(s!=null) //if uri has some path
     		{
     			BitmapFactory.Options options = new BitmapFactory.Options();
     			options.inSampleSize = 4;
     			Bitmap bitmap = BitmapFactory.decodeFile(s, options);
     		
     			try {
     				ExifInterface exif = new ExifInterface(s);
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
    }
            
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

    
    public void Clear_List()
    {
    	
    	
    	//clearing all data from list
 		try { 				
				File myFile1 = new File(Environment.getExternalStorageDirectory().getPath()+"/ScanNEdit/Notes/camera.xml");				
				FileOutputStream fOut = new FileOutputStream(myFile1);
				OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut); 				 				
 			String data = "<Camera></Camera>";  				
				myOutWriter.append(data);			
				myOutWriter.close();
				fOut.close(); 				
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
    	
    	//now getting an empty list
 		ArrayList<HashMap<String, String>> picturesList = new ArrayList<HashMap<String, String>>();
 		XMLParser parser = new XMLParser();
 		String xml = parser.getXmlFromUrl(URL); // getting XML from URL
 		Document doc = parser.getDomElement(xml); // getting DOM element  
 		
 		NodeList nl = doc.getElementsByTagName(KEY_PICTURE);
 		
 		// looping through all picture nodes <picture>
 		for (int i = 0; i < nl.getLength(); i++) {
 			// creating new HashMap
 			HashMap<String, String> map = new HashMap<String, String>();
 			Element e = (Element) nl.item(i);
 			// adding each child node to HashMap key => value
 			map.put(KEY_ID, parser.getValue(e, KEY_ID));
 			map.put(KEY_TITLE, parser.getValue(e, KEY_TITLE));
 			map.put(KEY_DESC, parser.getValue(e, KEY_DESC));
 			map.put(KEY_DATE, parser.getValue(e, KEY_DATE));
 			map.put(KEY_URL, parser.getValue(e, KEY_URL));

 			// adding HashList to ArrayList
 			picturesList.add(map);
 		}
         		
 		list=(ListView)findViewById(R.id.list); 		
 		// Getting adapter by passing xml data ArrayList
 	    adapter=new LazyAdapter(SampleActivity.this, picturesList);        
 	    list.setAdapter(adapter);    
    	
    	
    	
    	
    	
    	
    }
    
        public void share	()
    {
    	
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);    	
    	alert.setTitle("Sharing..");
    	alert.setMessage("Please enter Text file name");

    	// Set an EditText view to get user input 
    	final EditText input = new EditText(this);
    	alert.setView(input);

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog, int whichButton) {
    	  
    		String value = input.getText().toString();
    		String myFile = Environment.getExternalStorageDirectory().getPath() + "/ScanNEdit/Notes/"+value+".txt";
    		value = ReadFromFile(myFile);
    		if(value!=null)
    		{
    		startActivity(createShareIntent(value));
    		}	  
    	  }
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	  public void onClick(DialogInterface dialog, int whichButton) {
    	    // Canceled.
    	  }
    	});

    	alert.show();
    }
    
       
    public String ReadFromFile(String url) {
    	final AlertDialog.Builder alert2 = new AlertDialog.Builder(this);
        
		String xml = "";
		String data = "";

			File myFile = new File(url);    
    		if(myFile.exists()==true)
    		{
    			    		
    			try {
    			//read xml to string	
    				FileInputStream fIn = new FileInputStream(myFile);
        			BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
        			while ((data = myReader.readLine()) != null) {
                    xml += data + "\n";
                }		
        			myReader.close();			
    			
    			} catch (IOException e) {
    				// TODO Auto-generated catch block    				
    				alert2.setTitle("Warning");
    		    	alert2.setMessage("Text file does not exist in SD card");
    		    	alert2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    		        	public void onClick(DialogInterface dialog, int whichButton) {

    		        	 }
    		    	});
    		    	alert2.show();	
    			} catch (Exception e) {
    				// TODO Auto-generated catch block    				
    				alert2.setTitle("Warning");
    		    	alert2.setMessage("Text file does not exist in SD card");
    		    	alert2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    		        	public void onClick(DialogInterface dialog, int whichButton) {

    		        	 }
    		    	});
    		    	alert2.show();	
    			}
    							
    		}		
    		else{
    			alert2.setTitle("Warning");
		    	alert2.setMessage("Text file does not exist in SD card");
		    	alert2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		        	public void onClick(DialogInterface dialog, int whichButton) {

		        	 }
		    	});
		    	alert2.show();	
		    	return null;
    		}
		return xml;
	}
	 
    public void generateNoteOnSD(String sFileName, String sBody)
    {
        try
        {
            File gpxfile = new File(DATA_PATH + "/Notes/", sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();

            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        }
        catch(IOException e)
        {
        	final AlertDialog.Builder alert2 = new AlertDialog.Builder(this);            
        	alert2.setTitle("Warning!");
	    	alert2.setMessage("File with same name already exist.!!");
	    	alert2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        	public void onClick(DialogInterface dialog, int whichButton) {

	        	 }
	    	});
	    	alert2.show();  
            e.printStackTrace(); 
        }
    }   
    
    
 public void OpenTextFile()
 {
	 
	AlertDialog.Builder alert = new AlertDialog.Builder(this);    	
 	alert.setTitle("Open..");
 	alert.setMessage("Please enter Text file name without (.txt)");

 	// Set an EditText view to get user input 
 	final EditText input = new EditText(this);
 	alert.setView(input);

 	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 	public void onClick(DialogInterface dialog, int whichButton) {
 	  
 		String path = Environment.getExternalStorageDirectory().getPath() + "/ScanNEdit/Notes/"+input.getText().toString()+".txt";
	  
     Intent intent = new Intent();
     intent.setAction(android.content.Intent.ACTION_VIEW);
     File file = new File(path);
   
     MimeTypeMap mime = MimeTypeMap.getSingleton();
     String ext=file.getName().substring(file.getName().indexOf(".")+1);
     String type = mime.getMimeTypeFromExtension(ext);
  
     intent.setDataAndType(Uri.fromFile(file),type);
   
     startActivity(intent);
	 
 	}
	});

	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	  public void onClick(DialogInterface dialog, int whichButton) {
	    // Canceled.    		  
	  }
	});

	alert.show();
	 
	 
 }
    
 public void ScanLastImage()
 {
	 
	 	XMLParser parser = new XMLParser();
		String xml = parser.getXmlFromUrl(URL); // getting XML from URL
		Document doc = parser.getDomElement(xml); // getting DOM element		
		int value = doc.getElementsByTagName(KEY_PICTURE).getLength();			
		if(value != 0)
		{
			//getting last node value
			Element element = (Element) doc.getElementsByTagName(KEY_PICTURE).item(value-1);
			textFileName_two = element.getLastChild().getTextContent();
		
    
	//scanning start
	AlertDialog.Builder alert = new AlertDialog.Builder(this);    	
	alert.setTitle("Saving..");
	alert.setMessage("Please enter Text file name without (.txt)");

	// Set an EditText view to get user input 
	final EditText input = new EditText(this);
	alert.setView(input);

	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	public void onClick(DialogInterface dialog, int whichButton) {
	  
		textFileName = input.getText().toString();
		
		//start of progress dialog
		final ProgressDialog myPd_ring=ProgressDialog.show(SampleActivity.this, "Please wait", "Scanning...", true);
		
		myPd_ring.setCancelable(true);
     new Thread(new Runnable() {  
           @Override
           public void run() {
                 // TODO Auto-generated method stub
                 try
                 {
                       Thread.sleep(3000);
                
		if(textFileName_two!=null) //if uri has some path
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;
			Bitmap bitmap = BitmapFactory.decodeFile(textFileName_two, options);
		
			try {
				ExifInterface exif = new ExifInterface(textFileName_two);
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
}
     
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
 
 else
 {
	 	AlertDialog.Builder alert = new AlertDialog.Builder(this);    	
		alert.setTitle("Error!");
		alert.setMessage("You don't have any last picture saved in memory..");

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		 
		}
		});
		alert.show();

 }
 } 
 
 
}//end of class