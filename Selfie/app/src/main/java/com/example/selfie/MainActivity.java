package com.example.selfie;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Jakub
 * */
public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String currentPhotoPath;
    private Bitmap bitmap;

    /**
     * Create activity and set content view to activity_main.xml
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Find button and add click listener
        Button button = findViewById(R.id.ButtonPhoto);
        button.setOnClickListener(v -> dispatchTakePictureIntent());
    }
    /**
     * On application close or change of activity
     * */
    @Override
    protected void onDestroy()
    {
        deleteRecursive(getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        super.onDestroy();
    }
    /**
     * Only Delete Photo Temp files from application picture folder
     * */
    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    /**
     *  Take Picture Intent
     *  When result code is ok then can set picture and save to gallery
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic();
            galleryAddPic();
        }
    }

    /**
     * Method called when button is pressed
     */
    private void dispatchTakePictureIntent()
    {
        Intent takePicturesIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Check if camera handler is active
        if (takePicturesIntent.resolveActivity(getPackageManager()) != null)
        {
            //Create Image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }catch (IOException ex)
            {
                Log.d("ErrorLog",ex.toString());
            }
            //
            if (photoFile != null)
            {
                Uri photoUri = FileProvider.getUriForFile(this,"com.example.selfie.fileprovider",photoFile);
                takePicturesIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                //Take Picture Intent
                startActivityForResult(takePicturesIntent,REQUEST_IMAGE_CAPTURE);
            }

        }
    }
    /**
     * Set Picture to the Image view in main activity
     * */
    private void setPic() {
        // Get the imageView
        ImageView imageView = findViewById(R.id.imageView);

        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }
    /**
     * Save Picture to gallery
     * With title of JPEG and current date
     */
    private void galleryAddPic()
    {
        MediaStore.Images.Media.insertImage(
                getContentResolver(),
                bitmap,
                "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()),
                "Selfie App Picture"
        );
    }

    /**
     * Save the picture under application picture folder temporary
     *
     * */
    private File createImageFile() throws IOException
    {
        // Create new timeStamp with current date
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName,".jpg",storageDir);

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}