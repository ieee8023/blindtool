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
    private Bitmap bitmap;
    private Button identifyButton;
    private Button autoIdentifyButton;
    private SharedPreferences sharedPreferences;
    private String currentPhotoPath;
    private static final String PREF_USE_CAMERA_KEY = "USE_CAMERA";
    public static Activity activity;

    private static final Lock lock = new Lock();
    protected static Camera mCamera;
    private CameraPreview mPreview;
    
    String TAG = "BlindTool";
    
    private TextToSpeech myTTS;
    
    @Override
    protected void onStop() {
    	super.onStop();
    	
    	Log.i(TAG, "onStop");
    	
    	mUnexpectedTerminationHelper.fini();
//    	if (mCamera != null){
//    		mCamera.stopPreview();
//    		mCamera.release();
//    		mCamera = null;
//    	}
    }
    
    @Override
    protected void onPause() {
    	
    	Log.d(TAG, "onPause " + mCamera);
    	
    	if (mCamera != null)
    		mCamera.stopPreview();
    	
//    	if (mCamera != null){
//    		
//    		mPreview.onPause();
//    		mPreview = null;
//    		mCamera.stopPreview();
//    		mCamera.unlock();
//    		//mCamera.release();
//    		//mCamera = null;
//    	}
    	
    	super.onPause();
    }
    
	@Override
    protected void onResume() {
        
        
        Log.d(TAG, "onResume " + mCamera);
        
        if (mCamera != null)
        	mCamera.startPreview();
        
//        if (mCamera == null){
//	        // Create an instance of Camera
//	        mCamera = getCameraInstance();
//	        initCamera();
//	        initPreview();
//	        mCamera.startPreview();
//        }else{
//        	try {
//				mCamera.reconnect();
//				mCamera.startPreview();
//				initPreview();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//        }
//        
        super.onResume();
         
    }
    
    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
    	Log.i(TAG, "onCreateView");
    	return super.onCreateView(parent, name, context, attrs);
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
    	Log.i(TAG, "onCreate");
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blindtool);
        activity = this;
        mUnexpectedTerminationHelper.init();
        identifyButton = (Button)findViewById(R.id.identify_button);
        autoIdentifyButton = (Button)findViewById(R.id.auto_identify_button);
        inputImageView = (ImageView)findViewById(R.id.tap_to_add_image);
        resultTextView = (TextView)findViewById(R.id.result_text);
        resultAllTextView = (TextView)findViewById(R.id.result_text_all);
        sharedPreferences = getSharedPreferences("Picture Pref", Context.MODE_PRIVATE);

        myTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
               if(status != TextToSpeech.ERROR) {
            	   
               }
            }
         });
        
        mCamera = getCameraInstance();
        initCamera();
        initPreview();
        mCamera.startPreview();
        
    }
    
    
    void initPreview(){
    	
        if (mCamera == null){
        	Log.e(TAG, "AAAA mCamera is null");
        }else{
    	
	        // Create our Preview view and set it as the content of our activity.
	   	 	mPreview = new CameraPreview(this, mCamera);
	        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
	        preview.addView(mPreview);
        }
    	
    }
    
    
    @SuppressWarnings("deprecation")
	void initCamera(){
    	
    	Log.i(TAG, "initCamera");
    	
        if (mCamera == null){
        	Log.e(TAG, "AAAA mCamera is null");
        }else{
        
        	try{
		        Camera.Parameters camPara = mCamera.getParameters();
		        
				int previewFormat = 0;
				Log.i(TAG, "Formats: " + camPara.getSupportedPreviewFormats());
				for (int format : camPara.getSupportedPreviewFormats()) {
					
					if (format == ImageFormat.RGB_565){
						previewFormat = ImageFormat.RGB_565;
						Log.i(TAG, "Format ImageFormat.RGB_565");
					} else if (format == ImageFormat.JPEG) {
						previewFormat = format;
						Log.i(TAG, "Format ImageFormat.JPEG");
					} else if (previewFormat == 0 && format == ImageFormat.NV21) {
						previewFormat = ImageFormat.NV21;
						Log.i(TAG, "Format ImageFormat.NV21");
					}
				}
	
				mCamera.setParameters(camPara);
        	}catch (Exception e){
        		Log.e(TAG, "Cannot set format");
        	}
			
        	try{
				Camera.Parameters camPara = mCamera.getParameters();
				
		        for (Size format : camPara.getSupportedPreviewSizes()) {
		        		System.out.println("AAAAA " + format.height + " " + format.width);
		        		
		        		if (format.height > 200)
		        			camPara.setPreviewSize(format.width, format.height);
			        }
		
		        mCamera.setParameters(camPara);
		        
		        System.out.println("AAAAA Set to " + mCamera.getParameters().getPreviewSize().height + " " + mCamera.getParameters().getPreviewSize().width);
		         
        	}catch (Exception e){
        		Log.e(TAG, "Cannot set resolution");
        	}
	        
	        
	    	mCamera.setPreviewCallback(new PreviewCallback() {
				
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					
					 synchronized (camera) {
					
						 if (!lock.working){
							 lock.working = true;
							 
							 long starttime = System.currentTimeMillis();
							 
							 //Log.i("TEST", "PreviewCallback " + Arrays.toString(data));
							 Size previewSize =  mCamera.getParameters().getPreviewSize();
							 
							 //System.out.println("AAAAA previewSize " + previewSize.height + " " + previewSize.width);
							  if (mCamera.getParameters().getPreviewFormat() == ImageFormat.NV21) {
								  
								  //Log.i(TAG, "NV21");
								  
								  YuvImage yuvimage=new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
								  ByteArrayOutputStream baos = new ByteArrayOutputStream();
								  yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, baos);
								  byte[] jdata = baos.toByteArray();
								  bitmap  = BitmapFactory.decodeByteArray(jdata, 0, jdata.length); 
								  
								  } else if (mCamera.getParameters().getPreviewFormat() == ImageFormat.JPEG || mCamera.getParameters().getPreviewFormat() == ImageFormat.RGB_565) {
								    // RGB565 and JPEG
									 //Log.i(TAG, "JPEG");
								    BitmapFactory.Options opts = new BitmapFactory.Options();
								    opts.inDither = true;
								    opts.inPreferredConfig = Bitmap.Config.RGB_565;
								    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
								  }
			                 
							 Bitmap processedBitmap = processBitmap(bitmap);
			                 
			                 processedBitmap = RotateBitmap(processedBitmap, 90);
			                 
			                 inputImageView.setImageBitmap(processedBitmap);
			                 
			                 
			                 long endtime = System.currentTimeMillis();
			                 
			                 Log.i("AAAAA", "preprocessing took " + (endtime-starttime)/1000.0 + "s");
							 
			                 new AsyncTask<Bitmap, Void, String[]>(){
			                     @Override
			                     protected void onPreExecute() {
			                    	 
			                     }
	
			                     @Override
			                     protected String[] doInBackground(Bitmap... bitmaps) {
			                         synchronized (identifyButton) {
			                             String[] tag = MxNetUtils.identifyImage(bitmaps[0]);
			                             return tag;
			                         }
			                     }
			                     @Override
			                     protected void onPostExecute(String[] tag) {
	
			                    	 lock.working = false;
			                    	 
			                         if (tag[0].length() > 0 &&
			                             !resultTextView.getText().equals(tag[0])){
			                 	    	myTTS.speak(tag[0], TextToSpeech.QUEUE_FLUSH, null);
			                         }
			                         
			                         resultTextView.setText(tag[0]);
			                         resultAllTextView.setText(tag[1]);
			                         
			                     }
			                 }.execute(processedBitmap);
						 }
					 }
				}
			});
	    }
    }
    
    
    
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
    
    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
    
    private static int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
          CameraInfo info = new CameraInfo();
          Camera.getCameraInfo(i, info);
          if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            Log.d("AAA", "Camera found");
            cameraId = i;
            break;
          }
        }
        return cameraId;
      }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, Constants.CAPTURE_PHOTO_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_whats, menu);
        return true;
    }

    static final int SHORTER_SIDE = 256;
    static final int DESIRED_SIDE = 224;

    private static Bitmap processBitmap(final Bitmap origin) {
        //TODO: error handling
        final int originWidth = origin.getWidth();
        final int originHeight = origin.getHeight();
        int height = SHORTER_SIDE;
        int width = SHORTER_SIDE;
        if (originWidth < originHeight) {
            height = (int)((float)originHeight / originWidth * width);
        } else {
            width = (int)((float)originWidth / originHeight * height);
        }
        final Bitmap scaled = Bitmap.createScaledBitmap(origin, width, height, false);
        int y = (height - DESIRED_SIDE) / 2;
        int x = (width - DESIRED_SIDE) / 2;
        return Bitmap.createBitmap(scaled, x, y, DESIRED_SIDE, DESIRED_SIDE);
    }
    
    static class Lock{
    	
    	boolean working = false;
    }
    
    
    private UnexpectedTerminationHelper mUnexpectedTerminationHelper = new UnexpectedTerminationHelper();
    
}
