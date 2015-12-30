package the.blindtool;

import java.lang.Thread.UncaughtExceptionHandler;

public class UnexpectedTerminationHelper {
	
    private Thread mThread;
    private Thread.UncaughtExceptionHandler mOldUncaughtExceptionHandler = null;
    private Thread.UncaughtExceptionHandler mUncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
    	
        @Override
        public void uncaughtException(Thread thread, Throwable ex) { // gets called on the same (main) thread

//        	if (BlindTool.mCamera != null){
//        		BlindTool.mCamera.stopPreview();
//        		BlindTool.mCamera.release();
//        		BlindTool.mCamera = null;
//        	}

			if (mOldUncaughtExceptionHandler != null) {
				// it displays the "force close" dialog
				mOldUncaughtExceptionHandler.uncaughtException(thread, ex);
			}
		}
	};

	void init() {
		mThread = Thread.currentThread();
		mOldUncaughtExceptionHandler = mThread.getUncaughtExceptionHandler();
		if (mUncaughtExceptionHandler != null)
			mThread.setUncaughtExceptionHandler(mUncaughtExceptionHandler);
	}

	void fini() {

		if (mOldUncaughtExceptionHandler != null) {
			mThread.setUncaughtExceptionHandler(mOldUncaughtExceptionHandler);
		}
		mOldUncaughtExceptionHandler = null;
		mThread = null;
	}
}