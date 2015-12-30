package the.blindtool;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Vibrator;
import android.util.Log;

import org.dmlc.mxnet.Predictor;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by leliana on 11/6/15.
 */
public class MxNetUtils {
    private static boolean libLoaded = false;
    private MxNetUtils() {}

    static final String TAG = "BlindTool - MXNET";
    
    static String name = "";
    static int count = 0;
    
    public static String[] identifyImage(final Bitmap bitmap) {
    	
    	long starttime = System.currentTimeMillis();
    	
        ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(byteBuffer);
        byte[] bytes = byteBuffer.array();
        float[] colors = new float[bytes.length / 4 * 3];

        float mean_b = BlindApplication.getMean().get("b");
        float mean_g = BlindApplication.getMean().get("g");
        float mean_r = BlindApplication.getMean().get("r");
        for (int i = 0; i < bytes.length; i += 4) {
            int j = i / 4;
            colors[0 * 224 * 224 + j] = (float)(((int)(bytes[i + 0])) & 0xFF) - mean_r;
            colors[1 * 224 * 224 + j] = (float)(((int)(bytes[i + 1])) & 0xFF) - mean_g;
            colors[2 * 224 * 224 + j] = (float)(((int)(bytes[i + 2])) & 0xFF) - mean_b;
        }
        Predictor predictor = BlindApplication.getPredictor();
        predictor.forward("data", colors);
        final float[] result = predictor.getOutput(0);

        
        Cat[] results = new Cat[result.length];
        
        for (int i = 0; i < result.length; ++i) {
        	
        	Cat cat = new Cat();
        	
        	cat.index = i;
        	cat.score = result[i];
        	
        	results[i] = cat;
        }
        
        Arrays.sort(results);
        
        String[] ret = {"",""};
        for (int i = 0; i < 10; ++i) {
        	
        	Cat cat = results[i];
        	
        	//Log.i("WHATISIT", WhatsApplication.getName(cat.index).split(" ", 2)[1] + " =  " + smallNum(cat.score));
        	ret[1] += BlindApplication.getName(cat.index).split(" ", 2)[1] + " =  " + smallNum(cat.score) + "\n";
        }
        
        String newname = BlindApplication.getName(results[0].index).split(" ", 2)[1];
        
        newname = newname.split(",", 2)[0];
        
        Vibrator v = (Vibrator) BlindTool.activity.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate((long) (500*Math.pow(results[0].score,2)));
        
        
        if (results[0].score > 0.25)
        	ret[0] = newname;
        
        long endtime = System.currentTimeMillis();
        
        Log.i(TAG, "identifyImage took " + (endtime-starttime)/1000.0 + "s");
        
        return ret;
    }
    
	public static String smallNum(double in){
		
		return String.format("%.5f", in);
	}
    
    static class Cat implements Comparable<Cat>{
    	
		
		int index;
    	float score;
    	
		@Override
		public int compareTo(Cat another) {
			
			return Float.valueOf(another.score).compareTo(this.score);
		}
    }
    
}
