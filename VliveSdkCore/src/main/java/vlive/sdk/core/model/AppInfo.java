package vlive.sdk.core.model;


import android.text.TextUtils;

import java.util.Arrays;

public class AppInfo {
    //client id and client secret for partner
    public String clientId;
    public String clientSecret;
    public String googleWebClientId;
    public String packageId;
    public String platformOS;
    public String version;
    public String locale;
    public String afDevKey;
    public String[] achievedLevels;
    public String[] achievedVips;
    public String scope;
    public String appleClientId;
    public String siteKey;

    //link config for game
    public String hotLinkHomepage;
    public String hotLinkFanpage;
    public String hotLinkGroup;
    public String hotLinkChat;
    public String hotline;

    public boolean hasConfigNull() {
        return TextUtils.isEmpty(clientId) ||
                TextUtils.isEmpty(clientSecret) ||
                TextUtils.isEmpty(googleWebClientId) ||
                TextUtils.isEmpty(packageId) ||
                TextUtils.isEmpty(platformOS) ||
                TextUtils.isEmpty(version) ||
                TextUtils.isEmpty(locale) ||
                TextUtils.isEmpty(afDevKey) ||
                TextUtils.isEmpty(scope) ||
                TextUtils.isEmpty(appleClientId) ||
                TextUtils.isEmpty(scope) ||
                achievedVips == null || achievedLevels == null;
    }

    public boolean isHasNewConfig(AppInfo appInfo){
        return  !clientId.equals(appInfo.clientId) ||
                !clientSecret.equals(appInfo.clientSecret) ||
                !googleWebClientId.equals(appInfo.googleWebClientId) ||
                !packageId.equals(appInfo.packageId) ||
                !platformOS.equals(appInfo.platformOS) ||
                !afDevKey.equals(appInfo.afDevKey) ||
                !scope.equals(appInfo.scope) ||
                !appleClientId.equals(appInfo.appleClientId) ||
                !siteKey.equals(appInfo.siteKey) ||
                !Arrays.equals(achievedLevels, appInfo.achievedLevels) ||
                !Arrays.equals(achievedVips, appInfo.achievedVips);
    }
}
