package com.kurtnovack.imageoverlay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Matrix;

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

    // these matrices will be used to move and zoom image
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    // we can be in one of these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    // remember some things for zooming
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private float[] lastEvent = null;

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
        imageOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return overlayEdits(v, event);
            }
        });

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

    private boolean overlayEdits(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    float dx = event.getX() - start.x;
                    float dy = event.getY() - start.y;
                    matrix.postTranslate(dx, dy);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = (newDist / oldDist);
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                    if (lastEvent != null && event.getPointerCount() == 3) {
                        newRot = rotation(event);
                        float r = newRot - d;
                        float[] values = new float[9];
                        matrix.getValues(values);
                        float tx = values[2];
                        float ty = values[5];
                        float sx = values[0];
                        float xc = (view.getWidth() / 2) * sx;
                        float yc = (view.getHeight() / 2) * sx;
                        matrix.postRotate(r, tx + xc, ty + yc);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix);
        return true;
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * Calculate the degree to be rotated by.
     *
     * @param event
     * @return Degrees
     */
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }
}
