package de.elanev.studip.android.app.frontend.localization;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.method.Touch;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import de.elanev.studip.android.app.util.Prefs;

/**
 * Created by Nils on 15.05.2015.
 */
public class MapBuilder {


    double scale = 2.3;


    private final String TAG = "MapBuilder";
    private Bitmap map;
    private TouchImageView imageView;
    private int x = 100;
    private int y = 50;
    private MapField[] mapFields;
    private int[] range;

    public int own_position;

    public double scale_x;
    public double scale_y;

    public Canvas canvas;

    public Bitmap single_me, single_you;
    public Bitmap double_me, double_you;
    public Bitmap triple_me, triple_you;

    public MapBuilder(TouchImageView iv, Bitmap m, double scale_x, double scale_y) {
        imageView = iv;
        map = m;
        this.scale_x = scale_x;
        this.scale_y = scale_y;
        range = new int[2]; //0: lower mapfields bound, 1 upper bound
        initializeMap(scale_x, scale_y, m);
        own_position = -1;
    }

    public void changeMap(Bitmap m, int id) {
        map = m;

        scale_x = (double) map.getWidth();
        scale_y = (double) map.getHeight();
/*
        for(int i = 0; i < mapFields.length; i++) {
            mapFields[i].deletePeopleFromField();
        }
*/
        if(id == 0) {

            //range[0] = 0;
            //range[1] = 12;
            range[0] = 100;
            range[1] = 107;

            //Log.d(TAG, "bitmap size: " + map.getWidth() + " X " + map.getHeight());

            //mapFields = new MapField[10];
/*
            mapFields[0] = new MapField(0, (int) (0.45 * scale_x), (int) (0.2 * scale_y),"73e9c913981a060e9eaafabdc9423f1e", "Eingang");
            mapFields[1] = new MapField(1, (int) (0.6 * scale_x), (int) (0.12 * scale_y),"ac4c90e751437bd27e40d5539700a38b", "Spinde");
            mapFields[2] = new MapField(2, (int) (0.7 * scale_x), (int) (0.12 * scale_y),"ac4c90e751437bd27e40d5539700a38b", "Spinde");
            mapFields[3] = new MapField(3, (int) (0.85 * scale_x), (int) (0.1 * scale_y),"dac4fb35938edd4bef54f8bca0858d50", "Cafeteria");
            mapFields[4] = new MapField(4, (int) (0.87 * scale_x), (int) (0.2 * scale_y),"dac4fb35938edd4bef54f8bca0858d50", "Cafeteria");
            mapFields[5] = new MapField(5, (int) (0.79 * scale_x), (int) (0.25 * scale_y),"95671820123ad427c2fe13f1924fde99", "Eingang Bibliothek");
            mapFields[6] = new MapField(6, (int) (0.7 * scale_x), (int) (0.3 * scale_y), "0a4dfc70b5870eca832d5a40a08b8686", "WC Herren");
            mapFields[7] = new MapField(7, (int) (0.37 * scale_x), (int) (0.22 * scale_y),"a8b273ecd584a388e1f11bcd2a2263f3", "Flur"); //WC Damen 22/227
            mapFields[8] = new MapField(8, (int) (0.27 * scale_x), (int) (0.22 * scale_y),"afcf6b04c729a511efe09200db01af86", "Flur"); //WC Damen 22/110
            mapFields[9] = new MapField(9, (int) (0.15 * scale_x), (int) (0.25 * scale_y),"fc95270521d98ebeb5a045a90ae9f9dc", "Treppenhaus West"); //Abstellraum B11
            mapFields[10] = new MapField(10, (int) (0.34 * scale_x), (int) (0.17 * scale_y),"c8ee6f4a3fa25735aa4ff41258309c60", "22/E26");
            mapFields[11] = new MapField(11, (int) (0.23 * scale_x), (int) (0.17 * scale_y),"1c7d100a5c0e6176df412d3553548b5e", "22/E25");
            mapFields[12] = new MapField(12, (int) (0.45 * scale_x), (int) (0.6 * scale_y),"73e9c913981a060e9eaafabdc9423f1e", "Eingang");
*/
            /*
            Log.d(TAG, "change map: users in spinde: " + mapFields[1].getPeople().size());
            mapFields[0].setMapXY((int) (0.45 * scale_x), (int) (0.2 * scale_y));
            mapFields[1].setMapXY((int) (0.6 * scale_x), (int) (0.12 * scale_y));
            mapFields[2].setMapXY((int) (0.7 * scale_x), (int) (0.12 * scale_y));
            mapFields[3].setMapXY((int) (0.85 * scale_x), (int) (0.1 * scale_y));
            mapFields[4].setMapXY((int) (0.87 * scale_x), (int) (0.2 * scale_y));
            mapFields[5].setMapXY((int) (0.79 * scale_x), (int) (0.25 * scale_y));
            mapFields[6].setMapXY((int) (0.7 * scale_x), (int) (0.3 * scale_y));
            mapFields[7].setMapXY((int) (0.37 * scale_x), (int) (0.22 * scale_y)); //WC Damen 22/227
            mapFields[8].setMapXY((int) (0.27 * scale_x), (int) (0.22 * scale_y)); //WC Damen 22/110
            mapFields[9].setMapXY((int) (0.15 * scale_x), (int) (0.25 * scale_y)); //Abstellraum B11
            mapFields[10].setMapXY((int) (0.34 * scale_x), (int) (0.145 * scale_y));
            mapFields[11].setMapXY((int) (0.2 * scale_x), (int) (0.145 * scale_y));
            mapFields[12].setMapXY((int) (0.45 * scale_x), (int) (0.62 * scale_y));
            */

            //mapFields[10].setMapXY((int) (0.34 * scale_x), (int) (0.17 * scale_y)); //christoph test



        } else if(id == 1) {

            //mapFields = new MapField[15];
            //upper row
/*            mapFields[0] = new MapField(0, (int) (0.1 * scale_x), (int) (0.11 * scale_y), "22/102");
            mapFields[1] = new MapField(1, (int) (0.22  * scale_x),(int) (0.11 *  scale_y),"22/103");
            mapFields[2] = new MapField(2, (int) (0.3  * scale_x),(int) (0.11 *  scale_y),"22/104");
            mapFields[3] = new MapField(3, (int) (0.42  * scale_x),(int) (0.11 *  scale_y),"22/105");
            mapFields[4] = new MapField(4, (int) (0.55  * scale_x),(int) (0.11 *  scale_y),"22/106");
            mapFields[5] = new MapField(5, (int) (0.7  * scale_x),(int) (0.11 *  scale_y),"22/107");
            mapFields[6] = new MapField(6, (int) (0.85  * scale_x),(int) (0.11 *  scale_y),"22/108");

            //mid row (floor)
            mapFields[7] = new MapField(7, (int) (0.3 *  scale_x),(int) (0.18 *  scale_y),"Flur");
            mapFields[8] = new MapField(8, (int) (0.5 *  scale_x),(int) (0.18 *  scale_y),"Flur");
            mapFields[9] = new MapField(9, (int) (0.7 *  scale_x),(int) (0.18 *  scale_y),"Flur");

            //lower row
            mapFields[10] = new MapField(10, (int) (0.34 *  scale_x),(int) (0.24 *  scale_y),"22/117");
            mapFields[11] = new MapField(11, (int) (0.475 *  scale_x),(int) (0.24 *  scale_y),"22/115");
            mapFields[12] = new MapField(12, (int) (0.535 *  scale_x),(int) (0.24 *  scale_y),"22/114");
            mapFields[13] = new MapField(13, (int) (0.6 *  scale_x),(int) (0.24 *  scale_y),"22/113");
            mapFields[14] = new MapField(14, (int) (0.665 *  scale_x),(int) (0.24 *  scale_y),"22/112");
*/
            //range[0] = 20;
            //range[1] = 34;
            range[0] = 108;
            range[1] = 118;
/*
            mapFields[20] = new MapField(20, (int) (0.1 * scale_x), (int) (0.11 * scale_y), "b30316b6bab6935bf1a162f822e55d66", "22/102");
            mapFields[21] = new MapField(21, (int) (0.22  * scale_x),(int) (0.11 *  scale_y),"e0d7bf89366b891f7776556efcf372cd", "22/103");
            mapFields[22] = new MapField(22, (int) (0.3  * scale_x),(int) (0.11 *  scale_y),"583410f1d1c8d4f20f8c9992cba8d539", "22/104");
            mapFields[23] = new MapField(23, (int) (0.42  * scale_x),(int) (0.11 *  scale_y),"ba4cd0d06b152f2c6af39ab68ef94c8f", "22/105");
            mapFields[24] = new MapField(24, (int) (0.55  * scale_x),(int) (0.11 *  scale_y),"5b7d42e52d16dda0dd58bc5fe2710543","22/106");
            mapFields[25] = new MapField(25, (int) (0.7  * scale_x),(int) (0.11 *  scale_y),"f89c5479904a162a44781f51a96b5e4d","22/107");
            mapFields[26] = new MapField(26, (int) (0.85  * scale_x),(int) (0.11 *  scale_y),"c589258c6e08d4a0e52721245a0c78a2","22/108");

            //mid row (floor)
            mapFields[27] = new MapField(27, (int) (0.3 *  scale_x),(int) (0.18 *  scale_y),"6fdcbcf80d6c4ee4e1684105ce51a07b", "Flur"); //WC-Damen E10
            mapFields[28] = new MapField(28, (int) (0.5 *  scale_x),(int) (0.18 *  scale_y),"85ef9d2498ad6fc995700f3ed9f4dada", "Flur"); //Vorraum WC Damen 118
            mapFields[29] = new MapField(29, (int) (0.7 *  scale_x),(int) (0.18 *  scale_y),"877a5029b59d96053156f0603812e8d1", "Flur"); //WC Herren 216a

            //lower row
            mapFields[30] = new MapField(30, (int) (0.34 *  scale_x),(int) (0.24 *  scale_y),"06055c1eb465a09d562596203b6c9f03","22/117");
            mapFields[31] = new MapField(31, (int) (0.475 *  scale_x),(int) (0.24 *  scale_y),"0a1058117a396f07f37b54c9944c9bf9", "22/115"); //Teeküche
            mapFields[32] = new MapField(32, (int) (0.535 *  scale_x),(int) (0.24 *  scale_y),"6553388f947f35624df89a2e9e733130","22/114");
            mapFields[33] = new MapField(33, (int) (0.6 *  scale_x),(int) (0.24 *  scale_y),"da31ce2135f25e18cb8f700d61cb3ca9","22/113");
            mapFields[34] = new MapField(34, (int) (0.665 *  scale_x),(int) (0.24 *  scale_y),"cae83f57bf04dfe8bd4aeed07420faef","22/112");
*/
            /*
            mapFields[20].setMapXY((int) (0.1 * scale_x), (int) (0.11 * scale_y));
            mapFields[21].setMapXY((int) (0.22  * scale_x),(int) (0.11 *  scale_y));
            mapFields[22].setMapXY((int) (0.3  * scale_x),(int) (0.11 *  scale_y));
            mapFields[23].setMapXY((int) (0.42  * scale_x),(int) (0.11 *  scale_y));
            mapFields[24].setMapXY((int) (0.55  * scale_x),(int) (0.11 *  scale_y));
            mapFields[25].setMapXY((int) (0.7  * scale_x),(int) (0.11 *  scale_y));
            mapFields[26].setMapXY((int) (0.85  * scale_x),(int) (0.11 *  scale_y));

            //mid row (floor)
            mapFields[27].setMapXY((int) (0.3 *  scale_x),(int) (0.18 *  scale_y)); //WC-Damen E10
            mapFields[28].setMapXY((int) (0.5 *  scale_x),(int) (0.18 *  scale_y)); //Vorraum WC Damen 118
            mapFields[29].setMapXY((int) (0.7 *  scale_x),(int) (0.18 *  scale_y)); //WC Herren 216a

            //lower row
            mapFields[30].setMapXY((int) (0.34 *  scale_x),(int) (0.24 *  scale_y));
            mapFields[31].setMapXY((int) (0.475 *  scale_x),(int) (0.24 *  scale_y)); //Teeküche
            mapFields[32].setMapXY((int) (0.535 *  scale_x),(int) (0.24 *  scale_y));
            mapFields[33].setMapXY((int) (0.6 *  scale_x),(int) (0.24 *  scale_y));
            mapFields[34].setMapXY((int) (0.665 *  scale_x),(int) (0.24 *  scale_y));*/

            //Test data
            /*mapFields[0].addToField("Nils Oesting");
            mapFields[6].addToField("Tobias Thelen");
            mapFields[8].addToField("Hans Wurst");
            mapFields[14].addToField("Andre Klaaßen");
            mapFields[14].addToField("Jörn Domnik");*/
        } else if(id == 2) {
            //mapFields = new MapField[33];

            //range[0] = 40;
            //range[1] = 72;
            range[0] = 119;
            range[1] = 129;
/*
            mapFields[40] = new MapField(40, (int) (0.075 * scale_x), (int) (0.15 * scale_y), "22/201");
            mapFields[41] = new MapField(41, (int) (0.13 * scale_x), (int) (0.15 * scale_y), "22/202");
            mapFields[42] = new MapField(42, (int) (0.187 * scale_x), (int) (0.15 * scale_y), "22/203");
            mapFields[43] = new MapField(43, (int) (0.227 * scale_x), (int) (0.17 * scale_y), "22/204");
            mapFields[44] = new MapField(44, (int) (0.276 * scale_x), (int) (0.17 * scale_y), "22/205");
            mapFields[45] = new MapField(45, (int) (0.323 * scale_x), (int) (0.17 * scale_y), "22/206");
            mapFields[46] = new MapField(46, (int) (0.39 * scale_x), (int) (0.17 * scale_y), "22/207");
            mapFields[47] = new MapField(47, (int) (0.44 * scale_x), (int) (0.17 * scale_y), "22/208");
            mapFields[48] = new MapField(48, (int) (0.489 * scale_x), (int) (0.17 * scale_y), "22/209");
            mapFields[49] = new MapField(49, (int) (0.55 * scale_x), (int) (0.17 * scale_y), "22/210");
            mapFields[50] = new MapField(50, (int) (0.63 * scale_x), (int) (0.17 * scale_y), "22/211");
            mapFields[51] = new MapField(51, (int) (0.69 * scale_x), (int) (0.17 * scale_y), "22/212");
            mapFields[52] = new MapField(52, (int) (0.74 * scale_x), (int) (0.17 * scale_y), "22/213");
            mapFields[53] = new MapField(53, (int) (0.788 * scale_x), (int) (0.17 * scale_y), "22/214");
            mapFields[54] = new MapField(54, (int) (0.87 * scale_x), (int) (0.15 * scale_y), "22/215");

            mapFields[55] = new MapField(55, (int) (0.1 * scale_x), (int) (0.24 * scale_y), "22/201a");
            mapFields[56] = new MapField(56, (int) (0.25 * scale_x), (int) (0.24 * scale_y), "Flur 1");
            mapFields[57] = new MapField(57, (int) (0.43 * scale_x), (int) (0.24 * scale_y),"55f2c4a85b17b1b028b7fe3d5eac4d63", "Flur 2"); //22/242
            mapFields[58] = new MapField(58, (int) (0.55 * scale_x), (int) (0.24 * scale_y), "Flur 3");
            mapFields[59] = new MapField(59, (int) (0.717 * scale_x), (int) (0.24 * scale_y), "Flur 4");
            mapFields[60] = new MapField(60, (int) (0.82 * scale_x), (int) (0.26 * scale_y), "Flur 5");

            mapFields[61] = new MapField(61, (int) (0.18 * scale_x), (int) (0.31 * scale_y), "Flur");
            mapFields[62] = new MapField(62, (int) (0.227 * scale_x), (int) (0.31 * scale_y), "Flur");
            mapFields[63] = new MapField(63, (int) (0.276 * scale_x), (int) (0.31 * scale_y), "Flur");
            mapFields[64] = new MapField(64, (int) (0.33 * scale_x), (int) (0.31 * scale_y), "Flur");
            mapFields[65] = new MapField(65, (int) (0.395 * scale_x), (int) (0.31 * scale_y), "Flur");
            mapFields[66] = new MapField(66, (int) (0.443 * scale_x), (int) (0.31 * scale_y), "Flur");
            mapFields[67] = new MapField(67, (int) (0.488 * scale_x), (int) (0.31 * scale_y), "Flur");
            mapFields[68] = new MapField(68, (int) (0.538 * scale_x), (int) (0.31 * scale_y), "Flur");
            mapFields[69] = new MapField(69, (int) (0.58 * scale_x), (int) (0.31 * scale_y), "Flur");
            mapFields[70] = new MapField(70, (int) (0.64 * scale_x), (int) (0.31 * scale_y), "Flur");
            mapFields[71] = new MapField(71, (int) (0.705 * scale_x), (int) (0.31 * scale_y), "Flur");
            mapFields[72] = new MapField(72, (int) (0.77 * scale_x), (int) (0.31 * scale_y), "Flur");
*/
           // mapFields[57].setMapXY((int) (0.43 * scale_x), (int) (0.24 * scale_y));

        } else if(id == 3) {
            //mapFields = new MapField[0];
            range[0] = 80;
            range[1] = 99;
        }


        int area_size = 50;
        for(int i = 0; i < mapFields.length; i++) {
            mapFields[i].setArea(mapFields[i].map_x - area_size, mapFields[i].map_x + area_size,
                    mapFields[i].map_y - area_size, mapFields[i].map_y + area_size);
        }


        drawMap();
    }

