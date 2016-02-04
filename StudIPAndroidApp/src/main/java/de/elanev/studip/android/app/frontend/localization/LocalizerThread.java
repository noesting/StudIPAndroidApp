package de.elanev.studip.android.app.frontend.localization;

import android.content.Context;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;


import android.content.Context;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpException;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import de.elanev.studip.android.app.R;
import de.elanev.studip.android.app.StudIPApplication;

import de.elanev.studip.android.app.backend.datamodel.LocationRoom;
import de.elanev.studip.android.app.backend.datamodel.Server;
import de.elanev.studip.android.app.backend.datamodel.User;
import de.elanev.studip.android.app.backend.datamodel.UserLocation;
import de.elanev.studip.android.app.backend.net.services.StudIpLegacyApiService;
//import de.elanev.studip.android.app.widget.ReactiveListFragment;
import de.elanev.studip.android.app.util.Prefs;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.app.AppObservable;
import rx.android.lifecycle.LifecycleEvent;
import rx.android.lifecycle.LifecycleObservable;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

import static rx.android.schedulers.AndroidSchedulers.mainThread;


/**
 * Created by Nils on 01.09.2015.
 */
public class LocalizerThread implements Runnable { //} extends TimerTask {

    private final String TAG = "LocalizerThread";
    private Context appContext;
    private WifiManager mainWifiObj;
    private MapBuilder mapBuilder;
    private Handler handler;

    private ArrayList<String> students;
    private ArrayList<String> tutors;
    private ArrayList<String> allPeople;

    private MapViewerActivity mapActivity;

    private LocalizedUser myself;

    private int lowerUserBound;
    private int upperUserBound;

    public int current_field_id;
    public boolean position_changed;
    public boolean newly_build;

    Server server = Prefs.getInstance(appContext).getServer();
    private StudIpLegacyApiService mApiService;
    private CompositeSubscription mCompositeSubscription;

    public LocalizerThread(Context context, WifiManager wifiManager, MapBuilder m_Builder, Handler h, MapViewerActivity mva) {
        appContext = context;
        mainWifiObj = wifiManager;
        mapBuilder = m_Builder;
        current_field_id = -1;
        handler = h;
        mapActivity = mva;
        position_changed = true;
        newly_build = true;

        mApiService = new StudIpLegacyApiService(server, appContext);
        mCompositeSubscription = new CompositeSubscription();

        lowerUserBound = 0;
        upperUserBound = 0;

        students = new ArrayList<String>();
        tutors = new ArrayList<String>();
    }

