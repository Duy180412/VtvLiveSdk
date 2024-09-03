package vlive.sdk.core.data;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

public class AppInfoResponse {
    @SerializedName("authen_api_base_url")
    public String authenApiBaseUrl;

    @SerializedName("payment_api_base_url")
    public String paymentApiBaseUrl;

    @SerializedName("hub_api_base_url")
    public String hubApiBaseUrl;

    @SerializedName("client_id")
    public String clientId;

    @SerializedName("client_secret")
    public String clientSecret;

    @SerializedName("api_key_recapcha")
    public String siteKey;

    @SerializedName("apple_client_id")
    public String appleClientId;

    @SerializedName("af_dev_key")
    public String afDevKey;

    @SerializedName("scope")
    public String scope;

    @SerializedName("google_web_client")
    public String googleWebClient;

    @SerializedName("package_id")
    public String packageId;

    @SerializedName("achieved_levels")
    public String[] achievedLevels;

    @SerializedName("achieved_vips")
    public String[] achievedVips;

    public String getAuthenApiBaseUrl() {
        return authenApiBaseUrl;
    }

    public void setAuthenApiBaseUrl(String authenApiBaseUrl) {
        this.authenApiBaseUrl = authenApiBaseUrl;
    }

    public String getPaymentApiBaseUrl() {
        return paymentApiBaseUrl;
    }

    public void setPaymentApiBaseUrl(String paymentApiBaseUrl) {
        this.paymentApiBaseUrl = paymentApiBaseUrl;
    }

    public String getHubApiBaseUrl() {
        return hubApiBaseUrl;
    }

    public void setHubApiBaseUrl(String hubApiBaseUrl) {
        this.hubApiBaseUrl = hubApiBaseUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public String getAppleClientId() {
        return appleClientId;
    }

    public void setAppleClientId(String appleClientId) {
        this.appleClientId = appleClientId;
    }

    public String getAfDevKey() {
        return afDevKey;
    }

    public void setAfDevKey(String afDevKey) {
        this.afDevKey = afDevKey;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getGoogleWebClient() {
        return googleWebClient;
    }

    public void setGoogleWebClient(String googleWebClient) {
        this.googleWebClient = googleWebClient;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String[] getAchievedLevels() {
        return achievedLevels;
    }

    public void setAchievedLevels(String[] achievedLevels) {
        this.achievedLevels = achievedLevels;
    }

    public String[] getAchievedVips() {
        return achievedVips;
    }

    public void setAchievedVips(String[] achievedVips) {
        this.achievedVips = achievedVips;
    }

    public boolean hasConfigNullOrEmpty(){
        return TextUtils.isEmpty(authenApiBaseUrl)||
                TextUtils.isEmpty(hubApiBaseUrl)||
                TextUtils.isEmpty(paymentApiBaseUrl)||
                TextUtils.isEmpty(clientId)||
                TextUtils.isEmpty(clientSecret)||
                TextUtils.isEmpty(siteKey)||
                TextUtils.isEmpty(appleClientId)||
                TextUtils.isEmpty(afDevKey)||
                TextUtils.isEmpty(scope)||
                TextUtils.isEmpty(googleWebClient)||
                TextUtils.isEmpty(packageId)||
                (achievedLevels == null )||
                (achievedVips == null );


    }
}