    public TouchImageView getImageView() {
        return imageView;
    }


    public void drawMap(/*int[] fields*/) {
        //Log.d(TAG, "drawMap");

        //Getting Size of imageView
        int imageViewHeight = imageView.getHeight();
        int imageViewWidth = imageView.getWidth();

        //Log.d(TAG, "draw map: users in spinde: " + mapFields[1].getPeople().size());

        //Log.d(TAG, "Is christoph there?: " + mapFields[10].getPeople().size());

        //Log.d(TAG, "----");
        //Log.d(TAG, "imageView height: " + imageViewHeight);
        //Log.d(TAG, "imageView width:  " + imageViewWidth);
        //Log.d(TAG, "----");


        //Log.d(TAG, "create overlay");
        Bitmap bmOverlay = Bitmap.createBitmap(map.getWidth(), map.getHeight(), map.getConfig());

        //Log.d(TAG, "create canvas");
        //Canvas canvas = new Canvas(bmOverlay);
        canvas = new Canvas(bmOverlay);

        //Log.d(TAG, "setup for the circles");
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);

        Paint paint_own = new Paint();
        paint_own.setColor(Color.BLUE);
        paint_own.setStrokeWidth(10);
        paint_own.setStyle(Paint.Style.STROKE);

        Paint tutor_paint = new Paint();
        tutor_paint.setColor(Color.BLUE);
 //       tutor_paint.setStrokeWidth(5);
        tutor_paint.setStyle(Paint.Style.FILL);

