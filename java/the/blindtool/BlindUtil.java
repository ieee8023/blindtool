package the.blindtool;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Display;
import android.view.Surface;

public class BlindUtil {

	static final String TAG = "BlindUtil";
    static final int SHORTER_SIDE = 256;
    static final int DESIRED_SIDE = 224;
	
    public static Bitmap processBitmap(final Bitmap origin) {
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
	
    public static int getRotation(Activity mActivity){
    	
    	if (mActivity == null){
    		Log.e(TAG, "mActivity is null");
    		return 0;
    	}
    	
    	int angle;
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        switch (display.getRotation()) {
            case Surface.ROTATION_0: // This is display orientation
                angle = 90; // This is camera orientation
                break;
            case Surface.ROTATION_90:
                angle = 0;
                break;
            case Surface.ROTATION_180:
                angle = 270;
                break;
            case Surface.ROTATION_270:
                angle = 180;
                break;
            default:
                angle = 90;
                break;
        }
    	
    	return angle;
    }
    
}
