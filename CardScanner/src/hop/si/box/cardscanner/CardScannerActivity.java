package hop.si.box.cardscanner;

import hop.si.box.cardscanner.util.PictureManipulator;
import hop.si.box.cardscanner.util.PictureSaver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;


/**
 * Main class for the app.
 * 1. Starts the back-side-camera when the app is started (needs the preview class for that).
 * 2. Attaches an preview object to the gui.
 * 3. A picture is taken whenever the user touches the display. (finally: A pic should be taken every x seconds.)
 *  * 
 * @author Basti Hoffmeister
 *
 */

public class CardScannerActivity extends Activity  {

	private Camera cam; // The camera to use. Initialized by initCamera
	public int cameraNo; // Which camera is used?
	private String LOG_TAG = "CardScannerActivity";
	private Preview mPreview; // Preview object. Used for the display.
	public static final int MEDIA_TYPE_IMAGE = 1; // 

	/**
	 * Callback to recognize whenever a picture is taken.
	 * Uses the class PictureSaver to create a new file where the picture is stored.
	 */
	private PictureCallback mPicture = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			File pictureFile = PictureSaver.getOutputMediaFile(MEDIA_TYPE_IMAGE);
			
			if (pictureFile == null){
	            Log.d(LOG_TAG, "Error creating media file, check storage permissions: ");
	            return;
	        }

	        try {
	        	Bitmap bitmapPicture = BitmapFactory.decodeByteArray(data, 0, data.length);
	        	Bitmap small = PictureManipulator.crop(bitmapPicture, bitmapPicture.getWidth(), 200);
	        	 
	            FileOutputStream fos = new FileOutputStream(pictureFile);
	            small.compress(Bitmap.CompressFormat.PNG, 90, fos);
	            fos.close();
	        } catch (FileNotFoundException e) {
	            Log.d(LOG_TAG, "File not found: " + e.getMessage());
	        } catch (IOException e) {
	            Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
	        }

		}
	};

	/**
	 * Initializing the app. Get the camera -> setup the preview (GUI). 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_card_scanner);

		initCamera();

		mPreview = new Preview(this, cam);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);

		// Add a listener to the surface
		preview.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// get an image from the camera
				cam.takePicture(null, null, mPicture);
			}
		});
	}

	/**
	 * Initialize the camera. Checks all cameras until a back-side-camera is found.
	 * The public field <b> cam </b> stores the used camera.
	 * 
	 * @return true if a camera could be opened.
	 */
	private boolean initCamera() {

		int nCams = Camera.getNumberOfCameras();

		if (nCams == 1) { // If there is only one camera try to open it
			try {
				cam = Camera.open();
				cameraNo = 0;
				configureCamera(cam);
				
				return true;
			} catch (Exception e) {
				Log.e(LOG_TAG,
						"Error when opening camera." + e.getLocalizedMessage());
				e.printStackTrace();
				return false;
			}
		} else { // open the first back-facing camera
			int i = 0;
			while (i < nCams - 1) {
				if (isBackFacing(i)) {
					try {
						cam = Camera.open(i);
						cameraNo = i-1;
						configureCamera(cam);
						
						return true; // if camera can be opend -> finish, else
										// go on
					} catch (Exception e) {
						Log.e(LOG_TAG, "Error when opening camera no " + i
								+ " " + e.getLocalizedMessage());
					}
				}
				i++;
			}
		}
		return true;
	}

	
	/**
	 * Define additional settings for the camera.
	 */
	private void configureCamera(Camera camera) {
		// Use Autofocus
		Camera.Parameters parameters = camera.getParameters();
		List<String> focusModes = parameters.getSupportedFocusModes();
		if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
		{
		    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		}
		
		// Set the picture resolution to xxx (some resolution that works good for ocr)
		List<Camera.Size> supportedResolutions = parameters.getSupportedPictureSizes();
		int resolution = 12;
		parameters.setPictureSize(supportedResolutions.get(resolution).width, supportedResolutions.get(resolution).height);
		// Log.i("Configure Camera", "Picture resolution is " + supportedResolutions.get(resolution).width + " width and " + supportedResolutions.get(resolution).height + " height.");
	
		// Need to configure the orientation as else the picture orientation != what you see.
		int orientation = 0;
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
	    android.hardware.Camera.getCameraInfo(cameraNo, info);
	    orientation = (orientation + 45) / 90 * 90;
		int rotation = (info.orientation + orientation) % 360; // Rotation needed for the back-facing camera
		parameters.setRotation(rotation);
		
		camera.setParameters(parameters);	
	}
	
	
	/**
	 * Check if a camera is front or back-facing. We need the back facing
	 * camera.
	 */
	private boolean isBackFacing(int cameraNo) {

		CameraInfo cameraInfo = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraNo, cameraInfo);
		if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
			return false;
		return true;
	}

	/**
	 * Release the camera. Do this whenever the app closes or is not in focus
	 * any more.
	 */
	private void releaseCamera() {
		if (cam != null) {
			cam.release();
			cam = null;
		}
	}

	
	/**
	 * Release the camera whenever the user leaves the app. (IMPORTANT! Else a restart is needed to open the camera again.)
	 */
	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera();
	}

}
