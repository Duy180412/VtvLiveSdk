package vlive.sdk.core.factory;


import static vlive.sdk.core.ultis.Constant.DEV_CONFIG_SDK_FIREBASE;
import static vlive.sdk.core.ultis.Constant.PREF_IS_FIRST_LAUNCH;
import static vlive.sdk.core.ultis.Constant.PROD_CONFIG_SDK_FIREBASE;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.Pair;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Response;
import vlive.sdk.core.R;
import vlive.sdk.core.callback.AppInfoCallBack;
import vlive.sdk.core.config.BaseConfig;
import vlive.sdk.core.data.AppInfoResponse;
import vlive.sdk.core.domain.repository.base.ConfigRepository;
import vlive.sdk.core.domain.response.BaseResponse;
import vlive.sdk.core.enumext.EnvironmentSdk;
import vlive.sdk.core.model.AppInfo;
import vlive.sdk.core.ultis.ActivityRetriever;
import vlive.sdk.core.ultis.LogExt;
import vlive.sdk.core.ultis.SharedPreferencesSdk;


public class AppInfoFactory {
    private static AppInfoFactory appInfoFactory;
    private static AppInfo appInfoConfig;
    private final AppInfoCallBack appInfoCallBack;
    private static final String TAG = "AppInfoFactory";

    public static void init(AppInfoCallBack appInfoCallBack) {
        appInfoFactory = new AppInfoFactory(appInfoCallBack);
    }

    public static AppInfoFactory getInstance() {
        return appInfoFactory;
    }

    public AppInfo getAppInfo() {
        return appInfoConfig;
    }

    public AppInfoFactory(AppInfoCallBack appInfoCallBack) {
        this.appInfoCallBack = appInfoCallBack;
        initConfigToSharedPrefFromFirebaseDefault();
        callApiConfig();
    }

