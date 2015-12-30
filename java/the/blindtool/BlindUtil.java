package the.blindtool;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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
    
	public static Bitmap rotateBitmap(Bitmap source, float angle) {
		
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
}
