package de.elanev.studip.android.app.frontend.localization;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import de.elanev.studip.android.app.R;
import de.elanev.studip.android.app.backend.db.UsersContract;
import de.elanev.studip.android.app.widget.UserDetailsActivity;

/**
 * Created by Nils on 08.10.2015.
 */
public class DetailViewOnTouchListener implements View.OnTouchListener {

    private static String TAG = "DetailViewOnTouchListener";

    private boolean isOnLongClick;
    private float mDownX;
    private float mDownY;
    final float SCROLL_THRESHOLD = 20;

    public MapBuilder mapBuilder;
    public Context context;

    final Handler handler = new Handler();
    Runnable mLongPressed = new Runnable() {
        public void run() {
            isOnLongClick = true;
        }
    };


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            Log.d(TAG, "Handler is online...");
            mDownX = event.getX();
            mDownY = event.getY();
            isOnLongClick = false;
            handler.postDelayed(mLongPressed, 500);
        }
        if(isMove(event, mDownX, mDownY) || event.getAction() == MotionEvent.ACTION_UP) {
            //Log.d(TAG, "HAH! MOVED!");
            handler.removeCallbacks(mLongPressed);
            isOnLongClick = false;
        }
        if(isOnLongClick) {
            Log.d(TAG, "Handling the Click!");
            handleClick(event, v);
            isOnLongClick = false;
        }
        return true;

    }

    /**
     * This Method decides whether an event is a move
     * (like a swype) or a single touch event
     * @param event The MotionEvent to be considered
     * @param x_orig The Original x-Coordinate of the Event
     * @param y_orig The Original y-Coordinate of the Event
     * @return true, if Event is a move, false if event is "static" touch
     */
    private boolean isMove(MotionEvent event, float x_orig, float y_orig) {
        if(event.getAction() == MotionEvent.ACTION_MOVE) {
            if ((Math.abs(x_orig - event.getX()) > SCROLL_THRESHOLD
                    || Math.abs(y_orig - event.getY()) > SCROLL_THRESHOLD)) {
                return true; //distance to origin is too big
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void handleClick(MotionEvent me, View view) {
        /*
        Log.d(TAG, "Handling click...");

        //The raw click position
        double x = (double) me.getX();
        double y = (double) me.getY();
        //Log.d(TAG, "Raw Click at: (" + x + ";" + y +")");



        //Get map-image, drawable, bounds/size
        TouchImageView imageView = mapBuilder.getImageView();
        Drawable map_picture = imageView.getDrawable();
        Rect map_bounds = map_picture.getBounds();


        //get imageView Location on screen (upper left corner)
        int[] location = new int[2];
        imageView.getLocationOnScreen(location);
        float imageViewY = location[1];
        float imageViewX = location[0];
        //Log.d(TAG, "Top left Corner: (" + imageViewX + ";" + imageViewY + ")");

        //since it's a touchimageview, there is zoom and a scaling
        float imageScaleX = imageView.getScaleX();
        float imageScaleY = imageView.getScaleY();

        float zoom = imageView.getCurrentZoom();

        //Log.d(TAG, "Zoom: " + zoom);


        //calculating the scaling factor
        //circles are drawn at raw coords of drawable
        //drawable has other size than screen width/height
        //double some_scale_x = (double) imageView.getWidth() / (double) imageView.getDrawable().getIntrinsicWidth();
        //double some_scale_y = (double) imageView.getHeight() / (double) imageView.getDrawable().getIntrinsicHeight();
        //TEST
        double some_scale_x = (double) imageView.getWidth() / (double) imageView.getDrawable().getIntrinsicWidth();
        double some_scale_y = (double) imageView.getHeight() / (double) imageView.getDrawable().getIntrinsicHeight();
        //Log.d(TAG, "Scaling Factors: (" + some_scale_x + ";" + some_scale_y + ")");

        //Log.d(TAG, "Top left corner of picture at (" + imageViewX + ";" + imageViewY + ")");
        //Log.d(TAG, "Scaling is: (" + imageScaleX + ";" + imageScaleY + ")");
        //Log.d(TAG, "Some Scaling is: (" + some_scale_x + ";" + some_scale_y + ")");



        //calculating the coords of the event transformed into the map-image
        */

        //x = (x - (imageViewX /** some_scale_x*/)) / (some_scale_x /** zoom*/);
        //y = (y - (imageViewY /** some_scale_y*/)) / (some_scale_y /** zoom*/);


        /*
        //getting scrolling stuff
        PointF pos = imageView.getScrollPosition();
        float pos_x = pos.x;
        float pos_y = pos.y;
        //Log.d(TAG, "Scroll position: (" + pos_x + ":" + pos_y + ")");

        float draw_center_x = pos_x * imageView.getWidth();
        float draw_center_y = pos_y * imageView.getHeight();

        float map_bounds_center_x = map_bounds.centerX();
        float map_bounds_center_y = map_bounds.centerY();
        float map_bounds_width = map_bounds.width();
        float map_bounds_height = map_bounds.height();

        /*Log.d(TAG, "Scroll position (absolute): (" + draw_center_x + ":" + draw_center_y + ")");
        Log.d(TAG, "Map bounds center: (" + map_bounds_center_x + ":" + map_bounds_center_y + ")");
        Log.d(TAG, "Map bounds size        : " + map_bounds_width + " X " + map_bounds_height + ")");

        Log.d(TAG, "ImageView size drawable: " + imageView.getDrawable().getIntrinsicWidth() + " X " + imageView.getDrawable().getIntrinsicHeight());
        Log.d(TAG, "ImageView size normal  : " + imageView.getWidth() + " X " + imageView.getHeight() + ")");
        Log.d(TAG, "ImageView measuredWidth: " + imageView.getMeasuredWidth());
        Log.d(TAG, "ImageView measuredHeight" + imageView.getMeasuredHeight());
        Log.d(TAG, "ImageView ImageMatrix " + imageView.getImageMatrix().toShortString());
        */


        /*
        float bitmapDrawableHeight = ((BitmapDrawable) imageView.getDrawable()).getBitmap().getHeight();
        float bitmapDrawableWidth = ((BitmapDrawable) imageView.getDrawable()).getBitmap().getWidth();
        Log.d(TAG, "ImageView size bitmap: " + bitmapDrawableWidth + " X " + bitmapDrawableHeight);
        */

        double x = 0, y = 0;

        //first, get all necessary sizes

        TouchImageView imageView = mapBuilder.getImageView();

        double iW = imageView.getDrawable().getIntrinsicWidth();
        double iH = imageView.getDrawable().getIntrinsicHeight();

        double iX = imageView.getWidth();
        double iY = imageView.getHeight();

        double s;
        double siX = 0, siY = 0;
        double zsiX = 0, zsiY = 0;
        double cX, cY;

        int[] location = new int[2];
        imageView.getLocationOnScreen(location);
        double topY = location[1];
        double topX = location[0];

        WindowManager wm = (WindowManager) view.getContext().getSystemService(view.getContext().WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int sX = size.x;
        int sY = size.y;

        double zoom = imageView.getCurrentZoom();
        RectF zoomedRect = imageView.getZoomedRect();

        double zoomedTop = zoomedRect.top * iH;
        double zoomedLeft = zoomedRect.left * iW;

        //calculating stuff now

        boolean isHorizontal = false;
        if(sX < sY) {
            isHorizontal = false;
        } else {
            isHorizontal = true;
        }

        if(isHorizontal) {
            Log.d(TAG, "horizontal");
            s = iH/iY;
            siX = iW/s;
            cY = 0;
            zsiX = siX * zoom;
            if(iX >= zsiX) {
                cX = 0.5 * (iX - siX);
            } else {
                cX = 0;
            }
        } else {
            Log.d(TAG, "vertical");
            s = iW/iX;
            siY = iH/s;
            cX = 0;
            zsiY = siY * zoom;
            if(iY >= zsiY) {
                cY = 0.5 * (iY - siY);
            } else {
                cY = 0;
            }
        }

        //Log.d(TAG, "All X variables: " + iW + "," + iX + "," + siX + "," + cX + "," + topX + "," + sX);
        //Log.d(TAG, "All Y variables: " + iH + "," + iY + "," + siY + "," + cY + "," + topY + "," + sY);
        //Log.d(TAG, "scaling: " + s);
        //Log.d(TAG, "Zoom is: " + zoom);
        //Log.d(TAG, "Zoomed Rect is: " + zoomedRect.width() + "," + zoomedRect.height() + ")");
        //Log.d(TAG, "Zoomed Rect top/left is: " + zoomedTop + "," + zoomedLeft + ")");

        //cX,cY should be top left corner

        //Log.d(TAG, "Top is: (" + cX + ";" + cY + ")");

        //The raw click position
        double clickedX = (double) me.getX();
        double clickedY = (double) me.getY();

        double alteredClickedX = clickedX;// + topX;
        double alteredClickedY = clickedY;// + topY;

        x = (((clickedX / zoom) - cX) * s) + zoomedLeft;
        y = (((clickedY /zoom) - cY) * s) + zoomedTop;

        //Log.d(TAG, "Raw click:       (" + clickedX + ";" + clickedY + ")");
        //Log.d(TAG, "Converted click: (" + x + ";" + y + ")");



        for(int i = mapBuilder.getLowerRange(); i <= mapBuilder.getUpperRange(); i++) {
            //Log.d(TAG, "--------------------");
            //Log.d(TAG, mapBuilder.getMapFields()[i].name);
            if(/*mapBuilder.getMapFields()[i].name == "129"*/true) {
                //Log.d(TAG, "Fieldname = " + mapBuilder.getMapFields()[i].id);
                //Log.d(TAG, "Field x: " + mapBuilder.getMapFields()[i].map_x + "; Event x: " + ((int) x));
                //Log.d(TAG, "Field y: " + mapBuilder.getMapFields()[i].map_y + "; Event y: " + ((int) y));
            }
            if(mapBuilder.getMapFields()[i].isInside((int) x, (int) y)) {
                //Log.d(TAG, "Field id is: " + mapBuilder.getMapFields()[i].id);
                //Toast.makeText(view.getContext(), "Bei " + mapBuilder.getMapFields()[i].id + " geklickt",
                //        Toast.LENGTH_SHORT);

                Log.d(TAG, "Field x: " + mapBuilder.getMapFields()[i].map_x + "; Event x: " + ((int) x));
                Log.d(TAG, "Field y: " + mapBuilder.getMapFields()[i].map_y + "; Event y: " + ((int) y));
                Log.d(TAG, "Field is: " + mapBuilder.getMapFields()[i].id);
                Log.d(TAG, "Field is: " + mapBuilder.getMapFields()[i].name);


                final String[] people_names = new String[mapBuilder.getMapFields()[i].getPeople().size()];
                final String[] people_ids = new String[mapBuilder.getMapFields()[i].getPeople().size()];

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle(mapBuilder.getMapFields()[i].bezeichnung);
                for(int j = 0; j < mapBuilder.getMapFields()[i].getPeople().size(); j++) {
                    people_names[j] = mapBuilder.getMapFields()[i].getPeople().get(j).getName();
                    people_ids[j] = mapBuilder.getMapFields()[i].getPeople().get(j).getId();
                }

                final Context ctx = context;

                builder.setItems(people_names, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "Item selected!: " + i);
                        //ImageView icon = (ImageView) caller.findViewById(R.id.user_image);
                        if (people_ids[i] != null) {
                            Log.d(TAG, "id to be shown: " + people_ids[i]);
                            Intent intent = new Intent(ctx, UserDetailsActivity.class);
                            intent.putExtra(UsersContract.Columns.USER_ID, people_ids[i]);
                            ctx.startActivity(intent);
                        }
                    }
                });

                Dialog dialog = builder.create();
                dialog.show();

            }
            //Log.d(TAG, "--------------------");
        }
    }

}
