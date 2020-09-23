package com.sitare.retrofitdemo2;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UserClient {

    @Multipart
    @Headers("Content-Type: multipart/form-data")
    @POST("post")
    Call<ResponseBody> upload(
            @Part MultipartBody.Part file
    );
}
