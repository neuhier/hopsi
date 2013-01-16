package hop.si.box.cardscanner;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Preview extends SurfaceView implements SurfaceHolder.Callback, AutoFocusCallback{
	
	private SurfaceHolder mHolder;
	private Camera mCamera;
	private String LOG_TAG = "Preview";

	public Preview (Context context, Camera camera) {
		super(context);
		mCamera = camera;
		
		mHolder = getHolder(); // Install a surface callback, so that we are notified when underlying surface is destroyed.
		mHolder.addCallback(this);
		
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // needed in Android lower version 3.0
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error setting camera preview: " + e.getMessage());
        }
	}
	
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }
    
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        mCamera.setDisplayOrientation(90);

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            mCamera.autoFocus(this);

        } catch (Exception e){
            Log.d(LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

	@Override
	public void onAutoFocus(boolean success, Camera camera) {

		if(success) {
			Log.d("Preview", "Autofocus done.");
		}
	}
    
}
