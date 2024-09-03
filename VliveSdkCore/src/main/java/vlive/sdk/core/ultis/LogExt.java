package vlive.sdk.core.ultis;

import android.nfc.Tag;
import android.util.Log;

import vlive.sdk.core.config.BaseConfig;
import vlive.sdk.core.enumext.EnvironmentSdk;

public class LogExt {
    private static final String TAG_I = "Vlive - Info";
    private static final String TAG_E = "Vlive - Exception";

    public static void logI(String tagClass, String nameKey, String message){
        if (BaseConfig.environment == EnvironmentSdk.Development){
            Log.i(TAG_I, tagClass + " : "+nameKey + "  - " + message );
        }
    }

    public static void logE(String tagClass,String nameKey, String message){
        if (BaseConfig.environment == EnvironmentSdk.Development){
            Log.e(TAG_E, tagClass + " : "+nameKey + "  - " + message );
        }
    }
}
