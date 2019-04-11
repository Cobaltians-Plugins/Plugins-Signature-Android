package io.kristal.signature;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtils {

    private static final String TAG = ImageUtils.class.getSimpleName();
    private static final int BYTE_LIMIT = 2097152;  // 2MB
    private static final int NUMBER_OF_RESIZE_ATTEMPTS = 4;

    //Create Imagefile at Directory, with Extension
    public static final File createImageFile(File directory, String extension){
        Date now = new Date();
        String filename = "IMG_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(now)
                + "-" + now.getTime();
        try {
            // / Make sure the Pictures directory exists.
            return File.createTempFile(filename,    /* prefix */
                    extension,                      /* suffix */
                    directory);                     /* directory */
        }
        catch (IOException exception) {
            Log.e(TAG, "createImageFile: " + directory.getAbsolutePath() + "/" + filename
                    + extension + " could not be created.", exception);
            return null;
        }
        catch (SecurityException exception) {
            Log.e(TAG, "createImageFile: " + directory.getAbsolutePath() + "/" + filename
                    + extension + " could not be created.", exception);
            return null;
        }
    }

    //Save a Bitmap to JPG at defined path with compressRate
    public static final void bmpToJpg(Bitmap bitmap, int compressRate, File path){
        try {
            FileOutputStream mFileOutStream = new FileOutputStream(path);

            bitmap.compress(Bitmap.CompressFormat.JPEG, compressRate, mFileOutStream);
            mFileOutStream.flush();
            mFileOutStream.close();
        } catch (IOException exception) {
            Log.e(TAG, "toJpg: cannot save bitmap at " + path, exception);
        }
    }

    //generate a base64 file
    public static final String bmpToBase64(Bitmap bitmap, int compressRate){
        String base64 = "";
        ByteArrayOutputStream baos = null;
        int attempts = 1;
        int quality = compressRate;
        int base64Length = BYTE_LIMIT;
        // Need this loop cause with some new device the max length of url is 2M characters (2097152 bytes)
        // In this loop, we attempt to compress/resize the content to fit this limit.
        do {
            quality = quality * BYTE_LIMIT / base64Length;  // watch for int division!
            Log.v(TAG, "Base64ImageAtPath: compress(2) w/ quality=" + quality);

            try {
                // Compress the image into a JPG. Start with QUALITY.
                // In case that the image byte size is still too large
                // reduce the quality in proportion to the desired byte size.
                if (baos != null) {
                    try {
                        baos.close();
                    }
                    catch (IOException exception) {
                        Log.e(TAG, exception.getMessage(), exception);
                    }
                }
                baos = new ByteArrayOutputStream();
                // compressing the image
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                // encode image
                base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
                base64Length = base64.length();
            }
            catch (OutOfMemoryError exception) {
                Log.w(TAG, "Base64ImageAtPath - image too big (OutOfMemoryError), "
                        + "will try with smaller quality, cur quality: " + quality);
                // fall through and keep trying with a smaller quality.
            }
            Log.v(TAG, "attempt=" + attempts
                    + " size=" + base64Length
                    + " quality=" + quality);
            attempts++;
        } while (base64Length > BYTE_LIMIT
                && attempts < NUMBER_OF_RESIZE_ATTEMPTS);

        if (baos != null) {
            try {
                baos.close();
            }
            catch (IOException exception) {
                Log.e(TAG, exception.getMessage(), exception);
            }
        }

        return base64;
    }
}
