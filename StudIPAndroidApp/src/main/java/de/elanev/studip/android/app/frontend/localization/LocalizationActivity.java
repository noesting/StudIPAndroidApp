package de.elanev.studip.android.app.frontend.localization;

import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import de.elanev.studip.android.app.R;
import de.elanev.studip.android.app.StudIPApplication;

public class LocalizationActivity extends Activity {

    public static final String TAG = "LocalizationActivity";

    boolean draw_all = false; //TODO raus damit!!!

    WifiManager mainWifiObj;
    WifiScanReceiver wifiReciever;
    ListView list;
    String wifis[];

    public static final String EXTRA_CURRENT_POSITION = "current_position";


    int current_field_id = -1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);
        list = (ListView)findViewById(R.id.listView1);
        mainWifiObj = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        mainWifiObj.startScan();
    }


    protected void onPause() {
        unregisterReceiver(wifiReciever);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(wifiReciever, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    public void refresh(View view) {
        Log.d(TAG, "refresh start");
        //list = (ListView)findViewById(R.id.listView1);
        //mainWifiObj = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //wifiReciever = new WifiScanReceiver();
        mainWifiObj.startScan();
        Log.d(TAG, "refresh end");
    }

    public void storePlaceToDB(View view) {
        Log.d(TAG, "storePlaceToDB starting ...");

        new AlertDialog.Builder(this)
                .setTitle("Achtung!")
                .setMessage("Wollen sie einen neuen Ort einspeichern?")
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Neuen Ort einspeichern!");
                        storeNewPlaceToDB();
                    }
                })
                .setNegativeButton("Nein", null).show();
    }

    public void storeNewPlaceToDB() {


        final String[] fingerprints = new String[5];

        EditText place_et;
        place_et = (EditText) findViewById(R.id.place_textedit);
        final String place = place_et.getText().toString();

        mainWifiObj.startScan();
        Handler handler0 = new Handler();
        handler0.postDelayed(new Runnable() {
            public void run() {

                fingerprints[0] = getCurrentFingerprintAsString();

                Toast toast = Toast.makeText(getApplicationContext(), "Step 0 of 4 completed",
                        Toast.LENGTH_LONG);
                toast.show();

                mainWifiObj.startScan();
            }
        }, 10000);

        Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            public void run() {

                fingerprints[1] = getCurrentFingerprintAsString();

                Toast toast = Toast.makeText(getApplicationContext(), "Step 1 of 4 completed",
                        Toast.LENGTH_LONG);
                toast.show();

                mainWifiObj.startScan();
            }
        }, 20000);

        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            public void run() {

                fingerprints[2] = getCurrentFingerprintAsString();

                Toast toast = Toast.makeText(getApplicationContext(), "Step 2 of 4 completed",
                        Toast.LENGTH_LONG);
                toast.show();

                mainWifiObj.startScan();
            }
        }, 30000);

        Handler handler3 = new Handler();
        handler3.postDelayed(new Runnable() {
            public void run() {

                fingerprints[3] = getCurrentFingerprintAsString();

                Toast toast = Toast.makeText(getApplicationContext(), "Step 3 of 4 completed",
                        Toast.LENGTH_LONG);
                toast.show();

                mainWifiObj.startScan();
            }
        }, 40000);

        Handler handler4 = new Handler();
        handler4.postDelayed(new Runnable() {
            public void run() {

                fingerprints[4] = getCurrentFingerprintAsString();

                Toast toast = Toast.makeText(getApplicationContext(), "Step 4 of 4 completed",
                        Toast.LENGTH_LONG);
                toast.show();

                mainWifiObj.startScan();
            }
        }, 50000);

        final Place dummy = new Place(-1, "bssid,1;");

        Handler waiter = new Handler();

        waiter.postDelayed(new Runnable() {
            @Override
            public void run() {
                FingerPrintElement[] print = dummy.calculateAveragePrint(fingerprints);

                String fingerprint = "";
                for(int i = 0; i < print.length; i++) {
                    String tmpString = print[i].getBssid() + "," + print[i].getLevel() + ";";
                    fingerprint += tmpString;
                }

                Log.d(TAG, "fingerprint = " + fingerprint);

                Log.d(TAG, "creating values ...");

                ContentValues values = new ContentValues();
                values.put("place", place);
                values.put("fingerprint", fingerprint);

                Log.d(TAG, "store to DB ...");
                StudIPApplication.getWritableDatabase().insert("places", null, values);

                Log.d(TAG, "... storePlaceToDB finished!");

                Toast toast = Toast.makeText(getApplicationContext(), "Neuen Ort - " + place + " - eingespeichert" +
                                " mit Fingerprint - " + fingerprint,
                        Toast.LENGTH_LONG);
                toast.show();
            }
        }, 60000);





    }

    public String getCurrentFingerprintAsString() {

        Log.d(TAG, "getting scan results ...");
        List<ScanResult> wifiScanList = mainWifiObj.getScanResults();

        int max_level = 0;
        int current_level = 0;
        for (int i = 0; i < wifiScanList.size(); i++) {
            //max_level+=WifiManager.calculateSignalLevel(wifiScanList.get(i).level, 100);
            current_level = WifiManager.calculateSignalLevel(wifiScanList.get(i).level, 100);
            if (current_level > max_level) {
                max_level = current_level;
            }
        }

        String fingerprint = new String();
        for (int i = 0; i < wifiScanList.size(); i++) {

            int sig_str = 0;
            double sig_str_pcnt = 0.0;
            sig_str = WifiManager.calculateSignalLevel(wifiScanList.get(i).level, 100);
            sig_str_pcnt = ((double) (sig_str)) / ((double) (max_level)) * 100;

            String ssid = wifiScanList.get(i).SSID;
            //Making sure, only access points of uos are taken for fingerprint
            if (ssid.contains("eduroam") || ssid.contains("Uni Osnabrueck")) {
                fingerprint = fingerprint + wifiScanList.get(i).BSSID + ",";
                fingerprint = fingerprint + String.valueOf(sig_str_pcnt) + ";";
            }
        }

        Log.d(TAG, "getCurentFingerPrintAsString: " + fingerprint);

        return fingerprint;
    }

    public void localize(View view) {
        Log.d(TAG, "starting localization ...");

        //Get Fingerprint of own localation
        //Build a place-object witht this information

        //Perform a wifi Scan
        List<ScanResult> wifiScanList = mainWifiObj.getScanResults();

        //Getting the maximum signal strength found
        // store to max_level
        //needed for calculating percentage signal strength
        int max_level = 0;
        int current_level = 0;
        for(int i = 0; i < wifiScanList.size(); i++) {
            //max_level+=WifiManager.calculateSignalLevel(wifiScanList.get(i).level, 100);
            current_level = WifiManager.calculateSignalLevel(wifiScanList.get(i).level, 100);
            if (current_level > max_level) {
                max_level = current_level;
            }
        }

        //Unnecessary?
        String place;
        String fingerprint = "";
        EditText place_et;
        place_et = (EditText) findViewById(R.id.place_textedit);
        place = place_et.getText().toString();

        //Getting Fingerprint of Current location in proper form
        //means BSSID1,SOMEVALUEINPERCENT;BSSID2,SOMEVALUEINPERCENT ...
        for(int i = 0; i < wifiScanList.size(); i++) {
            int sig_str = 0;
            double sig_str_pcnt = 0.0;
            sig_str = WifiManager.calculateSignalLevel(wifiScanList.get(i).level, 100);
            sig_str_pcnt = ((double) (sig_str)) / ((double) (max_level)) * 100;

            fingerprint = fingerprint + wifiScanList.get(i).BSSID + ",";
            fingerprint = fingerprint + String.valueOf(sig_str_pcnt) + ";";
        }

        //Creating a place object to be able to compare
        // id is -1 to ensure place does not exist in db
        Place own_place = new Place(-1, fingerprint);

        //Getting all the Data from the DB
        String[] FROM = { "_id" , "place" , "fingerprint"};   //the Attributes we want to get
        Cursor cursor = StudIPApplication.getWritableDatabase().query("places", FROM, null,
                null, null, null, null);
        boolean found = false;
        /*while (!found && cursor.moveToNext()) {
            Log.d(TAG, cursor.getInt(0) + "" + cursor.getString(1) + " " + cursor.getString(2));
            Place p = new Place(Integer.parseInt(cursor.getString(1)), cursor.getString(2));
            if(own_place.compareTo(p) == 0) {
                found = true;
                Log.d(TAG, "Yeah, i localized myself successfully!");
            } else {
                Log.d(TAG, "This wasn't a match ...");
            }
        }*/
        double min_distance = 100000.0;
        Place current_location = own_place;
        while (cursor.moveToNext()) {
            Place p;
            try {
                p = new Place(Integer.parseInt(cursor.getString(1)), cursor.getString(2));
                Log.d(TAG, "Distance to " + p.getPlace() + ": " + own_place.distanceTo(p));
                if(own_place.distanceTo(p) < min_distance) {
                    current_location = p;
                    min_distance = own_place.distanceTo(p);
                    found = true;
                }
            } catch (NumberFormatException e) {
                if(cursor.moveToNext());
            }

        }
        Toast toast;
        if (found ) {
            Log.d(TAG, "Tell the user!");
            toast = Toast.makeText(getApplicationContext(), "Sie sind bei " + current_location.getPlace(),
                    Toast.LENGTH_SHORT);
        } else {
            toast = Toast.makeText(getApplicationContext(), "Konnte nicht lokalisieren",
                    Toast.LENGTH_SHORT);
        }
        toast.show();
        //current_field_id = Integer.parseInt(cursor.getString(1));
        current_field_id = current_location.getPlace();
        Log.d(TAG, "localization done");
    }

    public void showMap(View view) {/*
        Log.d(TAG, "starting showMap");
        Button showMapButton = (Button) findViewById(R.id.show_map_button);
        LayoutInflater layoutInflater =
                (LayoutInflater)getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        Button closePopup = (Button)popupView.findViewById(R.id.close_popup_button);
        closePopup.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        Log.d(TAG, "getting image view");
        ImageView map = (ImageView) popupView.findViewById(R.id.map_image_view);
        Log.d(TAG, "getting bitmap");
        Bitmap map_bitmap = BitmapFactory.
                decodeResource(getResources(), R.drawable.grundriss_wohnung);
        //Drawable myDrawable = getResources().getDrawable(R.drawable.grundriss_wohnung);
        //Bitmap map_bitmap = ((BitmapDrawable) myDrawable).getBitmap();
        Log.d(TAG, "building map");
        MapBuilder m_builder = new MapBuilder(map, map_bitmap);
        Log.d(TAG, "draw map");
        /*if(current_field_id != -1 || draw_all) {  //TODO draw all raus!!!
            int[] pos = {current_field_id};
            if(draw_all) {
                pos = new int[8];
                for(int i = 0;  i < 8; i++) {
                    pos[i] = i;
                }
            }
            Log.d(TAG, "pos length = " + String.valueOf(pos.length));
            m_builder.drawMap(pos);
        } else {
            m_builder.drawMap();
        }*/
        /*m_builder.drawMap();

        Log.d(TAG, "showMap done");

        popupWindow.showAtLocation(view, 0 , 0, 0);*/

        Intent intent = new Intent(this, MapViewerActivity.class);
        intent.putExtra(EXTRA_CURRENT_POSITION, current_field_id);
        startActivity(intent);

    }

    public void clearDB(View view) {
        Log.d(TAG, "clearing DB...");
        new AlertDialog.Builder(this)
                .setTitle("Achtung!")
                .setMessage("Soll die gesamte Datenbank wirklich gelöscht werden?")
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Datenbank löschen!");
                        StudIPApplication.getWritableDatabase().execSQL("DELETE FROM places;");
                    }
                })
                .setNegativeButton("Nein", null).show();
    }

    class WifiScanReceiver extends BroadcastReceiver {
        @SuppressLint("UseValueOf")
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = mainWifiObj.getScanResults();
            wifis = new String[wifiScanList.size()];
            int max_level = 0;
            int current_level = 0;
            for(int i = 0; i < wifiScanList.size(); i++) {
                //max_level+=WifiManager.calculateSignalLevel(wifiScanList.get(i).level, 100);
                current_level = WifiManager.calculateSignalLevel(wifiScanList.get(i).level, 100);
                if (current_level > max_level) {
                    max_level = current_level;
                }
            }
            for(int i = 0; i < wifiScanList.size(); i++){
                int sig_str = 0;
                double sig_str_pcnt = 0.0;
                String cont;
                String ssid;
                sig_str = WifiManager.calculateSignalLevel(wifiScanList.get(i).level, 100);
                sig_str_pcnt = ((double) (sig_str)) / ((double) (max_level)) * 100;
                ssid = wifiScanList.get(i).SSID;
                if(ssid.contains("eduroam") || ssid.contains("Uni Osnabrueck")) {
                    cont = "SSID: " + wifiScanList.get(i).SSID + "\n" +
                            "BSSID: " + wifiScanList.get(i).BSSID + "\n" +
                            //        "Signalstärke: " + String.valueOf(
                            //        WifiManager.calculateSignalLevel(wifiScanList.get(i).level, 100) + "\n");
                            "Signalstärke: " + String.valueOf(sig_str_pcnt) + " %\n";
                    //wifis[i] = ((wifiScanList.get(i)).toString());
                    wifis[i] = cont;
                }
            }

            list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
                    android.R.layout.simple_list_item_1,wifis));
        }
    }

}