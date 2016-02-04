package de.elanev.studip.android.app.frontend.localization;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import org.apache.http.HttpException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import de.elanev.studip.android.app.R;
import de.elanev.studip.android.app.backend.datamodel.Course;
import de.elanev.studip.android.app.backend.datamodel.Courses;
import de.elanev.studip.android.app.backend.datamodel.ForumEntry;
import de.elanev.studip.android.app.backend.datamodel.LocationRoom;
import de.elanev.studip.android.app.backend.datamodel.Server;
import de.elanev.studip.android.app.backend.datamodel.User;
import de.elanev.studip.android.app.backend.datamodel.UserLocation;
import de.elanev.studip.android.app.backend.net.services.StudIpLegacyApiService;
import de.elanev.studip.android.app.util.Prefs;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.android.app.AppObservable;
import rx.android.lifecycle.LifecycleEvent;
import rx.android.lifecycle.LifecycleObservable;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

import static rx.android.schedulers.AndroidSchedulers.mainThread;


public class MapViewerActivity extends Activity implements View.OnClickListener {

    private final String TAG = "MapViewerActivity";
//    private WebView mapView;

    int current_field_id = -1;
    boolean draw_all = false;

    private final double DEF_HEIGHT = 1200;//1776;
    private final double DEF_WIDTH = 800;//1080;
    int height;
    int width;

    private String userName;

    private TouchImageView mapView;
    private MapBuilder m_builder;

    private Timer timer;

    private LocalizerThread thread;
    private Handler handler;

    protected Button choose_floor_button;
    protected Button choose_building_button;
    protected Button close_map_button;
    protected Button choose_filter_button;
    protected Button focus_map_button;

    private CompositeSubscription mCompositeSubscription;
    private StudIpLegacyApiService mApiService;

