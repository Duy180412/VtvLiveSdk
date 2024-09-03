package vlive.sdk.core.ultis;

import static vlive.sdk.core.ultis.Constant.PREF_SDK_NAME;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesSdk {
    private static final String TAG = "SharedPreferencesSdk";
    private static SharedPreferencesSdk instance;
    private SharedPreferences sharedPreferences;

    private SharedPreferencesSdk() {
        sharedPreferences = ActivityRetriever.getInstance().getApplication().getSharedPreferences(PREF_SDK_NAME, Context.MODE_PRIVATE);
    }

    public static SharedPreferencesSdk getInstance() {
        if (instance != null) {
            instance = new SharedPreferencesSdk();
        }
        return instance;
    }

    private boolean isHasSharedPreferences() {
        return sharedPreferences != null;
    }

    public boolean getBoolean(String key, boolean valueDefault) {
        if (!isHasSharedPreferences()) {
            LogExt.logI(TAG, "return default value", String.valueOf(valueDefault));
            return valueDefault;
        }

        try {
            return sharedPreferences.getBoolean(key, valueDefault);
        } catch (NullPointerException e) {
            LogExt.logI(TAG, "return default value", String.valueOf(valueDefault));
            e.printStackTrace();
            return valueDefault;
        }
    }
}
