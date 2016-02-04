package de.elanev.studip.android.app.frontend.localization;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpException;

import java.util.concurrent.TimeoutException;

import de.elanev.studip.android.app.backend.datamodel.User;
import de.elanev.studip.android.app.backend.datamodel.UserLocation;
import de.elanev.studip.android.app.backend.net.services.StudIpLegacyApiService;
import retrofit.RetrofitError;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Nils on 09.01.2016.
 */
public class LocationSubscriber extends Subscriber<UserLocation> {

    private final String TAG = "LocationSubscriber";

    private CompositeSubscription mCompositeSubscription;
    private MapViewerActivity mapActivity;
    private StudIpLegacyApiService mApiService;
    private String userId;
    private LocalizedUser localizedUser;
    private MapBuilder mapBuilder;
    private Context appContext;
    public int state;

    private UserSubscriber userSubscriber;

    public LocationSubscriber(CompositeSubscription sub, StudIpLegacyApiService sILAS, MapViewerActivity mVA, UserSubscriber uSub) {
        mCompositeSubscription = sub;
        mApiService = sILAS;
        mapActivity = mVA;
//        localizedUser = lU;

//        userId = lU.getId();
        mapBuilder = mVA.getMapBuilder();
        appContext = mVA.getApplicationContext();

//        userSubscriber = new UserSubscriber(mapBuilder, appContext);
        userSubscriber = uSub;
    }

    @Override
    public void onCompleted() {

        /*
        mCompositeSubscription.add(mapActivity.bind(mApiService.getUser(userId)).subscribe(
                new Subscriber<User>() {
                    @Override
                    public void onCompleted() {
                        mapBuilder.deleteUser(localizedUser);
                        Log.d(TAG, "Adding user: " + localizedUser.getName() + " : " + localizedUser.getLocation());
                        mapBuilder.addUser(localizedUser);
                        mapBuilder.drawMap();

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof TimeoutException) {
                            Toast.makeText(appContext, "Request timed out", Toast.LENGTH_SHORT).show();
                        } else if (e instanceof RetrofitError) {
                            //Toast.makeText(appContext, "Retrofit error", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        } else if (e instanceof HttpException) {
                            //Toast.makeText(appContext, "HTTP exception", Toast.LENGTH_LONG).show();
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
                        localizedUser.setName(user.getFullName());
                    }
                }
        ));*/

    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof TimeoutException) {
            Toast.makeText(appContext, "Request timed out", Toast.LENGTH_SHORT).show();
        } else if (e instanceof RetrofitError) {
            Toast.makeText(appContext, "Retrofit error", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } else if (e instanceof HttpException) {
            Toast.makeText(appContext, "HTTP exception", Toast.LENGTH_LONG).show();
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
        localizedUser = mapBuilder.getLocalizedUser(userLocation.userId);
        if(localizedUser != null) {
            Log.d(TAG, "Found a user: " + userLocation.userId);
            //mapBuilder.deleteUser(localizedUser);
            localizedUser.setLocation(userLocation.resourceId);
        } else {
            localizedUser = new LocalizedUser(userLocation.userId, userLocation.resourceId);
        }
        if(state == 1) {
            localizedUser.setState(state);
        }
        mapBuilder.addUser(localizedUser);
        mCompositeSubscription.add(mapActivity.bind(mApiService.getUser(userLocation.userId)).subscribe(userSubscriber));
        //localizedUser.setLocation(userLocation.resourceId);
    }
}
