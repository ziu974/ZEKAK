package kr.ac.konkuk.zekak.Server;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import kr.ac.konkuk.zekak.Model.ModelStatistics;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitInterface {

    @Headers("Content-Type: application/json")
    @POST("searchitem")
    Call<Void> requestNutrientInfo(@Body Map<String, String> body);

    @GET("statistics")
    Call<ModelStatistics> getStatistics(@Query("month") int month);


    //@FormUrlEncoded
    @DELETE("statistics")
    Call<Void> deleteStatistics(@Query("month") int targetMonth);


    @PUT("statistics")
    Call<String> startNewStatistics();
}

