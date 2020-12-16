package kr.ac.konkuk.zekak.Server;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitConnection {
    public static final String BASE_URL = "https://j1nsj4lwvf.execute-api.us-east-1.amazonaws.com/zekak/";
    public static Retrofit retrofit;


    static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    public static Retrofit getApiClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;

    }
}