        //Log.d(TAG, "draw bitmap background (map)");
        canvas.drawBitmap(map, new Matrix(), null);

        //Log.d(TAG, "drawcircles");
        //Log.d(TAG, "fields length = " + String.valueOf(fields.length));
        /*for(int i = 0; i < fields.length; i++) {
            //Log.d(TAG, "drawing circle " + String.valueOf(i));
            if(fields[i] < 15) {
                canvas.drawCircle(mapFields[fields[i]].map_x, mapFields[fields[i]].map_y, 15, paint);
                /*Log.d(TAG, "circle at x: " + mapFields[fields[i]].map_x);
                Log.d(TAG, "circle at y: " + mapFields[fields[i]].map_y);
            }
        }*/
        Log.d(TAG, "lower range: " + getLowerRange());
        Log.d(TAG, "upper range: " + getUpperRange());
        for (int i= getLowerRange(); i <= getUpperRange(); i++) {
            if(mapFields[i].containsTutor()) {
                canvas.drawRect(mapFields[i].area_x_min, mapFields[i].area_y_min,
                        mapFields[i].area_x_max, mapFields[i].area_y_max, tutor_paint);
            }
            switch (mapFields[i].getPeople().size()) {
                case 0: break;
                case 1: canvas.drawBitmap(single_you, mapFields[i].area_x_min, mapFields[i].area_y_min, null);
                    break;
                case 2: canvas.drawBitmap(double_you, mapFields[i].area_x_min, mapFields[i].area_y_min, null);
                    break;
                default: canvas.drawBitmap(triple_you, mapFields[i].area_x_min, mapFields[i].area_y_min, null);
                    break;
            }
            /*
            if(mapFields[i].getPeople().size() > 0) {
                canvas.drawCircle(mapFields[i].map_x, mapFields[i].map_y, 15, paint);
            }
            */
        }