    @Override
    public void run() {

        //delete fields
        /*for (int i = 0; i < mapBuilder.getMapFields().length; i++) {
            mapBuilder.getMapFields()[i].deletePeopleFromField();
        }*/

        String userID;
        userID = Prefs.getInstance(appContext).getUserId();
        Log.d(TAG, "UserID: " + userID);


        //Getting data from other students ...

        if(students.size() > 30) {
            if(upperUserBound == students.size()) {
                lowerUserBound = 0;
                upperUserBound = 30;
            } else {
                lowerUserBound = upperUserBound;
                upperUserBound += 30;
                if (upperUserBound > students.size()) {
                    upperUserBound = students.size();
                }
            }
        } else {
            upperUserBound = students.size();
        }

        if(students.size() == 0) {
            upperUserBound = 0;
            lowerUserBound = 0;
        }

        Log.d(TAG, "Students.size: " + students.size());

        for(int i = lowerUserBound; i < upperUserBound; i++) {
            Log.d(TAG, "Getting Location of student: " + students.get(i));
            if(!students.get(i).equals(userID)) {
                getUserLocation(students.get(i), 0); //student state
            }
        }

        for(int i = 0; i < tutors.size(); i++) {
            getUserLocation(tutors.get(i), 1); //tutor state
        }


        Log.d(TAG, "starting own localization ...");

        //Get Fingerprint of own localation
        //Build a place-object witht this information

        //Perform a wifi Scan
        mainWifiObj.startScan();
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

        //Creating empty Fingerprint string for later use
        String fingerprint = new String();


        //Getting Fingerprint of Current location in proper form
        //means BSSID1,SOMEVALUEINPERCENT;BSSID2,SOMEVALUEINPERCENT ...
        for(int i = 0; i < wifiScanList.size(); i++) {
            String ssid = wifiScanList.get(i).SSID;
            //Making sure, only access points of uos are taken for fingerprint
            //if(ssid.contains("eduroam") || ssid.contains("Uni Osnabrueck")) {
                int sig_str = 0;
                double sig_str_pcnt = 0.0;
                sig_str = WifiManager.calculateSignalLevel(wifiScanList.get(i).level, 100);
                sig_str_pcnt = ((double) (sig_str)) / ((double) (max_level)) * 100;

                fingerprint = fingerprint + wifiScanList.get(i).BSSID + ",";
                fingerprint = fingerprint + String.valueOf(sig_str_pcnt) + ";";
            //}
        }

        Log.d(TAG, "Fingerprint: " + fingerprint);

        //Test wether uos APs where found
        if(fingerprint.isEmpty()) {
            //No localization possible, abort now
            Log.d(TAG, "Cannot localize, no AP of UOS nearby");
            handler.postDelayed(this, 15000);
            return;
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
                //Log.d(TAG, "place p: " + p.getPlace());
                //Log.d(TAG, "Distance to " + p.getPlace() + ": " + own_place.distanceTo(p));
                if(own_place.distanceTo(p) < min_distance) {
                    current_location = p;
                    min_distance = own_place.distanceTo(p);
                    found = true;
                }
            } catch (NumberFormatException e) {
                if(cursor.moveToNext());
            }

        }
        /*Toast toast;
        if (found ) {
            Log.d(TAG, "Tell the user!");
            toast = Toast.makeText(appContext, "Sie sind bei " + current_location.getPlace(),
                    Toast.LENGTH_SHORT);
        } else {
            toast = Toast.makeText(appContext, "Konnte nicht lokalisieren",
                    Toast.LENGTH_SHORT);
        }
        toast.show();*/
        //current_field_id = Integer.parseInt(cursor.getString(1));

        current_field_id = current_location.getPlace();
        if (current_field_id != mapBuilder.own_position) {
            position_changed = true;
            mapBuilder.own_position = current_field_id;
        } else {
            position_changed = false;
        }
        if(newly_build) {
            position_changed = true;
            newly_build = false;
        }
        //Log.d(TAG, "localization done");


        //time to draw in map
        //Log.d(TAG, "getting positions to mark");
        /*if(current_field_id >= 0 && current_field_id < 20) {
            mapBuilder.changeMap(mapActivity.getMapBitmap(R.drawable.map_22_1e), 0);
        } else if(current_field_id >= 20 && current_field_id < 40) {
            mapBuilder.changeMap(mapActivity.getMapBitmap(R.drawable.map_1st_floor), 1);
        }
        else if(current_field_id >= 40 && current_field_id < 70) {
            mapBuilder.changeMap(mapActivity.getMapBitmap(R.drawable.map_22_2o), 2);
        }
        else if(current_field_id >= 70 && current_field_id < 90) {
            mapBuilder.changeMap(mapActivity.getMapBitmap(R.drawable.map_22_3o), 3);
        }*/

        if(position_changed && current_field_id != -1) {

            if(mapBuilder.getMapFields()[current_field_id].name.equals("default")) {
                myself.setLocation(mapBuilder.getMapFields()[current_field_id].bezeichnung);
            } else {
                myself.setLocation(mapBuilder.getMapFields()[current_field_id].name);
            }
            mapBuilder.deleteUser(myself);
            mapBuilder.addUser(myself, current_field_id);
            /*int[] pos = {current_field_id};
            boolean contains = false;
            //Log.d(TAG, "Current Field id: "+ current_field_id);
            //Log.d(TAG, "Map Fields length: " + mapBuilder.getMapFields().length);
            for(int i = 0; i < mapBuilder.getMapFields()[current_field_id].getPeople().size(); i++) {
                /*if (mapBuilder.getMapFields()[current_field_id].getPeople().get(i).equals(myself.getName())) {
                    contains = true;
                }
                if (mapBuilder.getMapFields()[current_field_id].containsUser(myself)) {
                    contains = true;
                }
            }
            if(!contains) {
                mapBuilder.getMapFields()[current_field_id].addToField(myself);
            }*/
            //int[] pos = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14};
            //Log.d(TAG, "pos length = " + String.valueOf(pos.length));
            //mapBuilder.drawMap(pos);
        } else {
           // mapBuilder.drawMap();
        }

