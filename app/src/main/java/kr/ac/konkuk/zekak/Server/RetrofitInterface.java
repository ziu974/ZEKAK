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
//    @FormUrlEncoded
//    @POST("searchItem")
//    Call<Void> requestNutrientInfo(@Field("itemName") String itemName,
//                                   @Field("month") int month);

    //@Headers("Content-Type: application/json")
    @POST("searchitem")
    Call<Void> requestNutrientInfo(@Body Map<String, String> body);

    @GET("statistics")
    Call<ModelStatistics> getStatistics(@Query("month") int month);


    @PUT("statistics")
    Call<String> startNewStatistics();

    @DELETE("statistics")
    Call<Void> deleteStatistics(@Field("month") int targetMonth);
}
