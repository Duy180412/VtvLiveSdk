package vlive.sdk.core.domain.api;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Part;
import vlive.sdk.core.domain.response.BaseResponse;
import vlive.sdk.core.domain.ultis.ApiConstants;

public interface ConfigApi {

    @GET(ApiConstants.CONFIG_SDK)
    Response<BaseResponse<T>> getConfig(@Part("app_package") String packageId);
}
