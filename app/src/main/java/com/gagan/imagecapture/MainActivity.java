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
import android.view.View;
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
    File file = null;
    private Uri uri, localUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCaptureImage = findViewById(R.id.tvCaptureImage);
        imgDisplayImage = findViewById(R.id.imgDisplayImage);

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
                Uri localUri_local = null;

                if (uri != null && uri.getPath().length() > 0) {
                    deleteFiles(uri.getPath());
                }
                //bitmap_local = (Bitmap) data.getExtras().get("data");
                String filePath = file.getPath();
                bitmap_local= BitmapFactory.decodeFile(filePath);

                if (localUri_local != null && localUri_local.getPath().length() > 0) {
                    deleteFiles(localUri_local.getPath());
                }
                //localUri_local = mCreateURI(bitmap_local);

                Bitmap photo =  mRotateImage(this, bitmap_local, localUri, 2);
                imgDisplayImage.setImageBitmap(photo);

                //displayImage(this, file, imgDisplayImage);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void displayImage(Activity activity, File file, ImageView imgDisplayImage) {
        if (file!=null){
            String filePath = file.getPath();
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            imgDisplayImage.setImageBitmap(bitmap);
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
                    bitmap.getWidth(), bitmap.getHeight(),
                    matrix, true);

            /*FileOutputStream fos = null;
            if (indicator == 2) {
                imageFile = file;
                fos = new FileOutputStream(imageFile);
                deleteFiles(imageUri.getPath());
            }*/

            //scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            /*fos.flush();
            fos.close();*/

        } catch (Exception e) {
            e.printStackTrace();
        }

//        return Uri.fromFile(imageFile);
        return scaledBitmap;
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