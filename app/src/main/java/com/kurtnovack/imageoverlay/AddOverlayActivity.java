package com.kurtnovack.imageoverlay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class AddOverlayActivity extends ActionBarActivity {
    private static final String BACKGROUND_BITMAP_STORAGE_KEY = "view1bitmap";
    private static final String OVERLAY_BITMAP_STORAGE_KEY = "view2bitmap";

    private String currentPhoto;
    private Bitmap bitmapBackground;
    private Bitmap bitmapOverlay;
    private ImageView imageBackground;
    private ImageView imageOverlay;
    private Button btnAddOverlay;
    private Button btnSave;

    //Some lifecycle callbacks so that the image can survive orientation change
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BACKGROUND_BITMAP_STORAGE_KEY, bitmapBackground);
        outState.putParcelable(OVERLAY_BITMAP_STORAGE_KEY, bitmapOverlay);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        bitmapBackground = savedInstanceState.getParcelable(BACKGROUND_BITMAP_STORAGE_KEY);
        bitmapOverlay = savedInstanceState.getParcelable(OVERLAY_BITMAP_STORAGE_KEY);
        imageBackground.setImageBitmap(bitmapBackground);
        imageOverlay.setImageBitmap(bitmapOverlay);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_overlay);

        Intent intent = getIntent();
        currentPhoto = intent.getStringExtra(MainActivity.SELECTED_IMAGE);

        imageBackground = (ImageView) findViewById(R.id.imgSelected);
        imageOverlay = (ImageView) findViewById(R.id.imgOverlay);

        setPic(currentPhoto);

        btnAddOverlay = (Button) findViewById(R.id.btnAddOverlay);
        btnAddOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOverlay();
            }
        });

        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePhoto();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setPic(String photo) {
        /* Get the size of the ImageView */
        int targetW = imageBackground.getWidth();
        int targetH = imageBackground.getHeight();

        /* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photo, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        /* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }

        /* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        /* Decode the JPEG file into a Bitmap */
        bitmapBackground = BitmapFactory.decodeFile(photo, bmOptions);

        /* Associate the Bitmap to the ImageView */
        imageBackground.setImageBitmap(bitmapBackground);
    }

    private void addOverlay() {
        /* Adds Overlay to second ImageView */
        int targetW = imageBackground.getWidth();
        int targetH = imageBackground.getHeight();

        /* Get the size of the overlay */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(
                AddOverlayActivity.this.getResources(),
                R.drawable.cats_head,
                bmOptions
        );
        int overlayW = bmOptions.outWidth;
        int overlayH = bmOptions.outHeight;

        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(overlayW/targetW, overlayH/targetH);
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        bitmapOverlay = BitmapFactory.decodeResource(
                AddOverlayActivity.this.getResources(),
                R.drawable.cats_head,
                bmOptions
        );

        /* Associate the Bitmap to the overlay ImageView */
        imageOverlay.setImageBitmap(bitmapOverlay);

        btnAddOverlay.setVisibility(View.INVISIBLE);
        btnSave.setVisibility(View.VISIBLE);
    }

    private void savePhoto() {

    }
}
