package com.tanishqsharma.verseful;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    EditText story;
    EditText author;
    TextView dash;
    Button share;
    LinearLayout canvas;
    LinearLayout parent;

    final int PERMISSION_CODE = 13;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Typeface authorFont = Typeface.createFromAsset(getAssets(), "tangerinebold.ttf");
        Typeface storyFont = Typeface.createFromAsset(getAssets(), "quicksand.ttf");

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.app_unit_id));
        //mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712"); //Test ads
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        story = (EditText) findViewById(R.id.story);
        author = (EditText) findViewById(R.id.author);
        dash = (TextView) findViewById(R.id.dash);
        share = (Button) findViewById(R.id.saveAction);
        canvas = (LinearLayout) findViewById(R.id.canvas);
        parent = (LinearLayout) findViewById(R.id.parent);

        author.setTypeface(authorFont);
        dash.setTypeface(authorFont);
        share.setTypeface(storyFont);
        story.setTypeface(storyFont);

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkEmpty()) {
                    // do nothing
                } else {
                    story.setCursorVisible(false);
                    author.setCursorVisible(false);
                    askForPermission();
                }



            }
        });

        setupSwipeListener(parent);


    }

    private void askForPermission(){

        //asking for permissions
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(MainActivity.this, "Permission required to save quote to gallery!", Toast.LENGTH_SHORT).show();

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_CODE);

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_CODE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else{
            File file = saveBitMap(getApplicationContext(), canvas);    //which view you want to pass that view as parameter
            if (file != null) {
                Toast.makeText(MainActivity.this, "Story saved to the gallery.", Toast.LENGTH_SHORT).show();

                //showing ad after successful generation
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }


            } else {
                Toast.makeText(MainActivity.this, "Could Not Save", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private File saveBitMap(Context context, LinearLayout canvas){
        File pictureFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Verseful");
        if(!pictureFileDir.exists()) {
            boolean isDirectoryCreated = pictureFileDir.mkdirs();
            if(!isDirectoryCreated) {
                return null;
            }
        }

        String filename = pictureFileDir.getPath()+File.separator+System.currentTimeMillis()+".jpg";
        File pictureFile = new File(filename);
        Bitmap bitmap = getBitmapFromView(canvas);

        try{
            pictureFile.createNewFile();
            FileOutputStream oStream = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, oStream);
            oStream.flush();
            oStream.close();
        } catch (IOException e){
            e.printStackTrace();
            Log.d("TAG","Issue saving the image");
        }

        scanGallery(context, pictureFile.getAbsolutePath());

        return pictureFile;

    }

    private Bitmap getBitmapFromView(View view){
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        }   else{
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    private void scanGallery(Context cntx, String path) {
        try {
            MediaScannerConnection.scanFile(cntx, new String[] { path },null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //checking for the empty story
    public boolean checkEmpty(){
        if(story.getText().toString().length()<1)
        {
            Toast.makeText(this, "Hey, what's your story?", Toast.LENGTH_SHORT).show();
            return true;
        }
        else {
            return false;
        }
    }


    private void setupSwipeListener(LinearLayout layout){
        layout.setOnTouchListener(new OnSwipeTouchListener(this) {
            public void onSwipeTop() {
                //Toast.makeText(getApplicationContext(), "top", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeRight() {
                //Toast.makeText(getApplicationContext(), "right", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeLeft() {
                //Toast.makeText(getApplicationContext(), "left", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeBottom() {
                //Toast.makeText(getApplicationContext(), "bottom", Toast.LENGTH_SHORT).show();
            }

        });
    }

    //handling the response of permissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    File file = saveBitMap(getApplicationContext(), canvas);    //which view you want to pass that view as parameter
                    if (file != null) {
                        Toast.makeText(MainActivity.this, "Story saved to the gallery.", Toast.LENGTH_SHORT).show();

                        //showing ad after successful generation
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            Log.d("TAG", "The interstitial wasn't loaded yet.");
                        }

                    } else {
                        Toast.makeText(MainActivity.this, "Could Not Save", Toast.LENGTH_SHORT).show();
                    }

                } else {

                    Toast.makeText(this, "You can change that from Settings > Apps > Verseful > Permissions", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


}