        if(position_changed && !mapBuilder.getMapFields()[current_field_id].name.equals("default")) { //Test if it's a resource

            CompositeSubscription mCompositeSubscription = new CompositeSubscription();

            Server server = Prefs.getInstance(appContext).getServer();
            StudIpLegacyApiService mApiService = new StudIpLegacyApiService(server, appContext);

            mCompositeSubscription.add(mapActivity.bind(mApiService.storeLocation(myself.getLocation(),
                    myself.getId())).subscribe(new Subscriber<UserLocation>() {
                @Override
                public void onCompleted() {
                    Log.d(TAG, "successfully added " + myself.getLocation());
                    //Toast.makeText(mapActivity.getApplicationContext(), R.string.successfully_added, Toast.LENGTH_LONG).show();
                    //this.setResult(Activity.RESULT_OK);
                    //getApplicationContext().finish();
                }

                @Override
                public void onError(Throwable e) {
                    if (e instanceof TimeoutException) {
                        Toast.makeText(mapActivity.getApplicationContext(), "Request timed out", Toast.LENGTH_SHORT).show();
                    } else if (e instanceof RetrofitError || e instanceof HttpException) {
                        if (!e.getLocalizedMessage().contains("json")) {
                            //Toast.makeText(mapActivity.getApplicationContext(), "Retrofit error or http exception", Toast.LENGTH_LONG)
                            //        .show();
                            Log.e(TAG, e.getLocalizedMessage());
                        }
                    } else {
                        e.printStackTrace();
                        throw new RuntimeException("See inner exception");
                    }
                    //setViewsVisible(true);
                }

                @Override
                public void onNext(UserLocation userLocation) {
                }
            }));
        }


        mapBuilder.drawMap();

