package vlive.sdk.core.ultis;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ActivityRetriever implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "ActivityRetriever";
    private static ActivityRetriever instance;
    private static Application application;
    private ArrayList<Activity> activities;

    public void initActivity(Activity activity) {
        application = activity.getApplication();
        activities.add(activity);
        initApplication(application);
    }

    private void initApplication(Application application) {
        application.registerActivityLifecycleCallbacks(this);
    }


    private ActivityRetriever() {

    }

    public Activity getActivity() {
        return activities.get(0);
    }

    public Application getApplication(){
        return application;
    }

    public static ActivityRetriever getInstance() {
        if (instance == null) {
            instance = new ActivityRetriever();
        }
        return instance;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        activities.remove(activity);
    }
}
