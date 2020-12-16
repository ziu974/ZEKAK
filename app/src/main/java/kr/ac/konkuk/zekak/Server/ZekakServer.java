package kr.ac.konkuk.zekak.Server;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.ac.konkuk.zekak.Server.RetrofitConnection;
import kr.ac.konkuk.zekak.Server.RetrofitInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import kr.ac.konkuk.zekak.Model.ModelStatistics;

import static kr.ac.konkuk.zekak.Model.ModelStatistics.userStatistics;

public class ZekakServer {
    private static final String TAG = "ZekakServer";
    public static RetrofitInterface retrofitInterface;

    public static int month = 0;

    public ZekakServer() {
        this.retrofitInterface = RetrofitConnection.getApiClient().create(RetrofitInterface.class);
        DateFormat dateFormat = new SimpleDateFormat("MM");
        Date date = new Date();
        Log.i("현재 Month",dateFormat.format(date));
        month = Integer.parseInt(dateFormat.format(date));
//        getStatistics();
//        requestNutrientInfo(itemName);
    }


//    public static boolean requestNutrientInfo(String itemName) {
//        boolean awsCheck = false;
//
//        JSONObject query = new JSONObject();
//        try {
//            query.put("itemName", itemName);
//            query.put("month", month);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        Call<Void> call = retrofitInterface.requestNutrientInfo();
//
//
//        call.enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                if(response.code() == 200){
//                    //awsCheck = true;
//                } else if(response.code() == 204) {
//                    Log.i("아아아ㅏㅇ", response.message());
//                } else {
//                    Log.i("코드", String.valueOf(response.code()));
//
//                    Log.i("왜", response.toString());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//               // awsCheck = false;
//                Log.i(TAG, t.getMessage());
//            }
//        });
//        return awsCheck;
//    }
//
//    public static void getStatistics() {
//        // statistics.js로 get url 날림
//        // 응답으로 서버에서 statistics 모델 형태로 json 보내줄 거임
//        // 단순히 이거 intent해주면 됨 (or return)
//        JSONObject query = new JSONObject();
//            try {
//                query.put("month", month);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//
//        Call<ModelStatistics> call = retrofitInterface.getStatistics(month);
//
//
//        call.enqueue(new Callback<ModelStatistics>() {
//            @Override
//            public void onResponse(Call<ModelStatistics> call, Response<ModelStatistics> response) {
//                if(response.code() == 200){
//                    //awsCheck = true;
//                    ModelStatistics userStatistics = response.body();
//                    setResults(userStatistics);
//                    Log.i(TAG, String.valueOf(userStatistics.month));
//
//                } else if(response.code() == 202) {
//                    Log.i("New Statistics for this month", response.message());
//                } else {    // error
//                    Log.i("CODE", String.valueOf(response.code()));
//                    Log.i("MSG", response.toString());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ModelStatistics> call, Throwable t) {
//                //awsCheck = false;
//                Log.i(TAG, t.getMessage());
//            }
//
//
//        });
//    }
//
//    private static void setResults(ModelStatistics results){
//        ModelStatistics.userStatistics = results;
//        Log.i("통계 결과--> ", userStatistics.toString());
//    }
}