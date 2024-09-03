package vlive.sdk.game;

import android.app.Activity;
import android.app.Application;

import vlive.sdk.core.config.BaseConfig;
import vlive.sdk.core.crypto.AndroidKeyStore;
import vlive.sdk.core.enumext.EnvironmentSdk;
import vlive.sdk.core.ultis.ActivityRetriever;
import vlive.sdk.core.ultis.SharedPreferencesSdk;


public class VLiveSdkGame {
    private static VLiveSdkGame instance;

    private VLiveSdkGame() {

    }

    public static VLiveSdkGame getInstance() {
        if (instance == null) {
            instance = new VLiveSdkGame();
        }
        return instance;
    }

    public void setEnvironment(EnvironmentSdk environment) {
        BaseConfig.environment = environment;
    }

    public void setActivity(Activity activity){
        ActivityRetriever.getInstance().initActivity(activity);
        AndroidKeyStore.generateSecretKey();
        SharedPreferencesSdk.getInstance();
    }

    private EnvironmentSdk getEnvironmentSdk() {
        return BaseConfig.environment ;
    }
}