        handler.postDelayed(this, 20000);
    }

    private void getUserLocation(final String userId, int state) {

        Log.d(TAG, "Getting user location ...");


        final ArrayList<UserLocation> locs = new ArrayList<>();

        UserSubscriber userSubscriber = new UserSubscriber(mapBuilder, appContext);
        LocationSubscriber locationSubscriber = new LocationSubscriber(mCompositeSubscription, mApiService, mapActivity, userSubscriber);
        locationSubscriber.state = state;

        mCompositeSubscription.add(mapActivity.bind(mApiService.getLocationRoom(userId)).subscribe(locationSubscriber));

    }

    public void setRightFloor() {

        current_field_id = localize();

        Log.d(TAG, "current_field_id: " + current_field_id);

        if(current_field_id >= 0 && current_field_id < 20) {
            mapBuilder.changeMap(mapActivity.getMapBitmap(R.drawable.map_22_1e), 0);
            mapActivity.choose_floor_button.setText("EG A");
            Log.d(TAG, "Range here: " + mapBuilder.getLowerRange() + ":" + mapBuilder.getUpperRange());
        } else if(current_field_id >= 20 && current_field_id < 40) {
            mapBuilder.changeMap(mapActivity.getMapBitmap(R.drawable.map_1st_floor), 1);
            mapActivity.choose_floor_button.setText("1. OG");
            Log.d(TAG, "Range here: " + mapBuilder.getLowerRange() + ":" + mapBuilder.getUpperRange());
        }
        else if(current_field_id >= 40 && current_field_id < 80) {
            mapBuilder.changeMap(mapActivity.getMapBitmap(R.drawable.map_22_2o), 2);
            mapActivity.choose_floor_button.setText("2. OG");
            Log.d(TAG, "Range here: " + mapBuilder.getLowerRange() + ":" + mapBuilder.getUpperRange());
        }
        else if(current_field_id >= 80 && current_field_id < 99) {
            mapBuilder.changeMap(mapActivity.getMapBitmap(R.drawable.map_22_3o), 3);
            mapActivity.choose_floor_button.setText("3. OG");
            Log.d(TAG, "Range here: " + mapBuilder.getLowerRange() + ":" + mapBuilder.getUpperRange());
        }
        else if(current_field_id >= 100 && current_field_id <= 107) {
            mapBuilder.changeMap(mapActivity.getMapBitmap(R.drawable.virtuos_0), 0);
            mapActivity.choose_floor_button.setText("EG");
        }
        else if(current_field_id >= 108 && current_field_id <= 118) {
            mapBuilder.changeMap(mapActivity.getMapBitmap(R.drawable.virtuos_1), 1);
            mapActivity.choose_floor_button.setText("1. OG");
        }
        else if(current_field_id >= 119 && current_field_id <= 129) {
            mapBuilder.changeMap(mapActivity.getMapBitmap(R.drawable.virtuos_2), 2);
            mapActivity.choose_floor_button.setText("2. OG");
        }

        //mapBuilder.getImageView().setScrollPosition(mapBuilder.getMapFields()[current_field_id].map_x,
        //        mapBuilder.getMapFields()[current_field_id].map_y);
        mapBuilder.getImageView().resetZoom();

    }

    public int localize() {

        int field_id = 0;

        mainWifiObj.startScan();
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

        //Creating empty Fingerprint string for later use
        String fingerprint = new String();


        //Getting Fingerprint of Current location in proper form
        //means BSSID1,SOMEVALUEINPERCENT;BSSID2,SOMEVALUEINPERCENT ...
        for(int i = 0; i < wifiScanList.size(); i++) {
            String ssid = wifiScanList.get(i).SSID;
            //Making sure, only access points of uos are taken for fingerprint
            //if(ssid.contains("eduroam") || ssid.contains("Uni Osnabrueck")) {
            int sig_str = 0;
            double sig_str_pcnt = 0.0;
            sig_str = WifiManager.calculateSignalLevel(wifiScanList.get(i).level, 100);
            sig_str_pcnt = ((double) (sig_str)) / ((double) (max_level)) * 100;

            fingerprint = fingerprint + wifiScanList.get(i).BSSID + ",";
            fingerprint = fingerprint + String.valueOf(sig_str_pcnt) + ";";
            //}
        }

        //Log.d(TAG, "Fingerprint: " + fingerprint);

        //Test wether uos APs where found
        if(fingerprint.isEmpty()) {
            //No localization possible, abort now
            Log.d(TAG, "Cannot localize, no AP of UOS nearby");
            field_id = -1;
            return field_id;
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
                //Log.d(TAG, "place p: " + p.getPlace());
                //Log.d(TAG, "Distance to " + p.getPlace() + ": " + own_place.distanceTo(p));
                if(own_place.distanceTo(p) < min_distance) {
                    current_location = p;
                    min_distance = own_place.distanceTo(p);
                    found = true;
                }
            } catch (NumberFormatException e) {
                if(cursor.moveToNext());
            }

        }
        /*Toast toast;
        if (found ) {
            Log.d(TAG, "Tell the user!");
            toast = Toast.makeText(appContext, "Sie sind bei " + current_location.getPlace(),
                    Toast.LENGTH_SHORT);
        } else {
            toast = Toast.makeText(appContext, "Konnte nicht lokalisieren",
                    Toast.LENGTH_SHORT);
        }
        toast.show();*/
        //current_field_id = Integer.parseInt(cursor.getString(1));
        field_id = current_location.getPlace();

        return field_id;
    }

    /*protected <T> Observable<T> bind(Observable<T> observable) {
        //Observable<T> boundObservable = AppObservable.bindSupportFragment(this, observable)
          //      .observeOn(mainThread());
        Observable<T> boundObservable = AppObservable.bindActivity(appContext, observable);
        return LifecycleObservable.bindFragmentLifecycle(lifecycle(), boundObservable);
    }*/

    private Observable<LifecycleEvent> lifecycle() {
        BehaviorSubject<LifecycleEvent> lifecycleSubject = BehaviorSubject.create();
        return lifecycleSubject.asObservable();
    }

    public void setStudents(ArrayList<String> s) {
        students = s;
    }

    public void setTutors(ArrayList<String> t) {
        tutors = t;
    }

    public ArrayList<String> composeLists(ArrayList<String> students, ArrayList<String> tutors) {
        ArrayList<String> resList = new ArrayList<String>();
        resList.addAll(students);
        resList.addAll(tutors);
        return resList;
    }

    public void getAllPeople() {
        allPeople = composeLists(students, tutors);
    }

    public LocalizedUser getMyself() {
        return myself;
    }

    public void setMyself(LocalizedUser user) {
        myself = user;
    }

    public void setRestApiComponents(CompositeSubscription sub, StudIpLegacyApiService api) {
        mApiService = api;
        mCompositeSubscription = sub;
    }
}
