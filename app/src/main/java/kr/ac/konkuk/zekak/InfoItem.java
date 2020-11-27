package kr.ac.konkuk.zekak;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class InfoItem extends AppMain {
    int itemID;
    int position;
    Item itemInfo;

    TextView name;
    TextView category;
    ImageView pin;
    ImageView photo;
    TextView exp;
    TextView remainingDates;
    TextView portion;
    ProgressBar portionBar;
    TextView portionLabel;
    TextView memo;

    Button btnAllUsed;
    ImageButton btnEditItem;
    ImageButton btnDeleteItem;
    TextView btnCloseInfo;

    static final int EDIT_ITEM = 1;

    int initialUsage;       // 처음 아이템 사용량
    int divided;
    int used;
    int percentage;

    boolean dbCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_info);

        // PART1: 어디에서 왔는지
        // intent 엑스트라로 id 값 넘갸줄거라서 해시맵에서 이 아이디값 가지고 탐색 진행해야함
        itemID = getIntent().getIntExtra("ITEM_ID", 0);
        position = getIntent().getIntExtra("TAB_POSITION", -1);
        Log.i("Item정보", itemID+"&"+position);

        // PART2: 해시맵 탐색해서 아이템 정보 가져옴
        itemInfo = getItem();


        ///// PART3: 레이아웃 관련
        name = (TextView) findViewById(R.id.text_item_name);
        category = (TextView) findViewById(R.id.text_item_category);
        pin = (ImageView) findViewById(R.id.icon_item_pin);
        photo = (ImageView) findViewById(R.id.image_item_photo);
        exp = (TextView) findViewById(R.id.exp_date);
        remainingDates = (TextView) findViewById(R.id.text_remaining_dates);
        portion = (TextView) findViewById(R.id.text_item_portion);
        portionBar = (ProgressBar) findViewById(R.id.portion_bar);
        portionLabel = (TextView) findViewById(R.id.text_item_portion_bar);
        memo = (TextView) findViewById(R.id.item_memo);

        btnAllUsed = (Button) findViewById(R.id.all_used_btn);
        btnEditItem = (ImageButton) findViewById(R.id.edit_item_btn);
        btnDeleteItem = (ImageButton) findViewById(R.id.delete_item_btn);
        btnCloseInfo = (TextView) findViewById(R.id.item_info_close_btn);

        initialUsage = itemInfo.portion;



        //[설명] ex. DB.portion:42 --> divided(1회분설정값):5 , used(사용횟수):2
        ////    ==> 따라서 nn이면 1회분만 남은 상태(ex.44)
        divided = initialUsage / 10 + 1;   // 사용자 1회분 설정값
        used = initialUsage - ((divided-1) * 10); // 사용량
        percentage = 100 * used / divided;


        // item name
        name.setText(itemInfo.name);
        // category
        category.setText(itemInfo.category);
        // pin icon
        if(itemInfo.flag){
            pin.setVisibility(View.VISIBLE);    // OR View.INVISIBLE
        }
        // add_photo_background.xml thumbnail
        //(CODE: 포토) 이렇게 하면 이미지 잘 뜸!!! (최종 끝)
        if(itemInfo.photo != null) {
            String path = imagePath.getPath() + "/" + itemInfo.photo;
            photo.setImageBitmap(BitmapFactory.decodeFile(path));
        }

        // exp info
        exp.setText(itemInfo.exp);
        calcRemainingDates(itemInfo.exp);

        // item portion bar
        portion.setText("남은 양\n(설정된 1회분: "+divided+")");
        portionBar.setProgress(percentage);
        portionLabel.setText(used+"/"+divided);
        // memo

        memo.setText(itemInfo.memo);


        // 버튼들에 리스너 등록
        BtnOnClickListener onClickListener = new BtnOnClickListener();
        btnAllUsed.setOnClickListener(onClickListener);
        btnEditItem.setOnClickListener(onClickListener);
        btnDeleteItem.setOnClickListener(onClickListener);
        btnCloseInfo.setOnClickListener(onClickListener);
    }


    class BtnOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            Intent returnIntent = new Intent();
            switch (view.getId()){
                case R.id.all_used_btn:         // 모두 먹음 처리
                    final Dialog askAllUse = new Dialog(InfoItem.this);
                    askAllUse.setContentView(R.layout.all_used_dialog);
                    TextView itemName1 = (TextView) askAllUse.findViewById(R.id.item_name_text);
                    Button allUsedBtn1 = (Button) askAllUse.findViewById(R.id.all_used_btn);
                    Button portionUsedBtn = (Button) askAllUse.findViewById(R.id.use_portion_btn);
                    ImageView cancelBtn2 = askAllUse.findViewById(R.id.cancel_btn);

                    itemName1.setText(name.getText()+" 을(를)");
                    allUsedBtn1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dbCheck = itemsDB.statistics(itemID);           // 모두 먹은 아이템 영양성분 분석 처리 함수(해당 아이템은 삭제됨)
                            if(!dbCheck){
                                Toast.makeText(InfoItem.this, "모두먹음 처리 fail", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(InfoItem.this, "모두 먹음", Toast.LENGTH_SHORT).show();
                                returnIntent.putExtra("ITEM_ID", itemID);
                                returnIntent.putExtra("TAB_POSITION", position);
                                setResult(ITEM_USED, returnIntent);
                                askAllUse.dismiss();
                                finish();
                            }
                        }
                    });
                    if((divided - used) != 1){  // 1회분만 남은 경우에는 1회분 사용 버튼 활성화 안됨(모두먹음 처리만 가능해짐)
                        portionUsedBtn.setVisibility(View.VISIBLE);
                        portionUsedBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {   // 사용량 저장하고, progress bar 업데이트
                                initialUsage++;
                                dbCheck = itemsDB.usePortion(itemID, initialUsage);       // 1회분 사용 처리 함수
                                Log.i("변경된 사용량", String.valueOf(initialUsage));
                                if(!dbCheck) {
                                    Toast.makeText(InfoItem.this, "1회분 사용 fail", Toast.LENGTH_SHORT).show();
                                } else {
                                    // memo
                                    used++;
                                    percentage = 100 * used / divided;
                                    portionBar.setProgress(percentage, true);
                                    portionLabel.setText(used+"/"+divided);
                                    returnIntent.putExtra("ITEM_ID", itemID);
                                    returnIntent.putExtra("TAB_POSITION", position);
                                    setResult(ITEM_USED_PORTION, returnIntent);
                                }
                                askAllUse.dismiss();
                            }
                        });
                    } else {    // 1회분만 남은 경우
                        portionUsedBtn.setText("(모두 먹음만 가능)");  // 1회분 사용 invalid 하다는 것을 표현하기 위해
                        portionUsedBtn.setTextSize(10);
                    }

                    cancelBtn2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            askAllUse.dismiss();
                        }
                    });

                    askAllUse.show();
                    break;

                case R.id.edit_item_btn:        // edit item
                    Intent intent = new Intent(InfoItem.this, AddItem.class);
                    intent.putExtra("EXTRA_SESSION_ID", "edit");
                    intent.putExtra("ITEM_ID", itemID);
                    intent.putExtra("INITIAL_CATEGORY", categoryList.indexOf(itemInfo.category));
                    intent.putExtra("TAB_POSITION", position);

                    startActivityForResult(intent, EDIT_ITEM);
                    break;

                case R.id.delete_item_btn:      // delete item
                    final Dialog askAgain = new Dialog(InfoItem.this);
                    askAgain.setContentView(R.layout.delete_item_dialog);
                    TextView itemName2 = askAgain.findViewById(R.id.item_name_text);
                    Button allUsedBtn2 = askAgain.findViewById(R.id.all_used_btn);
                    Button deletePermanentBtn = askAgain.findViewById(R.id.item_delete_btn);
                    ImageView cancelBtn = askAgain.findViewById(R.id.cancel_btn);

                    itemName2.setText(name.getText()+" 을(를)");
                    allUsedBtn2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dbCheck = itemsDB.statistics(itemID);           // 모두 먹은 아이템 영양성분 분석 처리 함수(해당 아이템은 삭제됨)
                            if(!dbCheck){
                                Toast.makeText(InfoItem.this, "모두먹음 처리 fail", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(InfoItem.this, "모두 먹음", Toast.LENGTH_SHORT).show();
                                returnIntent.putExtra("ITEM_ID", itemID);
                                returnIntent.putExtra("TAB_POSITION", position);
                                setResult(ITEM_USED, returnIntent);
                                finish();
                            }
                        }
                    });
                    deletePermanentBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(itemsDB.delete("item", Integer.toString(itemID))>0){
                                // successfully deleted
                                Toast.makeText(InfoItem.this, "Successfully deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.i("zekak", "Error deleting item from DB");
                                Toast.makeText(InfoItem.this, "Error deleting", Toast.LENGTH_SHORT).show();
                            }
                            askAgain.dismiss();
                            returnIntent.putExtra("ITEM_ID", itemID);
                            returnIntent.putExtra("TAB_POSITION", position);
                            setResult(ITEM_DELETED, returnIntent);
                            finish();
                        }
                    });
                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            askAgain.dismiss();
                        }
                    });
                    askAgain.show();
                    break;

                case R.id.item_info_close_btn:  // close info activity
                    // main 화면으로 돌아감
                    setResult(RESULT_CANCELED, returnIntent);
                    finish();
                    break;

            }
        }
}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == EDIT_ITEM){
            Intent returnIntent = new Intent();
            returnIntent.putExtra("TAB_POSITION", position);
            if(resultCode == RESULT_OK){        // 아이템 수정이 되었으면 새로운 액티비티가 켜질 것이므로 이 창은 닫음
                setResult(ITEM_EDITED, returnIntent);
                finish();
            }
        }
    }

    // 잔여 유통기한 일을 계산하는 함수
    public void calcRemainingDates(String exp) {
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
            //SimpleDateFormat resultFormat = new SimpleDateFormat("MM달 dd일 남음");
            final int oneDay = 24 * 60 * 60 * 1000;

            Date present = new Date();
            Date expDate = dateFormat.parse(exp);

            Calendar now = Calendar.getInstance();
            Calendar due = Calendar.getInstance();
            now.setTime(present);
            due.setTime(expDate);

            long from = now.getTimeInMillis();
            long expire = due.getTimeInMillis();
            long remaining = (expire - from) / oneDay;
            // format안됨(1970.01.01)String remain = resultFormat.format(remaining);


            remainingDates.setText(String.valueOf(remaining)+" days left");
        }
        catch(ParseException e) {
            e.printStackTrace();
        }
    }

    // AppMain.java와 유사
    public Item getItem() {       // 특정 아이템을 데이터베이스로부터 받아오는 함수
        Cursor foundItem = itemsDB.search("item", String.valueOf(itemID));  // 데이터 베이스에서 해당 아이템을 불러옴

        // 불러온 아이템들을 Item 구조체 리스트 안에 넣음
        if (foundItem != null) {
            foundItem.moveToFirst();        // 이렇게 cursor를 맨 앞으로 이동해야 그 첫번쨰부터 가리킬 수 있음, 안그러면 마지막(포인터라고 생각히면됨)
            int id = foundItem.getInt(foundItem.getColumnIndex(itemsDB.helper.ID));
            String name = foundItem.getString(foundItem.getColumnIndex(itemsDB.helper.NAME));
            String product = foundItem.getString(foundItem.getColumnIndex(itemsDB.helper.PRODUCT));
            String barcode = foundItem.getString(foundItem.getColumnIndex(itemsDB.helper.BARCODE));
            String exp = foundItem.getString(foundItem.getColumnIndex(itemsDB.helper.EXP));
            int portion = foundItem.getInt(foundItem.getColumnIndex(itemsDB.helper.PORTION));
            String category = foundItem.getString(foundItem.getColumnIndex(itemsDB.helper.CATEGORY));
            String photo = foundItem.getString(foundItem.getColumnIndex(itemsDB.helper.PHOTO));
            String memo = foundItem.getString(foundItem.getColumnIndex(itemsDB.helper.MEMO));
            boolean pin = false;
            if (foundItem.getInt(foundItem.getColumnIndex(itemsDB.helper.FLAG)) > 0) {
                pin = true;
            }
            itemInfo = new Item(id, name, product, barcode, exp, portion, category, photo, memo, pin);

            //  해시맵에서 데이터 갱신 필요 --> AppMain에서는 ITEM_MAP을 사용해서 아이템을 관리하기 때문에
            ITEM_MAP.remove(id);
            ITEM_MAP.put(id, itemInfo);
        }
        return itemInfo;
    }
}