    private void getConfigFromFireBase() {
        LogExt.logI(TAG, "Call Config From Firebase", null);
        try {
            FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(3600)
                    .build();
            mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
            mFirebaseRemoteConfig.setDefaultsAsync(R.xml.vtvlive_config_sdk_game_defaults);
            mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String environment = (BaseConfig.environment == EnvironmentSdk.Production) ? PROD_CONFIG_SDK_FIREBASE : DEV_CONFIG_SDK_FIREBASE;
                    LogExt.logI(TAG, "Active Firebase", String.valueOf(task.getResult()));
                    String data = mFirebaseRemoteConfig.getString(environment);
                    LogExt.logI(TAG, "Firebase Data", data);
                    if (!TextUtils.isEmpty(data)) {
                        trySaveConfigFromFirebase(data);
                    } else {
                        setConfigFromSharedPref();
                    }
                } else {
                    setConfigFromSharedPref();
                    LogExt.logI(TAG, "Firebase Action Result", "Not Successful");
                }
            }).addOnCanceledListener(() -> {
                setConfigFromSharedPref();
                LogExt.logI(TAG, "Firebase Action", " Cancel");
            }).addOnFailureListener(e -> {
                setConfigFromSharedPref();
                if (!TextUtils.isEmpty(e.getMessage()))
                    LogExt.logE(TAG, "Firebase Action  Failure", e.getMessage());
            });
        } catch (Exception e) {
            setConfigFromSharedPref();
            e.printStackTrace();
        }
    }

    private AppInfoResponse getConfigFromXmlFireBaseDefault() {
        Activity activity = ActivityRetriever.getInstance().getActivity();
        try {
            XmlResourceParser xrp = activity.getResources().getXml(R.xml.vtvlive_config_sdk_game_defaults);
            ArrayList<Pair<String, String>> arrayList = new ArrayList<>();
            String key = "";
            while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlResourceParser.START_TAG) {
                    if (xrp.getName().equals("key")) {
                        key = xrp.nextText();
                    }
                    if (xrp.getName().equals("value")) {
                        arrayList.add(new Pair<>(key, xrp.nextText()));
                        key = "";
                    }
                }
                xrp.next();
            }
            boolean isProdEnv = BaseConfig.environment == EnvironmentSdk.Production;
            String configKey = isProdEnv ? PROD_CONFIG_SDK_FIREBASE : DEV_CONFIG_SDK_FIREBASE;
            for (Pair<String, String> pair : arrayList) {
                if (pair.first.equals(configKey)) {
                    return getAppInfoResponseFromStringJson(pair.second);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private AppInfoResponse getAppInfoResponseFromStringJson(String info) {
        try {
            String infoJson = decryptData(info);
            Type genericType = TypeToken.getParameterized(AppInfoResponse.class).getType();
            return new Gson().fromJson(infoJson, genericType);
        } catch (Exception e) {
            e.printStackTrace();
            if (!TextUtils.isEmpty(e.getMessage())) {
                LogExt.logE(TAG, "Error Convent Data " , e.getMessage());
            }
            return null;
        }

    }

    private void initConfigToSharedPrefFromFirebaseDefault() {
        try {
            Activity activity = ActivityRetriever.getInstance().getActivity();
            boolean isFirstLaunch = SharedPreferencesSdk.getInstance().getBoolean(PREF_IS_FIRST_LAUNCH, false);
            if (!isFirstLaunch) saveConfigToSharedPrefFromFireBaseDefault();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callApiConfig() {
        String packageId = ActivityRetriever.getInstance().getApplication().getPackageName();
        ConfigRepository repository = new ConfigApiRepository(packageId);
        if (configApiRepository.isHasErrorRetrofit()) {
            getConfigFromFireBase();
            return;
        }
        Call<ResponseBody> callApi = configApiRepository.callApi();
        callApi.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String text = new String(response.body().bytes());
                        LogExt.logI(TAG, "API Info " + text);
                        Gson gson = new Gson();
                        Type genericType = new TypeToken<BaseResponse<String>>() {
                        }.getType();
                        BaseResponse<String> configResponse = gson.fromJson(text, genericType);
                        String data = configResponse.getData();
                        if (!TextUtils.isEmpty(data)) {
                            trySaveConfigFromApi(data);
                        } else {
                            getConfigFromFireBase();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        getConfigFromFireBase();
                    }

                } else {
                    getConfigFromFireBase();
                    if (!TextUtils.isEmpty(response.message())) {
                        LogExt.logDe(TAG, "ErrorConventData: " + response.message());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getConfigFromFireBase();
                if (!TextUtils.isEmpty(t.getMessage())) {
                    LogExt.logDe(TAG, "ErrorCallApi: " + t.getMessage());
                }
            }
        });
    }

    private void trySaveConfigFromApi(String data) {
        try {
            String packageId = VGameSDK.getInstance().getActivity().getApplicationContext().getPackageName();
            AppInfoResponse AppInfoResponse = getAppInfoResponseFromStringJson(data);
            if (AppInfoResponse != null && !AppInfoResponse.hasConfigNullOrEmpty()) {
                String packageIdApi = AppInfoResponse.packageId;
                LogExt.logI(TAG, "Check Package Id Api: " + packageId + " : " + packageIdApi);
                if (packageId.equals(packageIdApi)) {
                    saveConfigFromRemoteAndCreateAppInfo(AppInfoResponse);
                } else {
                    getConfigFromFireBase();
                }
            } else {
                getConfigFromFireBase();
            }
        } catch (Exception e) {
            getConfigFromFireBase();
            e.printStackTrace();
            if (!TextUtils.isEmpty(e.getMessage())) LogExt.logDe(TAG, e.getMessage());
        }
    }

    private void trySaveConfigFromFirebase(String data) {
        try {
            String packageId = VGameSDK.getInstance().getActivity().getApplicationContext().getPackageName();
            AppInfoResponse AppInfoResponse = getAppInfoResponseFromStringJson(data);
            if (AppInfoResponse != null && !AppInfoResponse.hasConfigNullOrEmpty()) {
                String packageIdApi = AppInfoResponse.packageId;
                LogExt.logI(TAG, "Check Package Id FireBase: " + packageId + " : " + packageIdApi);
                if (packageId.equals(packageIdApi)) {
                    saveConfigFromRemoteAndCreateAppInfo(AppInfoResponse);
                } else {
                    setConfigFromSharedPref();
                }
            } else {
                setConfigFromSharedPref();
            }
        } catch (Exception e) {
            setConfigFromSharedPref();
            e.printStackTrace();
            if (!TextUtils.isEmpty(e.getMessage())) LogExt.logDe(TAG, e.getMessage());
        }
    }

    private void saveConfigFromRemoteAndCreateAppInfo(AppInfoResponse AppInfoResponse) {
        checkAndSaveIfNew(AppInfoResponse.authenApiBaseUrl, Constant.PREF_CONFIG_SDK_AUTHEN_API_BASE_URL);
        checkAndSaveIfNew(AppInfoResponse.paymentApiBaseUrl, Constant.PREF_CONFIG_SDK_PAYMENT_API_BASE_URL);
        checkAndSaveIfNew(AppInfoResponse.hubApiBaseUrl, Constant.PREF_CONFIG_SDK_HUB_API_BASE_URL);
        String packageId = VGameSDK.getInstance().getActivity().getApplicationContext().getPackageName();
        String packageIdApi = AppInfoResponse.packageId;
        LogExt.logI(TAG, packageId + " : " + packageIdApi);
        if (packageId.equals(packageIdApi)) {
            AppInfo appInfoLocal = createAppInfoFromSharedPref();
            AppInfo appInfoApi = createAppInfoFromApi(AppInfoResponse);
            if (appInfoApi != null) {
                checkAppInfoNew(appInfoApi, appInfoLocal);
            } else {
                callBackAppInfo(appInfoLocal);
            }
        } else setConfigFromSharedPref();
    }

    private void callBackAppInfo(AppInfo appInfo) {
        appInfo.scope = checkScopeAppInfo(appInfo);
        appInfoConfig = appInfo;
        appInfoCallBack.configAppInfoSuccessful(appInfo);
    }

    private String checkScopeAppInfo(AppInfo appInfo) {
        String scope = appInfo.scope;
        if (!TextUtils.isEmpty(scope)) {
            if (!scope.contains("openid")) scope = scope + " openid";
            if (!scope.contains("offline_access")) scope = scope + " offline_access";
        } else {
            scope = "openid offline_access";
        }
        saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_SCOPE, encryptData(scope));
        return scope;
    }

    private void setConfigFromSharedPref() {
        LogExt.logI(TAG, "Config From SharedPref");
        AppInfo appInfo = createAppInfoFromSharedPref();
        callBackAppInfo(appInfo);
    }

    private void checkAndSaveIfNew(String newData, String keySharedPref) {
        Activity activity = VGameSDK.getInstance().getActivity();
        if (TextUtils.isEmpty(newData)) return;
        String originalData = decryptData(SharedPref.getString(activity, keySharedPref));
        if (TextUtils.isEmpty(originalData) || !newData.equals(originalData)) {
            SharedPref.saveData(activity, keySharedPref, encryptData(newData));
            if (keySharedPref.equals(Constant.PREF_CONFIG_SDK_AUTHEN_API_BASE_URL)) {
                LogExt.logI(TAG, "Logout");
                SharedPref.clearSdkAll(activity);
            }
        }

    }

    private void checkAppInfoNew(AppInfo appInfoRemote, AppInfo appInfo) {
        boolean isHasNewConfig = appInfoRemote.isHasNewConfig(appInfo);
        if (isHasNewConfig) {
            callBackAppInfo(appInfoRemote);
            saveNewAppInfoFromApi(appInfoRemote);
        } else {
            callBackAppInfo(appInfo);
        }
    }

    private void saveNewAppInfoFromApi(AppInfo appInfoApi) {
        saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_CLIENT_ID, encryptData(appInfoApi.clientId));
        saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_CLIENT_SECRET, encryptData(appInfoApi.clientSecret));
        saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_SITE_KEY, encryptData(appInfoApi.siteKey));
        saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_APPLE_CLIENT_ID, encryptData(appInfoApi.appleClientId));
        saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_AF_DEV_KEY, encryptData(appInfoApi.afDevKey));
        saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_SCOPE, encryptData(appInfoApi.scope));
        saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_GOOGLE_WEB_CLIENT_ID, encryptData(appInfoApi.googleWebClientId));
        saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_PACKAGE_ID, encryptData(appInfoApi.packageId));
        saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_ACHIEVED_LEVELS, encryptData(new Gson().toJson(appInfoApi.achievedLevels)));
        saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_ACHIEVED_VIPS, encryptData(new Gson().toJson(appInfoApi.achievedVips)));
    }

    private AppInfo createAppInfoFromApi(AppInfoResponse AppInfoResponse) {
        String version = "1.0";
        try {
            Activity activity = VGameSDK.getInstance().getActivity();
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        AppInfo appInfo = new AppInfo();
        appInfo.version = version;
        appInfo.clientId = AppInfoResponse.clientId;
        appInfo.clientSecret = AppInfoResponse.clientSecret;
        appInfo.siteKey = AppInfoResponse.siteKey;
        appInfo.platformOS = "Android";
        appInfo.locale = "vi";
        appInfo.appleClientId = AppInfoResponse.appleClientId;
        appInfo.afDevKey = AppInfoResponse.afDevKey;
        appInfo.scope = AppInfoResponse.scope;
        appInfo.googleWebClientId = AppInfoResponse.googleWebClient;
        appInfo.packageId = AppInfoResponse.packageId;
        appInfo.achievedLevels = AppInfoResponse.achievedLevels;
        appInfo.achievedVips = AppInfoResponse.achievedVips;
        if (appInfo.hasConfigNull()) {
            LogExt.logI(TAG, "Has Config Null From Remote");
            return null;
        } else return appInfo;
    }

    private AppInfo createAppInfoFromSharedPref() {
        String version = "1.0";
        try {
            Activity activity = VGameSDK.getInstance().getActivity();
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        AppInfo appInfo = new AppInfo();
        appInfo.version = version;
        appInfo.clientId = decryptData(getDataFromSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_CLIENT_ID));
        appInfo.clientSecret = decryptData(getDataFromSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_CLIENT_SECRET));
        appInfo.siteKey = decryptData(getDataFromSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_SITE_KEY));
        appInfo.platformOS = "Android";
        appInfo.locale = "vi";
        appInfo.appleClientId = decryptData(getDataFromSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_APPLE_CLIENT_ID));
        appInfo.afDevKey = decryptData(getDataFromSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_AF_DEV_KEY));
        appInfo.scope = decryptData(getDataFromSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_SCOPE));
        appInfo.googleWebClientId = decryptData(getDataFromSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_GOOGLE_WEB_CLIENT_ID));
        appInfo.packageId = decryptData(getDataFromSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_PACKAGE_ID));
        String achievedLevelsJson = decryptData(getDataFromSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_ACHIEVED_LEVELS));
        appInfo.achievedLevels = jsonToStringArray(achievedLevelsJson);
        String achievedVipsJson = decryptData(getDataFromSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_ACHIEVED_VIPS));
        appInfo.achievedVips = jsonToStringArray(achievedVipsJson);
        LogExt.d(TAG, "Has Config Null When Create Config From Shared: " + appInfo.hasConfigNull());
        return appInfo;
    }

    private void saveConfigToSharedPrefFromFireBaseDefault() {
        AppInfoResponse AppInfoResponse = getConfigFromXmlFireBaseDefault();
        if (AppInfoResponse != null) {
            LogExt.d(TAG, "ConfigFromLocal: " + new Gson().toJson(AppInfoResponse));
            saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_AUTHEN_API_BASE_URL, encryptData(AppInfoResponse.authenApiBaseUrl));
            saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_PAYMENT_API_BASE_URL, encryptData(AppInfoResponse.paymentApiBaseUrl));
            saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_HUB_API_BASE_URL, encryptData(AppInfoResponse.hubApiBaseUrl));
            saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_CLIENT_ID, encryptData(AppInfoResponse.clientId));
            saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_CLIENT_SECRET, encryptData(AppInfoResponse.clientSecret));
            saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_SITE_KEY, encryptData(AppInfoResponse.siteKey));
            saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_APPLE_CLIENT_ID, encryptData(AppInfoResponse.appleClientId));
            saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_AF_DEV_KEY, encryptData(AppInfoResponse.afDevKey));
            saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_SCOPE, encryptData(AppInfoResponse.scope));
            saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_GOOGLE_WEB_CLIENT_ID, encryptData(AppInfoResponse.googleWebClient));
            saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_PACKAGE_ID, encryptData(AppInfoResponse.packageId));
            saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_ACHIEVED_LEVELS, encryptData(stringArrayToString(AppInfoResponse.achievedLevels)));
            saveConfigToSharedPref(Constant.PREF_CONFIG_SDK_APP_INFO_ACHIEVED_VIPS, encryptData(stringArrayToString(AppInfoResponse.achievedLevels)));
        }
    }

    private void saveConfigToSharedPref(String keySharePerf, String value) {
        Activity activity = VGameSDK.getInstance().getActivity();
        if (!TextUtils.isEmpty(value)) SharedPref.saveData(activity, keySharePerf, value);
    }

    private String getDataFromSharedPref(String sharedPref) {
        Activity activity = VGameSDK.getInstance().getActivity();
        return SharedPref.getString(activity, sharedPref);
    }

    private String getDataByEnvironmentFromBaseLocal(Activity activity, int prodResId, int devResId) {
        boolean isProdEnvironment = VGameSDK.getInstance().isProdEnv();
        return isProdEnvironment ? activity.getString(prodResId) : activity.getString(devResId);
    }

    private String decryptData(String data) {
        String key = AndroidKeyStore.getKey();
        try {
            return AESCryptoJS.decrypt(key, data);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String encryptData(String data) {
        String key = AndroidKeyStore.getKey();
        try {
            return AESCryptoJS.encrypt(key, data);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String[] jsonToStringArray(String dataJson) {
        try {
            return new Gson().fromJson(dataJson, String[].class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String stringArrayToString(String[] array) {
        try {
            return new Gson().toJson(array);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
