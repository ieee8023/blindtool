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

    private CameraPreview2 mPreview;
    
    static final String TAG = "BlindTool";
    
    private TextToSpeech myTTS;
    
    @Override
    protected void onStop() {
    	super.onStop();
    	
    	Log.i(TAG, "onStop");
    	mUnexpectedTerminationHelper.fini();
    	
//    	if (mCamera != null){
//    		mCamera.stopPreview();
//			mCamera.setOneShotPreviewCallback(null);
//			mCamera.setPreviewCallback(null);
//			mCamera.setErrorCallback(null);
//			mCamera.unlock();
//			mCamera.release();
//			mCamera = null;
//    	}
    	
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	Log.d(TAG, "onPause ");
    	
        mPreview.stop();
        mLayout.removeView(mPreview); // This is necessary.
        mPreview = null;
    }
    
	@Override
    protected void onResume() {
		super.onResume();
		
        Log.d(TAG, "onResume ");

   	 	mPreview = new CameraPreview2(this);
        mLayout.addView(mPreview);
        
        mPreview.setPreviewCallback(new BlindToolPreviewCallback());
    }
    
    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
    	return super.onCreateView(parent, name, context, attrs);
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
    
    
//	void initCamera(){
//    	
//    	Log.i(TAG, "initCamera");
//    	
//        if (mCamera == null){
//        	Log.e(TAG, "AAAA mCamera is null");
//        }else{
//        
//        	try{
//		        Camera.Parameters camPara = mCamera.getParameters();
//		        
//				int previewFormat = 0;
//				Log.i(TAG, "Formats: " + camPara.getSupportedPreviewFormats());
//				for (int format : camPara.getSupportedPreviewFormats()) {
//					
//					if (format == ImageFormat.RGB_565){
//						previewFormat = ImageFormat.RGB_565;
//						Log.i(TAG, "Format ImageFormat.RGB_565");
//					} else if (format == ImageFormat.JPEG) {
//						previewFormat = format;
//						Log.i(TAG, "Format ImageFormat.JPEG");
//					} else if (previewFormat == 0 && format == ImageFormat.NV21) {
//						previewFormat = ImageFormat.NV21;
//						Log.i(TAG, "Format ImageFormat.NV21");
//					}
//				}
//	
//				mCamera.setParameters(camPara);
//        	}catch (Exception e){
//        		Log.e(TAG, "Cannot set format");
//        	}
//			
//        	try{
//				Camera.Parameters camPara = mCamera.getParameters();
//				
//		        for (Size format : camPara.getSupportedPreviewSizes()) {
//		        	Log.i(TAG, "AAAAA " + format.height + " " + format.width);
//		        		
//		        		if (format.height > 200)
//		        			camPara.setPreviewSize(format.width, format.height);
//			        }
//		
//		        mCamera.setParameters(camPara);
//		        
//		        Log.i(TAG, "AAAAA Set to " + mCamera.getParameters().getPreviewSize().height + " " + mCamera.getParameters().getPreviewSize().width);
//		         
//        	}catch (Exception e){
//        		Log.e(TAG, "Cannot set resolution");
//        	}
//	        
//	        
//	    	mCamera.setPreviewCallback(new BlindToolPreviewCallback());
//	    }
//    }
    
    
    
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
          Matrix matrix = new Matrix();
          matrix.postRotate(angle);
          return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    
    static void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {

    	   final int frameSize = width * height;

    	   for (int j = 0, yp = 0; j < height; j++) {
    	     int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
    	     for (int i = 0; i < width; i++, yp++) {
    	       int y = (0xff & ((int) yuv420sp[yp])) - 16;
    	       if (y < 0)
    	         y = 0;
    	       if ((i & 1) == 0) {
    	         v = (0xff & yuv420sp[uvp++]) - 128;
    	         u = (0xff & yuv420sp[uvp++]) - 128;
    	       }

    	       int y1192 = 1192 * y;
    	       int r = (y1192 + 1634 * v);
    	       int g = (y1192 - 833 * v - 400 * u);
    	       int b = (y1192 + 2066 * u);

    	       if (r < 0)
    	         r = 0;
    	       else if (r > 262143)
    	         r = 262143;
    	       if (g < 0)
    	         g = 0;
    	       else if (g > 262143)
    	         g = 262143;
    	       if (b < 0)
    	         b = 0;
    	       else if (b > 262143)
    	         b = 262143;

    	       rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
    	     }
    	   }
    	 }   
    


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_whats, menu);
        return true;
    }
    
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
	
							YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height,
									null);
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
			             
			             processedBitmap = RotateBitmap(processedBitmap, BlindUtil.getRotation(BlindTool.this));
			             
			             inputImageView.setImageBitmap(processedBitmap);
			             
			             long endtime = System.currentTimeMillis();
			             
			             Log.i(TAG, "preprocessing took " + (endtime-starttime)/1000.0 + "s");
						 
						new AsyncTask<Bitmap, Void, String[]>() {
							@Override
							protected void onPreExecute() {
	
							}
	
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
	
								if (tag[0].length() > 0 && !tag[0].equals(resultTextView.getText())) {
	
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
