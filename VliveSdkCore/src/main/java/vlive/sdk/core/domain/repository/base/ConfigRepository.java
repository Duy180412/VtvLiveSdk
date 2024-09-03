package vlive.sdk.core.domain.repository.base;


import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import vlive.sdk.core.domain.ultis.ApiConstants;

public class ConfigRepository extends BaseRepository {


    public ConfigRepository() {

    }

    @Override
    public String getBaseUrl() {
        return ApiConstants.CONFIG_URL_DEV;
    }

    @Override
    public OkHttpClient getHttpClient() {
       return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(getLoggingInterceptor())
                .addInterceptor(getHeaderInterceptor())
                .build();
    }

}
