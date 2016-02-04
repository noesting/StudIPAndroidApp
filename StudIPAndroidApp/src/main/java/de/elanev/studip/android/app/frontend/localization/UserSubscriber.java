package de.elanev.studip.android.app.frontend.localization;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpException;

import java.util.concurrent.TimeoutException;

import de.elanev.studip.android.app.backend.datamodel.User;
import de.elanev.studip.android.app.backend.net.services.StudIpLegacyApiService;
import retrofit.RetrofitError;
import rx.Subscriber;

/**
 * Created by Nils on 09.01.2016.
 */
public class UserSubscriber extends Subscriber<User> {

    private final String TAG = "UserSubscrber";

    private MapBuilder mapBuilder;
    private Context appContext;
    private LocalizedUser localizedUser;

    public UserSubscriber(MapBuilder mapBuilder, Context context) {
        super();
        this.mapBuilder = mapBuilder;
        this.appContext = context;
    }

    @Override
    public void onCompleted() {
        //mAdapter.addAll(courses);
        //courses.clear();
        //courses.clear();
        //mapBuilder.deleteUser(localizedUser);
        //Log.d(TAG, "Adding user: " + localizedUser.getName() + " : " + localizedUser.getLocation());
        //mapBuilder.addUser(localizedUser);
        mapBuilder.drawMap();

        //Log.d(TAG, "Done getting user ");
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
        localizedUser = mapBuilder.getLocalizedUser(user.userId);
        if(localizedUser != null) {
            localizedUser.setName(user.getFullName());
        }

        //localizedUser.setName(user.getFullName());

    }
}