    private String userId;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_map_viewer);

        Log.d(TAG, "getting current position from MainActivity");
        Intent intent = getIntent();
        current_field_id = intent.getIntExtra(LocalizationActivity.EXTRA_CURRENT_POSITION, -1);

        Log.d(TAG, "getting display dimensions");
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        height = size.y;
        width = size.x;

        Log.d(TAG, "calculating scaling factor");
        double scale_x = ((double)(width)) / DEF_WIDTH;
        double scale_y = ((double)(height)) / DEF_HEIGHT;

        Log.d(TAG, "getting image view");
        mapView = (TouchImageView) findViewById(R.id.tiv_map);

        //Log.d(TAG, "Getting map as drawable");
        //Drawable myDrawable = getResources().getDrawable(R.drawable.map_1st_floor);

        //Log.d(TAG, "Getting map as bitmap");
        //Bitmap map_bitmap = ((BitmapDrawable) myDrawable).getBitmap();
        //Bitmap map_bitmap = loadBitmap();
        //mapView.setImageBitmap(map_bitmap);

        Log.d(TAG, "send map to mapbuilder");
        m_builder = new MapBuilder(mapView, getMapBitmap(R.drawable.map_1st_floor), scale_x, scale_y);

        choose_floor_button = (Button) findViewById(R.id.choose_floor_button);
        //choose_building_button = (Button) findViewById(R.id.choose_building_button);
        close_map_button = (Button) findViewById(R.id.close_map_button);
        choose_filter_button = (Button) findViewById(R.id.choose_filter_button);
        focus_map_button = (Button) findViewById(R.id.focus_map_button);

        choose_floor_button.setTextColor(Color.WHITE);
        //choose_building_button.setTextColor(Color.WHITE);
        close_map_button.setTextColor(Color.WHITE);
        choose_filter_button.setTextColor(Color.WHITE);
        focus_map_button.setTextColor(Color.WHITE);

        m_builder.setMarkerBitmaps(getMapBitmap(R.drawable.single_red),
                getMapBitmap(R.drawable.single_blue),
                getMapBitmap(R.drawable.double_red_blue),
                getMapBitmap(R.drawable.double_blue),
                getMapBitmap(R.drawable.triple_red_blue),
                getMapBitmap(R.drawable.triple_blue));

        /*TimerTask timerTask = new LocalizerThread(getApplicationContext(),
                (WifiManager) getSystemService(Context.WIFI_SERVICE),
                m_builder);
        timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, 60000);*/

        userId = Prefs.getInstance(getApplicationContext()).getUserId();
        Log.d(TAG, "User id is " + userId);

        Server server = Prefs.getInstance(getApplicationContext()).getServer();
        Log.d(TAG, "Got Server " + server.getName());

        mApiService = new StudIpLegacyApiService(server, getApplicationContext());
        Log.d(TAG, "Got apiService");

        Log.d(TAG, "Creating Subscription");
        mCompositeSubscription = new CompositeSubscription();


        handler = new Handler();

        thread = new LocalizerThread(getApplicationContext(),
                (WifiManager) getSystemService(Context.WIFI_SERVICE),
                m_builder, handler, this);
        
        thread.setRightFloor();  //begin with floor user is at
        thread.setRestApiComponents(mCompositeSubscription, mApiService);

        thread.setMyself(new LocalizedUser("MySelf", userId, ""));

        handler.post(thread);

        /*

        Log.d(TAG, "getting positions to mark");
        if(current_field_id != -1 || draw_all) {  //TODO draw all raus!!!
            int[] pos = {current_field_id};
            if(draw_all) {
                Log.d(TAG, "going to draw all");
                pos = new int[15];
                for(int i = 0;  i < 15; i++) {
                    pos[i] = i;
                }
            }
            Log.d(TAG, "pos length = " + String.valueOf(pos.length));
            m_builder.drawMap(pos);
        } else {
            m_builder.drawMap();
        }
        //m_builder.drawMap()
        */

        Log.d(TAG, "Height = " + height);
        Log.d(TAG, "Width = " + width);

    }


    @Override
    public void onClick(View v) {

    }
    
    public void refocus(View view) {
        /*LocalizerThread resetter = new LocalizerThread(getApplicationContext(),
                (WifiManager) getSystemService(Context.WIFI_SERVICE),
                m_builder, handler, this);
        resetter.setRightFloor();*/
        thread.setRightFloor();
    }

    public Bitmap getMapBitmap(int drawable_id) {
        //Log.d(TAG, "Getting map as drawable");
        Drawable myDrawable = getResources().getDrawable(drawable_id);
        //Log.d(TAG, "myDrawable size: " + myDrawable.getIntrinsicWidth() + ";" + myDrawable.getIntrinsicHeight());


        //Log.d(TAG, "Getting map as bitmap");
        Bitmap map_bitmap = ((BitmapDrawable) myDrawable).getBitmap();
        //Log.d(TAG, "map_bitmap size: " + map_bitmap.getWidth() + ";" + map_bitmap.getHeight());

        return map_bitmap;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void chooseFloor(View view) {
        PopupMenu popup = new PopupMenu(this, view);

        popup.getMenu().add(1, 0, Menu.NONE, "0. OG");
        popup.getMenu().add(1, 1, Menu.NONE, "1. OG");
        popup.getMenu().add(1, 2, Menu.NONE, "2. OG");
        //popup.getMenu().add(1, 3, Menu.NONE, "3. OG");

        for(int i = 0; i < popup.getMenu().size(); i++) {
            Log.d(TAG, "Menu item: " + popup.getMenu().getItem(i) + " has id: " + popup.getMenu().getItem(i).getItemId());
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                                             @Override
                                             public boolean onMenuItemClick(MenuItem item) {
                                                 Log.d(TAG, "Item id: " + item.getItemId());

                                                 choose_floor_button.setText(item.getTitle());
                                                 if(item.getItemId() == 0) {
                                                     choose_floor_button.setText("EG");
                                                     //m_builder.changeMap(getMapBitmap(R.drawable.map_22_1e), item.getItemId());
                                                     m_builder.changeMap(getMapBitmap(R.drawable.virtuos_0), item.getItemId());
                                                 }
                                                 if(item.getItemId() == 1) {
                                                     choose_floor_button.setText("1. OG");
                                                     m_builder.changeMap(getMapBitmap(R.drawable.virtuos_1), item.getItemId());
                                                     //m_builder.changeMap(getMapBitmap(R.drawable.map_1st_floor), item.getItemId());
                                                 }
                                                 if(item.getItemId() == 2) {
                                                     choose_floor_button.setText("2. OG");
                                                     m_builder.changeMap(getMapBitmap(R.drawable.virtuos_2), item.getItemId());
                                                     //m_builder.changeMap(getMapBitmap(R.drawable.map_22_2o), item.getItemId());
                                                 }
                                                 /*if(item.getItemId() == 3) {
                                                     choose_floor_button.setText("3. OG");
                                                     m_builder.changeMap(getMapBitmap(R.drawable.map_22_3o), item.getItemId());
                                                 }*/
                                                 return true;
                                             }
                                         }
        );

        popup.show();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void chooseFilter(View view) {

        Log.d(TAG, "Entering chooseFilter...");

        final PopupMenu menu = new PopupMenu(this, view);


        //new Handler setUp
        handler.removeCallbacks(thread);
        thread = new LocalizerThread(getApplicationContext(),
                (WifiManager) getSystemService(Context.WIFI_SERVICE),
                m_builder, handler, this);

        Log.d(TAG, "Creating ArrayList");
        final ArrayList<Course> courses = new ArrayList<>();

        mCompositeSubscription.clear();

        mCompositeSubscription.add(bind(mApiService.getUser(userId)).subscribe(
                new Subscriber<User>() {
                    @Override
                    public void onCompleted() {

                        mCompositeSubscription.add(bind(mApiService.getAllCourses()).subscribe(
                                new Subscriber<Course>() {

                                    int counter = 0;

                                    @Override
                                    public void onCompleted() {
                                        menu.show();
                                        Log.d(TAG, "Done finding courses");
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        if (e instanceof TimeoutException) {
                                            Toast.makeText(getApplicationContext(), "Request timed out", Toast.LENGTH_SHORT).show();
                                        } else if (e instanceof RetrofitError) {
                                            Toast.makeText(getApplicationContext(), "Retrofit error", Toast.LENGTH_LONG).show();
                                            e.printStackTrace();
                                        } else if (e instanceof HttpException) {
                                            Toast.makeText(getApplicationContext(), "HTTP exception", Toast.LENGTH_LONG).show();
                                            Log.e(TAG, e.getLocalizedMessage());
                                        } else if (e instanceof StudIpLegacyApiService.UserNotFoundException) {
                                            Log.e(TAG, "User not found");
                                            return;
                                        } else {
                                            e.printStackTrace();
                                            throw new RuntimeException("See inner exception");
                                        }

                                    }

                                    @Override
                                    public void onNext(Course course) {
                                        Log.d(TAG, "Found course: " + course.title);
                                        courses.add(counter, course);
                                        menu.getMenu().add(0, counter, Menu.NONE, courses.get(counter).title);
                                        Log.d(TAG, "Counter: " + counter);
                                        Log.d(TAG, "Menu Item: " + menu.getMenu().getItem(counter));
                                        counter++;
                                    }
                                }));

                    }

                    @Override
                    public void onError(Throwable e) {

                        if (e instanceof TimeoutException) {
                            Toast.makeText(getApplicationContext(), "Request timed out", Toast.LENGTH_SHORT).show();
                        } else if (e instanceof RetrofitError) {
                            Toast.makeText(getApplicationContext(), "Retrofit error", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        } else if (e instanceof HttpException) {
                            Toast.makeText(getApplicationContext(), "HTTP exception", Toast.LENGTH_LONG).show();
                            Log.e(TAG, e.getLocalizedMessage());
                        } else if (e instanceof StudIpLegacyApiService.UserNotFoundException) {
                            Log.e(TAG, "User not found");
                            return;
                        } else {
                            e.printStackTrace();
                            throw new RuntimeException("See inner exception");
                        }

                    }

                    @Override
                    public void onNext(User user) {

                        userName = user.getFullName();

                    }
                }
        ));

        Log.d(TAG, "Adding Subscriber");


        /*if(courses.isEmpty()) {
            Log.d(TAG, "Empty Courses");
        } else {
            Log.d(TAG, "Something is in courses!");
            PopupMenu popupMenu = new PopupMenu(this, view);
            for(int i = 0; i < courses.size(); i++) {
                Log.d(TAG, "courses[" + i + "]: " + courses.get(i).title);
                popupMenu.getMenu().add(1, i, Menu.NONE,courses.get(i).title);
            }
            popupMenu.show();
        }*/

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                Log.d(TAG, "Menu Item clicked: " + menuItem.getItemId());
                Log.d(TAG, "Menu Item clicked: " + menuItem.toString());

                for(int i = 0; i < m_builder.getMapFields().length; i++) {
                    m_builder.getMapFields()[i].deletePeopleFromField();
                }

                Course course = courses.get(menuItem.getItemId());

                ArrayList<String> students = course.students;
                ArrayList<String> tutors = course.tutors;

                Log.d(TAG, "The Course is: " + course.courseId);
                Log.d(TAG, "The Course title is: " + course.title);

                for (int i = 0; i < students.size(); i++) {
                    Log.d(TAG, "Student is inside: " + students.get(i));
                }

                handler.removeCallbacks(thread);

                thread.setStudents(students);
                thread.setTutors(tutors);
                Log.d(TAG, "Set Students to thread!");

                thread.setRightFloor();

                thread.setMyself(new LocalizedUser(userName, userId, ""));


                if (handler.post(thread)) {
                    Log.d(TAG, "Handler posted!");
                    return true;
                }

                Log.d(TAG, "Something went wron...");

                return false;
            }
        });

        Log.d(TAG, "Courses call done");

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void chooseBuilding(View view) {

        //Testdata
        //final String res_id = "b30316b6bab6935bf1a162f822e55d66";
        final String res_id = "e0d7bf89366b891f7776556efcf372cd";

        final String user1_id = "da9b9e4edb62e426719c483d8ed36bfe";
        final String user1_res = "583410f1d1c8d4f20f8c9992cba8d539";
        final String user2_id = "76c5ecae320fa66a61cb7d89dd9a7be3";
        final String user2_res = "5b7d42e52d16dda0dd58bc5fe2710543";
        final String user3_id = "90a323f664d954cb09dacd98dab18c7e";
        final String user3_res = "da31ce2135f25e18cb8f700d61cb3ca9";

        final String user_id = Prefs.getInstance(getApplicationContext()).getUserId();

        //int room_id = 20;

        Server server = Prefs.getInstance(this.getApplicationContext()).getServer();

        StudIpLegacyApiService mApiService = new StudIpLegacyApiService(server, getApplicationContext());

        CompositeSubscription mCompositeSubscription = new CompositeSubscription();

        Log.d(TAG, "Resource id in MapViewer: " + user3_res);

        mCompositeSubscription.add(bind(mApiService.storeLocation(res_id,
                user_id)).subscribe(new Subscriber<UserLocation>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "successfully added " + res_id);
                Toast.makeText(getApplicationContext(), R.string.successfully_added, Toast.LENGTH_LONG).show();
                //this.setResult(Activity.RESULT_OK);
                //getApplicationContext().finish();
            }

            @Override
            public void onError(Throwable e) {
                if (e instanceof TimeoutException) {
                    Toast.makeText(getApplicationContext(), "Request timed out", Toast.LENGTH_SHORT).show();
                } else if (e instanceof RetrofitError || e instanceof HttpException) {
                    if(!e.getLocalizedMessage().contains("json")) {
                        Toast.makeText(getApplicationContext(), "Retrofit error or http exception", Toast.LENGTH_LONG)
                                .show();
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

       /* mCompositeSubscription.add(bind(mApiService.storeLocation(res_id,
                user_id)).subscribe(new Subscriber<UserLocation>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "successfully added " + res_id);
                Toast.makeText(getApplicationContext(), R.string.successfully_added, Toast.LENGTH_LONG).show();
                //this.setResult(Activity.RESULT_OK);
                //getApplicationContext().finish();
            }

            @Override
            public void onError(Throwable e) {
                if (e instanceof TimeoutException) {
                    Toast.makeText(getApplicationContext(), "Request timed out", Toast.LENGTH_SHORT).show();
                } else if (e instanceof RetrofitError || e instanceof HttpException) {
                    Toast.makeText(getApplicationContext(), "Retrofit error or http exception", Toast.LENGTH_LONG)
                            .show();
                    Log.e(TAG, e.getLocalizedMessage());
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


        mCompositeSubscription.add(bind(mApiService.storeLocation(user1_res,
                user1_id)).subscribe(new Subscriber<UserLocation>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "successfully added " + res_id);
                Toast.makeText(getApplicationContext(), R.string.successfully_added, Toast.LENGTH_LONG).show();
                //this.setResult(Activity.RESULT_OK);
                //getApplicationContext().finish();
            }

            @Override
            public void onError(Throwable e) {
                if (e instanceof TimeoutException) {
                    Toast.makeText(getApplicationContext(), "Request timed out", Toast.LENGTH_SHORT).show();
                } else if (e instanceof RetrofitError || e instanceof HttpException) {
                    Toast.makeText(getApplicationContext(), "Retrofit error or http exception", Toast.LENGTH_LONG)
                            .show();
                    Log.e(TAG, e.getLocalizedMessage());
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

        mCompositeSubscription.add(bind(mApiService.storeLocation(user2_res,
                user2_id)).subscribe(new Subscriber<UserLocation>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "successfully added " + res_id);
                Toast.makeText(getApplicationContext(), R.string.successfully_added, Toast.LENGTH_LONG).show();
                //this.setResult(Activity.RESULT_OK);
                //getApplicationContext().finish();
            }

            @Override
            public void onError(Throwable e) {
                if (e instanceof TimeoutException) {
                    Toast.makeText(getApplicationContext(), "Request timed out", Toast.LENGTH_SHORT).show();
                } else if (e instanceof RetrofitError || e instanceof HttpException) {
                    Toast.makeText(getApplicationContext(), "Retrofit error or http exception", Toast.LENGTH_LONG)
                            .show();
                    Log.e(TAG, e.getLocalizedMessage());
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

        mCompositeSubscription.add(bind(mApiService.storeLocation(user3_res,
                user3_id)).subscribe(new Subscriber<UserLocation>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "successfully added " + res_id);
                Toast.makeText(getApplicationContext(), R.string.successfully_added, Toast.LENGTH_LONG).show();
                //this.setResult(Activity.RESULT_OK);
                //getApplicationContext().finish();
            }

            @Override
            public void onError(Throwable e) {
                if (e instanceof TimeoutException) {
                    Toast.makeText(getApplicationContext(), "Request timed out", Toast.LENGTH_SHORT).show();
                } else if (e instanceof RetrofitError || e instanceof HttpException) {
                    Toast.makeText(getApplicationContext(), "Retrofit error or http exception", Toast.LENGTH_LONG)
                            .show();
                    Log.e(TAG, e.getLocalizedMessage());
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

        final ArrayList<UserLocation> locs = new ArrayList<>();

        final PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("Gebäude 22");
        popup.show();

        /*
        mCompositeSubscription.add(bind(mApiService.getLocationRoom(user_id)).subscribe(
                new Subscriber<UserLocation>() {
                    @Override
                    public void onCompleted() {
                        /*if (mOffset <= 0) {
                            mAdapter.clear();

                        }
                        //mAdapter.addAll(courses);
                        //courses.clear();
                        popup.show();
                        Log.d(TAG, "Done getting user ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof TimeoutException) {
                            Toast.makeText(getApplicationContext(), "Request timed out", Toast.LENGTH_SHORT).show();
                        } else if (e instanceof RetrofitError) {
                            Toast.makeText(getApplicationContext(), "Retrofit error", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        } else if (e instanceof HttpException) {
                            Toast.makeText(getApplicationContext(), "HTTP exception", Toast.LENGTH_LONG).show();
                            Log.e(TAG, e.getLocalizedMessage());
                        } else if (e instanceof StudIpLegacyApiService.UserNotFoundException) {
                            Log.e(TAG, "User not found");
                            return;
                        } else {
                            e.printStackTrace();
                            throw new RuntimeException("See inner exception");
                        }

                    }

                    @Override
                    public void onNext(UserLocation userLocation) {
                        Log.d(TAG, "Found user: " + userLocation.userId + " at " + userLocation.resourceId);
                        //menu.getMenu().add(course.title);
                        popup.getMenu().add("User " + user_id + " is at " + userLocation.resourceId);
                        locs.add(userLocation);
                    }
                }));
        */

    }

    public void close(View view) {
        //timer.cancel();
        handler.removeCallbacks(thread);
        Log.d(TAG, "Thread ended ...");
        this.finish();
    }

    public Bitmap loadBitmap() {
        Bitmap bitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inSampleSize = 5;

        AssetFileDescriptor fileDescriptor = null;

        Resources resources = getApplicationContext().getResources();
        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + resources.getResourcePackageName(R.drawable.map_1st_floor) + '/'
                + resources.getResourceTypeName(R.drawable.map_1st_floor) + '/'
                + resources.getResourceEntryName(R.drawable.map_1st_floor) );

        try {
            fileDescriptor = this.getContentResolver().openAssetFileDescriptor(uri, "r");
        } catch (FileNotFoundException e) {
            Log.d(TAG, "FileNotFound!");
            e.printStackTrace();
        }
        finally {
            try {
                bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
                fileDescriptor.close();
            } catch (IOException e) {
                Log.d(TAG, "IOException!");
                e.printStackTrace();
            }
        }

        return bitmap;
    }

    protected <T> Observable<T> bind(Observable<T> observable) {
        Observable<T> boundObservable = AppObservable.bindActivity(this, observable)
                .observeOn(mainThread());
        return LifecycleObservable.bindActivityLifecycle(lifecycle(), boundObservable);
    }

    private Observable<LifecycleEvent> lifecycle() {
        final BehaviorSubject<LifecycleEvent> lifecycleSubject = BehaviorSubject.create();
        return lifecycleSubject.asObservable();
    }

    public MapBuilder getMapBuilder() {
        return m_builder;
    }

}
