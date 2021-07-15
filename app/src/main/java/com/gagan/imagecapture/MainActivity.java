package com.gagan.imagecapture;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PICTURE_CAPTURE = 100;
    TextView tvCaptureImage;
    ImageView imgDisplayImage;
    EditText etWidth, etHeight, etQuality;
    File file = null;
    private Uri uri, localUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCaptureImage = findViewById(R.id.tvCaptureImage);
        imgDisplayImage = findViewById(R.id.imgDisplayImage);

        etWidth = findViewById(R.id.etWidth);
        etHeight = findViewById(R.id.etHeight);
        etQuality = findViewById(R.id.etQuality);

        tvCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
    }

    private void openCamera() {
        file = getFilePath();
        localUri = getImageUrl(file);

        Log.e("MainActivity", "file path " + file.getPath());
        Log.e("MainActivity", "file path from uri " + localUri.getPath());


        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoURI = getOutputMediaFileUri(this, file);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(cameraIntent, REQUEST_PICTURE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICTURE_CAPTURE && resultCode == Activity.RESULT_OK) {
            try {
                Bitmap bitmap_local = null;
                //Uri localUri_local = null;

                if (uri != null && uri.getPath().length() > 0) {
                    deleteFiles(uri.getPath());
                }
                //bitmap_local = (Bitmap) data.getExtras().get("data");

                String filePath = file.getPath();
                bitmap_local = BitmapFactory.decodeFile(filePath);


                /*if (localUri_local != null && localUri_local.getPath().length() > 0) {
                    deleteFiles(localUri_local.getPath());
                }*/
                //localUri_local = mCreateURI(bitmap_local);

                Bitmap photo = mRotateImage(this, bitmap_local, localUri, 2);
                imgDisplayImage.setImageBitmap(photo);

                //displayImage(this, file, imgDisplayImage);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private File getFilePath() {
        //String extr = Environment.getExternalStorageDirectory().toString() + File.separator + "SVAT Images";
        String extr = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
                + File.separator + "SVATImageFolder";
        SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
        String dateFormate = s.format(new Date());

        File file = new File(extr);
        if (!file.exists()) {
            file.mkdir();
        }
        File imageFile = new File(extr, "/" + "svat_image" + dateFormate + ".jpg");
        return imageFile;
    }

    private Uri getImageUrl(File file) {
        Uri uri = null;
        uri = Uri.fromFile(file);
        return uri;
    }

    private Bitmap mRotateImage(Activity activity, Bitmap bitmap, Uri imageUri, int indicator) {
        int rotate = 0;
        Bitmap scaledBitmap = null;
        File imageFile = null;

        try {
            //getContentResolver().notifyChange(imageUri, null);
            imageFile = new File(imageUri.getPath());

            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

//            Bitmap scaledBitmap_old = Bitmap.createScaledBitmap(bitmap, 480, 640, true);

            Matrix matrix = new Matrix();
            matrix.postRotate(rotate);

            scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            int width = 0;
            int height = 0;

            if (!TextUtils.isEmpty(etWidth.getText().toString())) {
                width = Integer.parseInt(etWidth.getText().toString());
            }

            if (!TextUtils.isEmpty(etHeight.getText().toString())) {
                height = Integer.parseInt(etHeight.getText().toString());
            }

            if (width != 0 && height != 0) {
                scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            } else {
                scaledBitmap = resizeBitmap(scaledBitmap, 1000);
            }

            FileOutputStream fos = null;
            if (indicator == 2) {
                imageFile = file;
                fos = new FileOutputStream(imageFile);
                //deleteFiles(imageUri.getPath());
            }

            int Quality = 100;
            if (!TextUtils.isEmpty(etQuality.getText().toString())) {
                Quality = Integer.parseInt(etQuality.getText().toString());
            }

            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, Quality, fos);
            fos.flush();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

//        return Uri.fromFile(imageFile);
        return scaledBitmap;
    }

    public Bitmap resizeBitmap(Bitmap source, int maxLength) {
        try {
            if (source.getHeight() >= source.getWidth()) {
                int targetHeight = maxLength;
                if (source.getHeight() <= targetHeight) { // if image already smaller than the required height
                    return source;
                }

                double aspectRatio = (double) source.getWidth() / (double) source.getHeight();
                int targetWidth = (int) (targetHeight * aspectRatio);

                Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
                if (result != source) {
                }
                return result;
            } else {
                int targetWidth = maxLength;

                if (source.getWidth() <= targetWidth) { // if image already smaller than the required height
                    return source;
                }

                double aspectRatio = ((double) source.getHeight()) / ((double) source.getWidth());
                int targetHeight = (int) (targetWidth * aspectRatio);

                Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
                if (result != source) {
                }
                return result;

            }
        } catch (Exception e) {
            return source;
        }
    }

    private Uri getOutputMediaFileUri(Context context, File file) {
        return FileProvider.getUriForFile(
                context, context.getPackageName() + ".provider", file
        );
    }

    private void deleteFiles(String path) {
        try {
            File file = new File(path);
            boolean deleted = file.delete();
        } catch (Exception e) {

        }
    }


    public Bitmap resizeBitmap(String photoPath, int targetW, int targetH) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true; //Deprecated API 21

        return BitmapFactory.decodeFile(photoPath, bmOptions);
    }

    private Bitmap getResizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float bitmapRatio = (width / height);
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private Uri mCreateURI(Bitmap bitmap) {
        Uri uri = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            uri = Uri.fromFile(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return uri;

    }
}