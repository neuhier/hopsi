package hop.si.box.cardscanner.util;

import android.graphics.Bitmap;

/**
 * Utility class to modify the taken bitmaps to optimize it for OCR.
 * -> Crop the image to the top part of the bitmap. That should contain the cards name if the orientation of the camera is ok.
 * 
 * @author Basti Hoffmeister
 *
 */
public class PictureManipulator {

	public static Bitmap crop(Bitmap bm, int width, int height) {

		// Check the size and orientation of the original bitmap
		Bitmap cropped = Bitmap.createBitmap(bm, 0, 0, width, height);
		
		return cropped;
	}
	
}