        String user_id = Prefs.getInstance(imageView.getContext()).getUserId();

        // draw me!
        if(own_position >= 0 && own_position >= getLowerRange() && own_position <= getUpperRange()) {
            //canvas.drawCircle(mapFields[fields[0]].map_x, mapFields[fields[0]].map_y, 15, paint_own);
            switch (mapFields[own_position].getPeople().size()) {
                case 0: break;
                case 1: canvas.drawBitmap(single_me, mapFields[own_position].area_x_min, mapFields[own_position].area_y_min, null);
                    break;
                case 2: canvas.drawBitmap(double_me, mapFields[own_position].area_x_min, mapFields[own_position].area_y_min, null);
                    break;
                default: canvas.drawBitmap(triple_me, mapFields[own_position].area_x_min, mapFields[own_position].area_y_min, null);
                    break;
            }
        }
        //canvas.drawCircle( x, y, 30, paint);

        Paint border_paint = new Paint();
        border_paint.setColor(Color.GREEN);
        border_paint.setStrokeWidth(5);
        border_paint.setStyle(Paint.Style.STROKE);


        /*for(int i = range[0]; i <= range[1]; i++) {
            canvas.drawRect(mapFields[i].area_x_min, mapFields[i].area_y_min,
                  mapFields[i].area_x_max, mapFields[i].area_y_max, border_paint);
        }*/


