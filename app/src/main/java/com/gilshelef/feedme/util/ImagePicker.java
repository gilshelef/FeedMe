
package com.gilshelef.feedme.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.gilshelef.feedme.R;
import com.mvc.imagepicker.ImageRotator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class ImagePicker {
    public static final int PICK_IMAGE_REQUEST_CODE = 234;
    private static final int DEFAULT_MIN_WIDTH_QUALITY = 400;
    private static final int DEFAULT_MIN_HEIGHT_QUALITY = 400;
    private static final String TAG = ImagePicker.class.getSimpleName();
    private static final String TEMP_IMAGE_NAME = "tempImage";
    private static int minWidthQuality = 400;
    private static int minHeightQuality = 400;

    private ImagePicker() {
    }

    public static void pickImage(Activity activity) {
        String chooserTitle = activity.getString(R.string.pick_image_intent_text);
        pickImage(activity, chooserTitle);
    }

    public static void pickImage(Fragment fragment) {
        String chooserTitle = fragment.getString(R.string.pick_image_intent_text);
        pickImage(fragment, chooserTitle);
    }

    public static void pickImage(Activity activity, String chooserTitle) {
        Intent chooseImageIntent = getPickImageIntent(activity, chooserTitle);
        activity.startActivityForResult(chooseImageIntent, 234);
    }

    public static void pickImage(Fragment fragment, String chooserTitle) {
        Intent chooseImageIntent = getPickImageIntent(fragment.getContext(), chooserTitle);
        fragment.startActivityForResult(chooseImageIntent, 234);
    }

    public static Intent getPickImageIntent(Context context, String chooserTitle) {
        Intent chooserIntent = null;
        ArrayList intentList = new ArrayList();
        Intent pickIntent = new Intent("android.intent.action.PICK", Media.EXTERNAL_CONTENT_URI);
        List intentList1 = addIntentsToList(context, intentList, pickIntent);
        if(!appManifestContainsPermission(context, "android.permission.CAMERA") || hasCameraAccess(context)) {
            Intent takePhotoIntent = new Intent("android.media.action.IMAGE_CAPTURE");
            takePhotoIntent.putExtra("return-data", true);
            takePhotoIntent.putExtra("output", Uri.fromFile(getTemporalFile(context)));
            intentList1 = addIntentsToList(context, intentList1, takePhotoIntent);
        }

        if(intentList1.size() > 0) {
            chooserIntent = Intent.createChooser((Intent)intentList1.remove(intentList1.size() - 1), chooserTitle);
            chooserIntent.putExtra("android.intent.extra.INITIAL_INTENTS", (Parcelable[])intentList1.toArray(new Parcelable[intentList1.size()]));
        }

        return chooserIntent;
    }

    private static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
//        Log.i(TAG, "Adding intents of type: " + intent.getAction());
        List resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        Iterator var4 = resInfo.iterator();

        while(var4.hasNext()) {
            ResolveInfo resolveInfo = (ResolveInfo)var4.next();
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
//            Log.i(TAG, "App package: " + packageName);
        }

        return list;
    }

    private static boolean hasCameraAccess(Context context) {
        return ContextCompat.checkSelfPermission(context, "android.permission.CAMERA") == 0;
    }

    private static boolean appManifestContainsPermission(Context context, String permission) {
        PackageManager pm = context.getPackageManager();

        try {
            PackageInfo e = pm.getPackageInfo(context.getPackageName(), 4096);
            String[] requestedPermissions = null;
            if(e != null) {
                requestedPermissions = e.requestedPermissions;
            }

            if(requestedPermissions == null) {
                return false;
            }

            if(requestedPermissions.length > 0) {
                List requestedPermissionsList = Arrays.asList(requestedPermissions);
                return requestedPermissionsList.contains(permission);
            }
        } catch (NameNotFoundException var6) {
            var6.printStackTrace();
        }

        return false;
    }

    public static Bitmap getImageFromResult(Context context, int requestCode, int resultCode, Intent imageReturnedIntent) {
//        Log.i(TAG, "getImageFromResult() called with: resultCode = [" + resultCode + "]");
        Bitmap bm = null;
        if(resultCode == -1 && requestCode == 234) {
            File imageFile = getTemporalFile(context);
            boolean isCamera = imageReturnedIntent == null || imageReturnedIntent.getData() == null || imageReturnedIntent.getData().toString().contains(imageFile.toString());
            Uri selectedImage;
            if(isCamera) {
                selectedImage = Uri.fromFile(imageFile);
            } else {
                selectedImage = imageReturnedIntent.getData();
            }

//            Log.i(TAG, "selectedImage: " + selectedImage);
            bm = decodeBitmap(context, selectedImage);
            int rotation = ImageRotator.getRotation(context, selectedImage, isCamera);
            bm = ImageRotator.rotate(bm, rotation);
        }

        return bm;
    }

    public static InputStream getInputStreamFromResult(Context context, int requestCode, int resultCode, Intent imageReturnedIntent) {
//        Log.i(TAG, "getFileFromResult() called with: resultCode = [" + resultCode + "]");
        if(resultCode == -1 && requestCode == 234) {
            File imageFile = getTemporalFile(context);
            boolean isCamera = imageReturnedIntent == null || imageReturnedIntent.getData() == null || imageReturnedIntent.getData().toString().contains(imageFile.toString());
            Uri selectedImage;
            if(isCamera) {
                selectedImage = Uri.fromFile(imageFile);
            } else {
                selectedImage = imageReturnedIntent.getData();
            }

//            Log.i(TAG, "selectedImage: " + selectedImage);

            try {
                return (InputStream)(isCamera?new FileInputStream(imageFile):context.getContentResolver().openInputStream(selectedImage));
            } catch (FileNotFoundException var8) {
//                Log.e(TAG, "Could not open input stream for: " + selectedImage);
                return null;
            }
        } else {
            return null;
        }
    }

    private static File getTemporalFile(Context context) {
        return new File(context.getExternalCacheDir(), "tempImage");
    }

    private static Bitmap decodeBitmap(Context context, Uri theUri) {
        Bitmap outputBitmap = null;
        AssetFileDescriptor fileDescriptor = null;

        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(theUri, "r");
            Options e = new Options();
            e.inJustDecodeBounds = true;
            if(fileDescriptor != null) {
                BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), (Rect) null, e);
                int[] sampleSizes = new int[]{8, 4, 2, 1};
                int i = 0;

                int targetWidth;
                int targetHeight;
                do {
                    targetWidth = e.outWidth / sampleSizes[i];
                    targetHeight = e.outHeight / sampleSizes[i];
                    ++i;
                }
                while (i < sampleSizes.length && (targetWidth < minWidthQuality || targetHeight < minHeightQuality));

                if (i == sampleSizes.length) {
//                    Log.d(TAG, "i = sample size length!");
                    i--;
                }

                Options decodeOptions = new Options();
                decodeOptions.inSampleSize = sampleSizes[i];
                outputBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), (Rect) null, decodeOptions);
                if (outputBitmap != null) {
//                    Log.i(TAG, "Loaded image with sample size " + decodeOptions.inSampleSize + "\t\tBitmap width: " + outputBitmap.getWidth() + "\theight: " + outputBitmap.getHeight());
                }

                fileDescriptor.close();
            }
        } catch (FileNotFoundException var10) {
            var10.printStackTrace();
        } catch (IOException var11) {
            var11.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        }

        return outputBitmap;
    }

    public static void setMinQuality(int minWidthQuality, int minHeightQuality) {
        ImagePicker.minWidthQuality = minWidthQuality;
        ImagePicker.minHeightQuality = minHeightQuality;
    }
}
