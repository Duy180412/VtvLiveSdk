package vlive.sdk.core.domain.repository.base;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vlive.sdk.core.config.BaseConfig;
import vlive.sdk.core.domain.model.Resource;
import vlive.sdk.core.enumext.EnvironmentSdk;
import vlive.sdk.core.ultis.ActivityRetriever;
import vlive.sdk.core.ultis.LogExt;

abstract class BaseRepository {
    private static final String TAG = "BaseRepository";
    protected Retrofit retrofit;
    private Boolean isHasErrorCreateRetrofit = false;

    public BaseRepository() {
        try {
            isHasErrorCreateRetrofit = false;
            Retrofit.Builder builder = new Retrofit.Builder();
            builder.baseUrl(getBaseUrl());
            builder.addConverterFactory(GsonConverterFactory.create());
            builder.client(getHttpClient());
            retrofit = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
            LogExt.logE(TAG, "Create Retrofit Exception", e.getMessage());
            isHasErrorCreateRetrofit = true;
        }

    }

    public Boolean getHasErrorCreateRetrofit() {
        return isHasErrorCreateRetrofit;
    }

    protected Interceptor getLoggingInterceptor() {
        if (BaseConfig.environment == EnvironmentSdk.Development) {
            return new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            return new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.NONE);
        }
    }

    protected Interceptor getHeaderInterceptor() {
        return chain -> chain.proceed(chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build());
    }


    protected Interceptor getNetworkConnectionInterceptor(){
        return  new Interceptor() {
            @NonNull
            @Override
            public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
                if (!isConnected() || !isInternetAvailable()) {
                    throw new NoConnectionException();
                }
                Request.Builder builder = chain.request().newBuilder();
                return chain.proceed(builder.build());
            }
        };
    }

    public boolean isInternetAvailable() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("8.8.8.8", 53), 1500);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isConnected() {
        boolean result = false;
        Application context = ActivityRetriever.getInstance().getApplication();
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        if (networkCapabilities != null) {
            result = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
        }
        return result;
    }

    protected abstract String getBaseUrl();

    protected abstract OkHttpClient getHttpClient();

    protected <T> Resource<T> callApi(Response<T> call) {

    }
}