        //Log.d(TAG, "set image");
        imageView.setImageBitmap(bmOverlay);

        //Adding TouchListener to ImageView
        DetailViewOnTouchListener dvotl = new DetailViewOnTouchListener();
        dvotl.mapBuilder = this;
        dvotl.context = imageView.getContext();
        imageView.setOnTouchListener(dvotl);

        //Log.d(TAG, "drawMap done");
    }

/*    public void drawMap() {
        int[] fields = new int[0];
        drawMap(fields);
    }
*/
    public void initializeMap(double s_x, double s_y, Bitmap map) {

        mapFields = new MapField[150];

        range[0] = 0;
        range[1] = 0;

        for(int i = 0; i < mapFields.length; i++) {
            mapFields[i] = new MapField(-2,0,0,"default");
        }

        //Test ViewTreeObserver
        final int[] iv_size = new int[2];
        ViewTreeObserver vto = imageView.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                iv_size[0] = imageView.getMeasuredHeight();
                iv_size[1] = imageView.getMeasuredWidth();
                return true;
            }
        });


        //Next try to handle the scaling
        scale_x = (double) map.getWidth();
        scale_y = (double) map.getHeight();

        int imageViewHeight = iv_size[0];
        int imageViewWidth = iv_size[1];

        /*Log.d(TAG, "----");
        Log.d(TAG, "map height: " + map.getHeight());
        Log.d(TAG, "map width:  " + map.getWidth());
        Log.d(TAG, "----");

        Log.d(TAG, "Scaling Width with: " + scale_x);
        Log.d(TAG, "Scaling Height with: " + scale_y);*/

        //EG

        mapFields[0] = new MapField(0, (int) (0.45 * scale_x), (int) (0.2 * scale_y),"73e9c913981a060e9eaafabdc9423f1e", "Eingang");
        mapFields[1] = new MapField(1, (int) (0.6 * scale_x), (int) (0.12 * scale_y),"ac4c90e751437bd27e40d5539700a38b", "Spinde");
        mapFields[2] = new MapField(2, (int) (0.7 * scale_x), (int) (0.12 * scale_y),"ac4c90e751437bd27e40d5539700a38b", "Spinde");
        mapFields[3] = new MapField(3, (int) (0.85 * scale_x), (int) (0.1 * scale_y),"dac4fb35938edd4bef54f8bca0858d50", "Cafeteria");
        mapFields[4] = new MapField(4, (int) (0.87 * scale_x), (int) (0.2 * scale_y),"eef2c4922af9404c32f09147ef0110e8", "Cafeteria"); //Fachschaft FB 9
        mapFields[5] = new MapField(5, (int) (0.79 * scale_x), (int) (0.25 * scale_y),"95671820123ad427c2fe13f1924fde99", "Eingang Bibliothek");
        mapFields[6] = new MapField(6, (int) (0.7 * scale_x), (int) (0.3 * scale_y), "0a4dfc70b5870eca832d5a40a08b8686", "WC Herren");
        mapFields[7] = new MapField(7, (int) (0.37 * scale_x), (int) (0.22 * scale_y),"a8b273ecd584a388e1f11bcd2a2263f3", "Flur"); //WC Damen 22/227
        mapFields[8] = new MapField(8, (int) (0.27 * scale_x), (int) (0.22 * scale_y),"afcf6b04c729a511efe09200db01af86", "Flur"); //WC Damen 22/110
        mapFields[9] = new MapField(9, (int) (0.15 * scale_x), (int) (0.25 * scale_y),"fc95270521d98ebeb5a045a90ae9f9dc", "Treppenhaus West"); //Abstellraum B11
        mapFields[10] = new MapField(10, (int) (0.34 * scale_x), (int) (0.17 * scale_y),"c8ee6f4a3fa25735aa4ff41258309c60", "22/E26");
        mapFields[11] = new MapField(11, (int) (0.23 * scale_x), (int) (0.17 * scale_y),"1c7d100a5c0e6176df412d3553548b5e", "22/E25");
        mapFields[12] = new MapField(12, (int) (0.45 * scale_x), (int) (0.6 * scale_y),"73e9c913981a060e9eaafabdc9423f1e", "Eingang");



        // 1.OG

        //mapFields = new MapField[15];
        //upper row
        /*mapFields[0] = new MapField(0, (int) (0.1 * scale_x), (int) (0.11 * scale_y), "22/102");
        mapFields[1] = new MapField(1, (int) (0.22  * scale_x),(int) (0.11 *  scale_y),"22/103");
        mapFields[2] = new MapField(2, (int) (0.3  * scale_x),(int) (0.11 *  scale_y),"22/104");
        mapFields[3] = new MapField(3, (int) (0.42  * scale_x),(int) (0.11 *  scale_y),"22/105");
        mapFields[4] = new MapField(4, (int) (0.55  * scale_x),(int) (0.11 *  scale_y),"22/106");
        mapFields[5] = new MapField(5, (int) (0.7  * scale_x),(int) (0.11 *  scale_y),"22/107");
        mapFields[6] = new MapField(6, (int) (0.85  * scale_x),(int) (0.11 *  scale_y),"22/108");

        //mid row (floor)
        mapFields[7] = new MapField(7, (int) (0.3 *  scale_x),(int) (0.18 *  scale_y),"Flur");
        mapFields[8] = new MapField(8, (int) (0.5 *  scale_x),(int) (0.18 *  scale_y),"Flur");
        mapFields[9] = new MapField(9, (int) (0.7 *  scale_x),(int) (0.18 *  scale_y),"Flur");

        //lower row
        mapFields[10] = new MapField(10, (int) (0.34 *  scale_x),(int) (0.24 *  scale_y),"22/117");
        mapFields[11] = new MapField(11, (int) (0.475 *  scale_x),(int) (0.24 *  scale_y),"22/115");
        mapFields[12] = new MapField(12, (int) (0.535 *  scale_x),(int) (0.24 *  scale_y),"22/114");
        mapFields[13] = new MapField(13, (int) (0.6 *  scale_x),(int) (0.24 *  scale_y),"22/113");
        mapFields[14] = new MapField(14, (int) (0.665 *  scale_x),(int) (0.24 *  scale_y),"22/112");
*/
        mapFields[20] = new MapField(20, (int) (0.1 * scale_x), (int) (0.11 * scale_y), "b30316b6bab6935bf1a162f822e55d66", "22/102");
        mapFields[21] = new MapField(21, (int) (0.22  * scale_x),(int) (0.11 *  scale_y),"e0d7bf89366b891f7776556efcf372cd", "22/103");
        mapFields[22] = new MapField(22, (int) (0.3  * scale_x),(int) (0.11 *  scale_y),"583410f1d1c8d4f20f8c9992cba8d539", "22/104");
        mapFields[23] = new MapField(23, (int) (0.42  * scale_x),(int) (0.11 *  scale_y),"ba4cd0d06b152f2c6af39ab68ef94c8f", "22/105");
        mapFields[24] = new MapField(24, (int) (0.55  * scale_x),(int) (0.11 *  scale_y),"5b7d42e52d16dda0dd58bc5fe2710543","22/106");
        mapFields[25] = new MapField(25, (int) (0.7  * scale_x),(int) (0.11 *  scale_y),"f89c5479904a162a44781f51a96b5e4d","22/107");
        mapFields[26] = new MapField(26, (int) (0.85  * scale_x),(int) (0.11 *  scale_y),"c589258c6e08d4a0e52721245a0c78a2","22/108");

        //mid row (floor)
        mapFields[27] = new MapField(27, (int) (0.3 *  scale_x),(int) (0.18 *  scale_y),"6fdcbcf80d6c4ee4e1684105ce51a07b", "Flur"); //WC-Damen E10
        mapFields[28] = new MapField(28, (int) (0.5 *  scale_x),(int) (0.18 *  scale_y),"85ef9d2498ad6fc995700f3ed9f4dada", "Flur"); //Vorraum WC Damen 118
        mapFields[29] = new MapField(29, (int) (0.7 *  scale_x),(int) (0.18 *  scale_y),"877a5029b59d96053156f0603812e8d1", "Flur"); //WC Herren 216a

        //lower row
        mapFields[30] = new MapField(30, (int) (0.34 *  scale_x),(int) (0.24 *  scale_y),"06055c1eb465a09d562596203b6c9f03","22/117");
        mapFields[31] = new MapField(31, (int) (0.475 *  scale_x),(int) (0.24 *  scale_y),"0a1058117a396f07f37b54c9944c9bf9", "22/115"); //Teeküche
        mapFields[32] = new MapField(32, (int) (0.535 *  scale_x),(int) (0.24 *  scale_y),"6553388f947f35624df89a2e9e733130","22/114");
        mapFields[33] = new MapField(33, (int) (0.6 *  scale_x),(int) (0.24 *  scale_y),"da31ce2135f25e18cb8f700d61cb3ca9","22/113");
        mapFields[34] = new MapField(34, (int) (0.665 *  scale_x),(int) (0.24 *  scale_y),"cae83f57bf04dfe8bd4aeed07420faef","22/112");


        //2. OG

        mapFields[40] = new MapField(40, (int) (0.075 * scale_x), (int) (0.15 * scale_y), "22/201");
        mapFields[41] = new MapField(41, (int) (0.13 * scale_x), (int) (0.15 * scale_y), "22/202");
        mapFields[42] = new MapField(42, (int) (0.187 * scale_x), (int) (0.15 * scale_y), "22/203");
        mapFields[43] = new MapField(43, (int) (0.227 * scale_x), (int) (0.17 * scale_y), "22/204");
        mapFields[44] = new MapField(44, (int) (0.276 * scale_x), (int) (0.17 * scale_y), "22/205");
        mapFields[45] = new MapField(45, (int) (0.323 * scale_x), (int) (0.17 * scale_y), "22/206");
        mapFields[46] = new MapField(46, (int) (0.39 * scale_x), (int) (0.17 * scale_y), "22/207");
        mapFields[47] = new MapField(47, (int) (0.44 * scale_x), (int) (0.17 * scale_y), "22/208");
        mapFields[48] = new MapField(48, (int) (0.489 * scale_x), (int) (0.17 * scale_y), "22/209");
        mapFields[49] = new MapField(49, (int) (0.55 * scale_x), (int) (0.17 * scale_y), "22/210");
        mapFields[50] = new MapField(50, (int) (0.63 * scale_x), (int) (0.17 * scale_y), "22/211");
        mapFields[51] = new MapField(51, (int) (0.69 * scale_x), (int) (0.17 * scale_y), "22/212");
        mapFields[52] = new MapField(52, (int) (0.74 * scale_x), (int) (0.17 * scale_y), "22/213");
        mapFields[53] = new MapField(53, (int) (0.788 * scale_x), (int) (0.17 * scale_y), "22/214");
        mapFields[54] = new MapField(54, (int) (0.87 * scale_x), (int) (0.15 * scale_y), "22/215");

        mapFields[55] = new MapField(55, (int) (0.1 * scale_x), (int) (0.24 * scale_y), "22/201a");
        mapFields[56] = new MapField(56, (int) (0.25 * scale_x), (int) (0.24 * scale_y), "Flur 1");
        mapFields[57] = new MapField(57, (int) (0.43 * scale_x), (int) (0.24 * scale_y),"55f2c4a85b17b1b028b7fe3d5eac4d63", "Flur 2"); //22/242
        mapFields[58] = new MapField(58, (int) (0.55 * scale_x), (int) (0.24 * scale_y), "Flur 3");
        mapFields[59] = new MapField(59, (int) (0.717 * scale_x), (int) (0.24 * scale_y), "Flur 4");
        mapFields[60] = new MapField(60, (int) (0.82 * scale_x), (int) (0.26 * scale_y), "Flur 5");

        mapFields[61] = new MapField(61, (int) (0.18 * scale_x), (int) (0.31 * scale_y), "Flur");
        mapFields[62] = new MapField(62, (int) (0.227 * scale_x), (int) (0.31 * scale_y), "Flur");
        mapFields[63] = new MapField(63, (int) (0.276 * scale_x), (int) (0.31 * scale_y), "Flur");
        mapFields[64] = new MapField(64, (int) (0.33 * scale_x), (int) (0.31 * scale_y), "Flur");
        mapFields[65] = new MapField(65, (int) (0.395 * scale_x), (int) (0.31 * scale_y), "Flur");
        mapFields[66] = new MapField(66, (int) (0.443 * scale_x), (int) (0.31 * scale_y), "Flur");
        mapFields[67] = new MapField(67, (int) (0.488 * scale_x), (int) (0.31 * scale_y), "Flur");
        mapFields[68] = new MapField(68, (int) (0.538 * scale_x), (int) (0.31 * scale_y), "Flur");
        mapFields[69] = new MapField(69, (int) (0.58 * scale_x), (int) (0.31 * scale_y), "Flur");
        mapFields[70] = new MapField(70, (int) (0.64 * scale_x), (int) (0.31 * scale_y), "Flur");
        mapFields[71] = new MapField(71, (int) (0.705 * scale_x), (int) (0.31 * scale_y), "Flur");
        mapFields[72] = new MapField(72, (int) (0.77 * scale_x), (int) (0.31 * scale_y), "Flur");

        //virtUOS
        //EG
        mapFields[100] = new MapField(100, (int) (0.6 * scale_x), (int) (0.88 * scale_y), "2fb46e6440fb324e70a28ae06d5707bf", "Treppenhaus");
        mapFields[101] = new MapField(101, (int) (0.6 * scale_x), (int) (0.55 * scale_y), "c1e795ae8ccd7ceda20e8fb7476dc35c", "Flur");
        mapFields[102] = new MapField(102, (int) (0.2 * scale_x), (int) (0.9 * scale_y), "5b0678c40ec2af4a56bbb3f95bc94544", "42/E02");
        mapFields[103] = new MapField(103, (int) (0.35 * scale_x), (int) (0.66 * scale_y), "4693b892602c0896ee5e7aeb0fa7f105", "42/E03");
        mapFields[104] = new MapField(104, (int) (0.35 * scale_x), (int) (0.48 * scale_y), "f0c3cc56cdc41767541b035221c93aa7", "42/E04");
        mapFields[105] = new MapField(105, (int) (0.6 * scale_x), (int) (0.2 * scale_y), "f7834a77dbab55877f469e0ccf5aacac", "42/E05");
        mapFields[106] = new MapField(106, (int) (0.85 * scale_x), (int) (0.55 * scale_y), "de99fc55bd63819300e474e38277c7ef", "42/E06");
        mapFields[107] = new MapField(107, (int) (0.85 * scale_x), (int) (0.9 * scale_y), "717a34ea9a2afd63545b45b20ddbe341", "42/E07");

        //1.OG
        mapFields[108] = new MapField(108, (int) (0.6 * scale_x), (int) (0.88 * scale_y), "cbc49b2e563599b16267006b143f2056", "Treppenhaus 1. OG");
        mapFields[109] = new MapField(109, (int) (0.6 * scale_x), (int) (0.55 * scale_y), "bf1ea1372ba894928ff9a0099d390aa1", "Flur 1. OG");
        mapFields[110] = new MapField(110, (int) (0.35 * scale_x), (int) (0.9 * scale_y), "566f10a709ba632d1d718b37cb5b8230", "42/101");
        mapFields[111] = new MapField(111, (int) (0.35 * scale_x), (int) (0.66 * scale_y), "91cb5b15f3ba2bb8ba37e8d9fc57ffc3", "WC");
        mapFields[112] = new MapField(112, (int) (0.35 * scale_x), (int) (0.45 * scale_y), "ae2a49e164d38167b42e4b279aa856e9", "42/103");
        mapFields[113] = new MapField(113, (int) (0.35 * scale_x), (int) (0.2 * scale_y), "e34e7e7c177364a6ee4153729f03f36b", "42/104a");
        mapFields[114] = new MapField(114, (int) (0.6 * scale_x), (int) (0.2 * scale_y), "04ced017ef2b0964b46a56915076ea3f", "42/104");
        mapFields[115] = new MapField(115, (int) (0.9 * scale_x), (int) (0.2 * scale_y), "c1c1b76d48200645124c918d5716fe07", "42/106");
        mapFields[116] = new MapField(116, (int) (0.9 * scale_x), (int) (0.46 * scale_y), "fa6243a3e08c053f9545d3389b5ad9d3", "42/107");
        mapFields[117] = new MapField(117, (int) (0.9 * scale_x), (int) (0.65 * scale_y), "c850fd1c9ccdd9faba90f4d4d3ac06be", "42/108");
        mapFields[118] = new MapField(118, (int) (0.9 * scale_x), (int) (0.9 * scale_y), "dd5303f27f39972416ea9c3b3301edc7", "42/109");

        //2.OG
        mapFields[119] = new MapField(119, (int) (0.6 * scale_x), (int) (0.88 * scale_y), "4426b7e2bf20dbc0627d231535c887fa", "Treppenhaus 2. OG");
        mapFields[120] = new MapField(120, (int) (0.6 * scale_x), (int) (0.55 * scale_y), "8bde73fcfadcfc78c732d9f9369b913b", "Flur 2. OG");
        mapFields[121] = new MapField(121, (int) (0.35 * scale_x), (int) (0.9 * scale_y), "6742f910d6eded1b6a5f3338de3bc470", "42/201");
        mapFields[122] = new MapField(122, (int) (0.35 * scale_x), (int) (0.66 * scale_y), "bb19a39c271d08645d91d66b7a4c7e5f", "WC");
        mapFields[123] = new MapField(124, (int) (0.35 * scale_x), (int) (0.45 * scale_y), "c735e0d55f9f5b95c68c6f36a617f069", "42/203");
        mapFields[124] = new MapField(123, (int) (0.35 * scale_x), (int) (0.2 * scale_y), "3413e2fcbf558581eb016a8c974678fa", "42/204");
        mapFields[125] = new MapField(125, (int) (0.6 * scale_x), (int) (0.2 * scale_y), "84f6cab220c8c9bfad822d833d9eb554", "42/205");
        mapFields[126] = new MapField(126, (int) (0.9 * scale_x), (int) (0.2 * scale_y), "07501732f15020b180883b9bc4f759b0", "42/206");
        mapFields[127] = new MapField(127, (int) (0.9 * scale_x), (int) (0.46 * scale_y), "e11fa3da2b035692d8f6bb40600224e9", "42/207");
        mapFields[128] = new MapField(128, (int) (0.9 * scale_x), (int) (0.65 * scale_y), "1264c1e140ac41c643370c608d760dd6", "42/208");
        mapFields[129] = new MapField(129, (int) (0.9 * scale_x), (int) (0.9 * scale_y), "7df0e6b5548eedbb0aa87f65fcce91b7", "42/209");


        //
        int areasize = 35;

        for(int i = range[0]; i <= range[1]; i++) {
            mapFields[i].setArea(mapFields[i].map_x - areasize, mapFields[i].map_x + areasize,
                    mapFields[i].map_y - areasize, mapFields[i].map_y + areasize);
        }

        //now drawing a map
        drawMap();

        //Adding some test data
        /*mapFields[0].addToField("Nils Oesting");
        mapFields[6].addToField("Tobias Thelen");
        mapFields[8].addToField("Hans Wurst");
        mapFields[14].addToField("Andre Klaaßen");
        mapFields[14].addToField("Jörn Domnik");*/
    }

    public MapField[] getMapFields() {
        return mapFields;
    }

    public void setMarkerBitmaps(Bitmap sm, Bitmap sy, Bitmap dm, Bitmap dy, Bitmap tm, Bitmap ty) {
        single_me = sm;
        single_you = sy;
        double_me = dm;
        double_you = dy;
        triple_me = tm;
        triple_you = ty;
    }

    public boolean addUser(LocalizedUser localizedUser) {
        //Delete user if exists
        for(int i = 0; i < mapFields.length; i++) {
            if(mapFields[i].containsUser(localizedUser)) {
                mapFields[i].deleteFromField(localizedUser);
            }
        }
        //add than
        for(int i = 0; i < mapFields.length; i++) {
            if(mapFields[i].name.equals(localizedUser.getLocation()) && !mapFields[i].name.equals("default")) {
                mapFields[i].addToField(localizedUser);
                return true;
            } else if(mapFields[i].bezeichnung.equals(localizedUser.getLocation())) {
                mapFields[i].addToField(localizedUser);
            }
        }

        return false;
    }

    public boolean addUser(LocalizedUser user, int field_id) {
        //Delete user if exists
        for(int i = 0; i < mapFields.length; i++) {
            if(mapFields[i].containsUser(user)) {
                mapFields[i].deleteFromField(user);
            }
        }
        mapFields[field_id].addToField(user);
        return true;
    }

    public int getLowerRange() {
        return range[0];
    }

    public int getUpperRange() {
        return range[1];
    }

    public void deleteUser(LocalizedUser localizedUser) {
        for (int i = 0; i < mapFields.length; i++) {
            if(mapFields[i].deleteFromField(localizedUser));
            return;
        }
    }

    public LocalizedUser getLocalizedUser(String userId) {
        for(int i = 0; i < mapFields.length; i++) {
            if(mapFields[i].getLocalizedUser(userId) != null) {
                return mapFields[i].getLocalizedUser(userId);
            }
        }
        return null;
    }

}