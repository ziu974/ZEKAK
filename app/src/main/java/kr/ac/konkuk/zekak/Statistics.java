package kr.ac.konkuk.zekak;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import kr.ac.konkuk.zekak.Fragments.PieChart;
import kr.ac.konkuk.zekak.Model.ModelStatistics;
import kr.ac.konkuk.zekak.Server.ZekakServer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static kr.ac.konkuk.zekak.Model.ModelStatistics.userStatistics;

public class Statistics extends AppCompatActivity {
    // 이름 변경--> AWSstaistics? AWSNutrients?

    //Server: AWS, (class 이름: Statistics.java)
    // '모두먹음'카테고리 관리하기 위해(sqlite용량한계,장기적관점),
    //
    // [과정]
    // 시작:'모두먹음'버튼이 눌려졌을 떄 그 클래스에서 Statistics 객체 써서 진행
    // TODO:영양성분 api 사용해서 모두먹음 처리된 아이템의 영양성분 추출(이름으로 검색) -->
    // TODO:(파일형태??에 저장) -->
    // TODO:그 파일을 aws 서버에 업로드 (성공하면 로컬 ItemsDB에서 delete()진행)-->
    // <aws쪽구현>서버 상에서 람다코드 돌려서 자신의 데베에 저장 -->
    // <aws쪽구현> 서버 데베 column: id/탄/단/지/트랜 등 -->
    // TODO: local: 통계 탭 누를 시 서버에서 이 자료 다 긁어와서 띄워줌 //[끝!]
    TextView info;

    private boolean statisticsVersion = true; // true: 파이차트(영양소 총) / false: 바 차트(칼로리, 개인정보 대비 섭취 퍼센트량 만족도)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        ZekakServer zekakServer = new ZekakServer();

        // Get statistics info from Server
        //zekakServer.getStatistics();
//        JSONObject query = new JSONObject();
//        try {
//            query.put("month", month);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }


        Call<ModelStatistics> call = zekakServer.retrofitInterface.getStatistics(zekakServer.month);


        call.enqueue(new Callback<ModelStatistics>() {
            @Override
            public void onResponse(Call<ModelStatistics> call, Response<ModelStatistics> response) {
                if(response.code() == 200){
                    //awsCheck = true;
                    ModelStatistics userStatistics = response.body();
                    setResults(userStatistics);
                    Log.i("666", String.valueOf(userStatistics.month));

                } else if(response.code() == 201) {
                    Log.i("이번 달의 통계 새로 만들어짐", response.message());

                } else {    // error
                    Log.i("코드6", String.valueOf(response.code()));
                    Log.i("왜", response.toString());
                }
            }

            @Override
            public void onFailure(Call<ModelStatistics> call, Throwable t) {
                //awsCheck = false;
                Log.i("오류", t.getMessage());
            }


        });



        // TODO: 차트로 띄워주기, 지금은 임시로 그냥 텍스트로만 디스플레이 해줌
        // todo 근데 이거 설마 또 스레드 문제 때문에 기다려야되려나..oo

        info = findViewById(R.id.thisMonthStats);

        if(userStatistics != null){     // 해당 달의 통계자료 있는 경우
            Log.i("아","아아아");
            info.setText(String.valueOf(userStatistics.month) + userStatistics.calories + userStatistics.carbohydrate);
        }

        ImageView cancelBtn = findViewById(R.id.statistics_close_btn);

        // 차트 프래그먼트 추가
//        FragmentManager fm = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fm.beginTransaction();
//        fragmentTransaction.add(R.id.fragmentBorC, new PieChart());
//        fragmentTransaction.commit();

        ImageView SOMETHONG = findViewById(R.id.statistics_version1_btn);
        SOMETHONG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment();
            }
        });


        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    private void setResults(ModelStatistics results){
        ModelStatistics.userStatistics = results;
        info.setText("총 칼로리: "+ userStatistics.calories + "\n탄수화물: "+ userStatistics.carbohydrate);
        Log.i("통계 결과--> ", userStatistics.toString());
    }


    public void switchFragment() {
        Fragment fragment;

        if(statisticsVersion) {
            fragment = new PieChart();
        } else {
            //TODO
            // fragment = new BarChart();
        }

        statisticsVersion = (statisticsVersion) ? false : true;

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.add(R.id.fragmentBorC, new PieChart());
        fragmentTransaction.commit();
    }


}
