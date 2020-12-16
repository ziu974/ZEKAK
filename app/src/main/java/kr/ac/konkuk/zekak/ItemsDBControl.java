package kr.ac.konkuk.zekak;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import kr.ac.konkuk.zekak.Database.ItemsDBHelper;
import kr.ac.konkuk.zekak.Server.ZekakServer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemsDBControl {
    Context context;
    ItemsDBHelper helper;
    SQLiteDatabase db;
    Cursor cursor;



    public ItemsDBControl(Context context){     // 데이터베이스에 task 수행하고 싶을 때 이것의 객체 생성해서 함수 사용
        this.context = context;
        helper = new ItemsDBHelper(context);
        db = context.openOrCreateDatabase(helper.DATABASE_NAME, Context.MODE_PRIVATE, null);
    }



    public boolean insert(Item item){
        //db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(helper.NAME, item.name);
        values.put(helper.PRODUCT, item.product);
        values.put(helper.BARCODE, item.barcode);
        values.put(helper.EXP, item.exp);
        values.put(helper.PORTION, item.portion);
        values.put(helper.CATEGORY, item.category);
        values.put(helper.PHOTO, item.photo);
        values.put(helper.MEMO, item.memo);
        values.put(helper.FLAG, item.flag);

        if(db.insert(helper.TABLE_NAME, null, values) > 0){
            Log.i("데이터베이스에 데이터 insert 됨", item.name);
            Log.i("items.db - Inserted", item.name);    // TODO: 이게 더 낫나
            //db.close();
            return true;
        } else {
            //db.close();
            return  false;
        }
    }

    //11/10 더 검색할 것 있으면 searchType 케이스 추가해서 여기에 selection 설정하면 됨
    public Cursor search(String searchType, String value){
        db = helper.getWritableDatabase();
        String selection = null;
        String[] selectionArgs = {value};
        switch (searchType) {
            // 아직은 카테고리 search()만 필요해서
            case "category":
                if(value.equals("CATEGORY")){       // 전체 아이템 목록 불러오려는 경우
                    selectionArgs = null;
                } else {
                    selection = helper.CATEGORY + "= ? ";
                }
                break;
            case "item":
                selection = helper.ID + "= ? ";
                break;
            default:
                selection = null;       // define 안되면 그냥 전체 아이템 가져옴
                break;
        }
        cursor = db.query(helper.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        return cursor;
    }

    public boolean edit(Item item, int itemID){     // 아이템 상세정보 수정 시
        //db = helper.getWritableDatabase();
        String whereClause = "ID = "+ itemID;
        ContentValues values = new ContentValues();

        values.put(helper.NAME, item.name);
        values.put(helper.EXP, item.exp);
        values.put(helper.PORTION, item.portion);
        values.put(helper.CATEGORY, item.category);
        values.put(helper.PHOTO, item.photo);
        values.put(helper.MEMO, item.memo);
        values.put(helper.FLAG, item.flag);


        if(db.update(helper.TABLE_NAME, values, whereClause, null)>0){
            return true;
        } else{
            return false;
        }
    }

    public boolean usePortion(int itemID, int value) {      // 1회분 사용시 데이터베이스에 적용
        db = helper.getWritableDatabase();
        String whereClause = "ID = "+ itemID;
        ContentValues values = new ContentValues();
        values.put(helper.PORTION, value);

        if(db.update(helper.TABLE_NAME, values, whereClause, null)>0){
            return true;
        } else{
            return false;
        }
    }


    public int delete(String deleteType, String value) {
        //db = helper.getWritableDatabase();
        String selection;
        String whereClause;
        String[] selectionArgs = {value};

        switch (deleteType) {                 //11/10 더 검색할 것 있으면 searchBy 케이스 추가해서 여기에 selection 설정하면 됨
            case "item":
                selection = helper.ID;
                break;
            case "category":
                selection = helper.CATEGORY;
                break;
            default:
                selection = helper.CATEGORY;
                break;
        }

        whereClause = selection + "= ?";
        int itemsDeleted = db.delete(helper.TABLE_NAME, whereClause, selectionArgs);
        return itemsDeleted;
    }


    public boolean statistics(int itemID) { // 모두 먹은 아이템 영양성분 분석 처리 함수
        // TODO: statistics.java에 적어놓음
        // TODO 0. this.select해서 itemID로 아이템 정보 가져옴
        String itemName = " ";
        String productName = " ";
        String queryName = " ";
        ZekakServer zekakServer = new ZekakServer();
        cursor = this.search("item", Integer.toString(itemID));
        if (cursor != null) {
            Log.i("탐색할 아이템 개수", String.valueOf(cursor.getCount()));
            // 커서를 첫 번쨰 레코드로 이동한다
            if (cursor.moveToFirst()) {
                do {
                    itemName = cursor.getString(cursor.getColumnIndex(helper.NAME));
                    productName = cursor.getString(cursor.getColumnIndex(helper.PRODUCT));
                } while (cursor.moveToNext());
            }
        } else {
            Log.i("아이템을 찾지 못함", cursor.toString());
            return false;
        }

        //if(!productName.isEmpty()){     // 바코드 있는 식재료라 정확한 상품명이 있는 경우, 그 상품명으로 탐색진행
          //  queryName = productName;
        //} else {
            queryName = itemName;
       // }

        // TODO 1. aws 블라블라 처리--> 성공하면 2.
        //boolean awsCheck = true;       // TODO: 테스트를 위해 임시 --> 서버 추가되면 false로 초기화 바꾸기
        //boolean awsCheck = zekakServer.requestNutrientInfo(queryName);
        JSONObject json = new JSONObject();
        try {
            json.put("itemName", itemName);
            json.put("month", zekakServer.month);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("JSON 객체 내용", json.toString());

        Map<String, String> body = new HashMap<>();   // 아이템 관리 위해서 (key: id)
        body.put("itemName", itemName);
        body.put("month", String.valueOf(zekakServer.month));



        Call<Void> call = zekakServer.retrofitInterface.requestNutrientInfo(body);


        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.code() == 200){
                    //awsCheck = true;
                    String[] selectionArgs = {String.valueOf(itemID)};
                    int itemsDeleted = db.delete(helper.TABLE_NAME, "ID = ", selectionArgs);
                    if(itemsDeleted>0){
                        //Toast.makeText(this, "모두 먹음처리 성공", Toast.LENGTH_LONG).show();
                        Log.i("모두 먹음처리 성공", String.valueOf(itemsDeleted));
                    }
                } else if(response.code() == 204) {
                    Log.i("아아아ㅏㅇ", response.message());
                } else {
                    Log.i("코드", String.valueOf(response.code()));

                    Log.i("왜", response.toString());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // awsCheck = false;
                Log.i("TAG", t.getMessage());
            }
        });


        // 2. 내부에서 삭제 (아래코드)
//        if(awsCheck){
//            String[] selectionArgs = {String.valueOf(itemID)};
//            int itemsDeleted = db.delete(helper.TABLE_NAME, "ID = ", selectionArgs);
//            if(itemsDeleted>0){
//                return true;
//            } else return false;
//        } else {
//            Log.i("통계부분", "Something went wrong with the server");
//            return false;
//        }
        return true;
    }


    // SQLite Close
    public void db_close() {
        db.close();
        helper.close();
    }


}

