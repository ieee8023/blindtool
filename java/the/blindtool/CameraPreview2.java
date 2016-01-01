package the.blindtool;

import java.io.IOException;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

@SuppressWarnings("deprecation")
public class CameraPreview2 extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    private static final String LOG_TAG = "BlindTool";
    private static final String CAMERA_PARAM_ORIENTATION = "orientation";
    private static final String CAMERA_PARAM_LANDSCAPE = "landscape";
    private static final String CAMERA_PARAM_PORTRAIT = "portrait";
    protected Activity mActivity;
    
    private PreviewCallback previewCallback;

    protected List<Camera.Size> mPreviewSizeList;
    protected List<Camera.Size> mPictureSizeList;
    protected Camera.Size mPreviewSize;
    protected Camera.Size mPictureSize;

    static String TAG = "BlindTool";
    public static int displayOrientation = 0;

    
	public CameraPreview2(Context context) throws Exception{
        super(context);

        this.mActivity=(Activity)context;
        this.mCamera = Camera.open();

        if (this.mCamera == null){
        	throw new Exception("Camera Null");
        }
        
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


    }

    public void surfaceCreated(SurfaceHolder holder) {
    	
    	Log.d(TAG, "surfaceCreated " + mCamera);
    	
    	if (mCamera == null)
    		return;
    	
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
        	mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
        	
            mCamera.release();
            mCamera = null;
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	
    	Log.d(TAG, "surfaceDestroyed " + mCamera);
    	
        stop();
    }
    
    public void stop() {
    	
    	Log.d(TAG, "stop " + mCamera);
    	
        if (null == mCamera) {
            return;
        }

        try{
			mCamera.stopPreview();
			mCamera.setOneShotPreviewCallback(null);
			mCamera.setPreviewCallback(null);
			mCamera.setPreviewDisplay(null);
			mCamera.setErrorCallback(null);
			mCamera.unlock();
			mCamera.release();
			mCamera = null;
		
        }catch(Exception e){
        	Log.e(TAG, "Cannot release camera",e);
        }
        
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	
    	Log.d(TAG, "surfaceChanged " + mCamera);
    	
    	if (mCamera == null)
    		return;
    	
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

//        // stop preview before making changes
//        try {
////        	mCamera.setPreviewCallback(null);
////        	mCamera.stopPreview();
//        	
//        } catch (Exception e){
//          // ignore: tried to stop a non-existent preview
//        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        try {
            configureCameraParameters();

        } catch (Exception e){
            Log.d("CameraView", "Error setting camera params: " + e.getMessage());
        }
        
        
        // start preview with new settings
        try {
        	 mCamera.startPreview();
        	 mCamera.setPreviewCallback(previewCallback);

        } catch (Exception e){
            Log.d("CameraView", "Error starting camera preview: " + e.getMessage());
        }
        
        
       
        
    }

	protected void configureCameraParameters() {
    	
    	Log.d(TAG, "configureCameraParameters " + mCamera);
    	
    	if (mCamera == null)
    		return;
    	
    	//cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
    	
    	{
    		Camera.Parameters camPara = mCamera.getParameters();
    	
    		camPara.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
	    		
	    	mCamera.setParameters(camPara);
    	}
    	{
    		
    	
	        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) { // for 2.1 and before
	        	
	        	Camera.Parameters camPara = mCamera.getParameters();
	            if (isPortrait()) {
	            	camPara.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_PORTRAIT);
	            } else {
	            	camPara.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_LANDSCAPE);
	            }
	            mCamera.setParameters(camPara);
	        } else { // for 2.2 and later
	        	
	        	displayOrientation = BlindUtil.getRotation(mActivity);
	            Log.v(LOG_TAG, "angle: " + displayOrientation);
	            mCamera.setDisplayOrientation(displayOrientation);
	        }
    	}
        
        {
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
        }
		
		
		
        {
		Camera.Parameters camPara = mCamera.getParameters();
		
        for (Size format : camPara.getSupportedPreviewSizes()) {
        	Log.i(TAG, "AAAAA " + format.height + " " + format.width);
        		
        		if (format.height > 200)
        			camPara.setPreviewSize(format.width, format.height);
	        }

        mCamera.setParameters(camPara);
        
        Log.i(TAG, "AAAAA Set to " + mCamera.getParameters().getPreviewSize().height + " " + mCamera.getParameters().getPreviewSize().width);
        }
        
    }


    public boolean isPortrait() {
        return (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
    }

	public void setPreviewCallback(PreviewCallback previewCallback) {
		
		this.previewCallback = previewCallback;
		mCamera.setPreviewCallback(previewCallback);

	}
}