package the.blindtool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import the.blindtool.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@SuppressWarnings("deprecation")
public class BlindTool extends AppCompatActivity {

    private TextView resultTextView;
    private TextView resultAllTextView;
    private ImageView inputImageView;
    private FrameLayout mLayout;
    private Bitmap bitmap;
    private UnexpectedTerminationHelper mUnexpectedTerminationHelper = new UnexpectedTerminationHelper();
    public static Activity activity;
    private AsyncTask<Void, Void, Void> cameraConnectionTask = null;
    
    private CameraPreview2 mPreview;
    
    static final String TAG = "BlindTool";
    
    private TextToSpeech myTTS;
    
    @Override
    protected void onStop() {
    	super.onStop();
    	
    	Log.i(TAG, "onStop");
    	mUnexpectedTerminationHelper.fini();
    }
    
    @Override
    protected void onPause() {
    	
    	super.onPause();
    	
    	Log.d(TAG, "onPause ");
    	
    	if (cameraConnectionTask != null)
    		cameraConnectionTask.cancel(true);
    	
    	if (mPreview != null){
    		
    		mLayout.removeView(mPreview); // This is necessary.
	        mPreview.stop();
	        mPreview = null;
	        System.gc();
    	}
    }
    
	@Override
    protected void onResume() {
		super.onResume();
		
        Log.d(TAG, "onResume ");

		connectCamera(0);
        
    }
    
	
	private void connectCamera(final int count){
		
		
        try{
        	
	   	 	mPreview = new CameraPreview2(BlindTool.this);
	        mLayout.addView(mPreview);
	        mPreview.setPreviewCallback(new BlindToolPreviewCallback());

        }catch(Throwable t){
        	
        	Log.e(TAG, "Retrying Camera Connection",t);
        	resultTextView.setText("Retrying Camera Connection " + count);
	   	 	//Toast.makeText(BlindTool.this, "Camera is locked or not available", Toast.LENGTH_SHORT).show();
	   	 	
        	cameraConnectionTask = new AsyncTask<Void, Void, Void>() {
    			@Override
    			protected void onPreExecute() {}

    			@Override
    			protected Void doInBackground(Void... voids) {
    				
    				try {
    					Thread.sleep(200);
    				} catch (InterruptedException e) {
    					Log.e(TAG, "Retry Wait Error", e);
    				}
    				return null;
    			}

    			@Override
    			protected void onPostExecute(Void v) {

    			   	 	connectCamera(count + 1);
    			}
    		}.execute();
        	
        }
		
		
		

	}
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
    	Log.i(TAG, "onCreate");
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blindtool);
        
        // keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        activity = this;
        mUnexpectedTerminationHelper.init();
        mLayout = (FrameLayout) findViewById(R.id.camera_preview);
        inputImageView = (ImageView)findViewById(R.id.tap_to_add_image);
        resultTextView = (TextView)findViewById(R.id.result_text);
        resultAllTextView = (TextView)findViewById(R.id.result_text_all);

		myTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				if (status != TextToSpeech.ERROR) {

				}
			}
		});
    }
    

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_whats, menu);
//        return true;
//    }
    
    final class BlindToolPreviewCallback implements PreviewCallback {
    	
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			
			try{
				//Log.d(TAG, "onPreviewFrame ");
				 synchronized (camera) {
				
					 if (!Lock.working){
						 Lock.working = true;
						 
						 long starttime = System.currentTimeMillis();
						 
						 //Log.i("TEST", "PreviewCallback " + Arrays.toString(data));
						 Size previewSize =  camera.getParameters().getPreviewSize();
						 
						 //System.out.println("AAAAA previewSize " + previewSize.height + " " + previewSize.width);
						 if (camera.getParameters().getPreviewFormat() == ImageFormat.NV21) {
	
							// Log.i(TAG, "NV21");
	
							YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height,null);
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, baos);
							byte[] jdata = baos.toByteArray();
							bitmap = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
	
						} else if (camera.getParameters().getPreviewFormat() == ImageFormat.JPEG
								|| camera.getParameters().getPreviewFormat() == ImageFormat.RGB_565) {
							// RGB565 and JPEG
							// Log.i(TAG, "JPEG");
							BitmapFactory.Options opts = new BitmapFactory.Options();
							opts.inDither = true;
							opts.inPreferredConfig = Bitmap.Config.RGB_565;
							bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
						}

						 Bitmap processedBitmap = BlindUtil.processBitmap(bitmap);
			             
			             processedBitmap = BlindUtil.rotateBitmap(processedBitmap, BlindUtil.getRotation(BlindTool.this));
			             
			             inputImageView.setImageBitmap(processedBitmap);
			             
			             long endtime = System.currentTimeMillis();
			             
			             Log.i(TAG, "preprocessing took " + (endtime-starttime)/1000.0 + "s");
						 
						new AsyncTask<Bitmap, Void, String[]>() {
							
							@Override
							protected void onPreExecute() {}
	
							@Override
							protected String[] doInBackground(Bitmap... bitmaps) {
								synchronized (this) {
									String[] tag = MxNetUtils.identifyImage(bitmaps[0]);
									return tag;
								}
							}
	
							@Override
							protected void onPostExecute(String[] tag) {
	
								Lock.working = false;
	
								Log.i(TAG, "identifyImage retuned: " + tag[0] + ", old: " + resultTextView.getText().toString() + ", matches: " + tag[0].equals(resultTextView.getText().toString()));
								
								if (tag[0].length() > 0 && !tag[0].equals(resultTextView.getText().toString())) {
	
									myTTS.speak(tag[0], TextToSpeech.QUEUE_FLUSH, null);
								}
	
								resultTextView.setText(tag[0]);
								resultAllTextView.setText(tag[1]);
	
							}
						}.execute(processedBitmap);
					 }
				 }
			}catch (Throwable t){
				Log.e(TAG, "Error processing preview",t);
			}
		}
	}

	static class Lock{
    	
    	public static boolean working = false;
    }
}
