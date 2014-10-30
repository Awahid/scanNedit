package com.me.android.scanNedit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.Menu;
import android.widget.RelativeLayout;

public class StartActivity extends Activity {
	AnimationDrawable rocketAnimation;
	private static final String TAG = "StartActivity.java";
	public static final String lang = "eng";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    
        Handler handler = new Handler();
        RelativeLayout rocketImage = (RelativeLayout) findViewById(R.id.my);
		  rocketImage.setBackgroundResource(R.drawable.anim);
		  rocketAnimation = (AnimationDrawable) rocketImage.getBackground();
        
        // run a thread after 2 seconds to start the home screen
        handler.postDelayed(new Runnable() {
 
            @Override
            public void run() {
            	
      		  
                // make sure we close the splash screen so the user won't come back when it presses back key
               File main = new File(Environment.getExternalStorageDirectory() + "/ScanNEdit");
               File sub1 = new File(Environment.getExternalStorageDirectory() + "/ScanNEdit/Notes");
               File sub2 = new File(Environment.getExternalStorageDirectory() + "/ScanNEdit/Tesseract");
               File sub3 = new File(Environment.getExternalStorageDirectory() + "/ScanNEdit/Pictures");
               File sub4 = new File(Environment.getExternalStorageDirectory() + "/ScanNEdit/Croped_Pictures");
               File sub5 = new File(Environment.getExternalStorageDirectory() + "/ScanNEdit/Tesseract/tessdata");
           
           
               if(!main.exists())
           	{
           	    main.mkdir(); //directory is created;
           	}
           
               if(!sub1.exists())
       	    {
       	        sub1.mkdir(); //directory is created;
       	    }
           	
               if(!sub2.exists())
       	    {
       	        sub2.mkdir(); //directory is created;
       	    }
           	
               if(!sub3.exists())
       	    {
       	        sub3.mkdir(); //directory is created;
       	    }
           	
               if(!sub4.exists())
       	    {
       	        sub4.mkdir(); //directory is created;
       	    }
               
               if(!sub5.exists())
          	    {
          	        sub5.mkdir(); //directory is created;
          	    }
            
               if (!(new File(sub5 + "/" + lang+ ".traineddata")).exists()) {
       			try {

       				AssetManager assetManager = getAssets();
       				InputStream in = assetManager.open("tessdata/eng.traineddata");       				
       				OutputStream out = new FileOutputStream(sub5 + "/eng.traineddata");

       				// Transfer bytes from in to out
       				byte[] buf = new byte[1024];
       				int len;       				
       				while ((len = in.read(buf)) > 0) {
       					out.write(buf, 0, len);
       				}
       				in.close();
       				//gin.close();
       				out.close();
       				
       				Log.v(TAG, "Copied " + lang + " traineddata");
       			} catch (IOException e) {
       				Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
       			}
       		}
            
                finish();
                // start the home screen
 
                Intent intent = new Intent(StartActivity.this, SampleActivity.class);
                StartActivity.this.startActivity(intent);
 
            }
 
        }, 3000); // time in milliseconds (1 second = 1000 milliseconds) until the run() method will be called
 
    }


    @Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		
		
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus) 
		{
			rocketAnimation.start();
		 }
		 
	}
    
}
