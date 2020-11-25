 package kr.ac.konkuk.zekak;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ActionMenuView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import kr.ac.konkuk.zekak.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

 public class AppMain<ActivityFabBinding> extends AppCompatActivity implements CustomListAdapter.OnListItemSelectedInterface, CustomListAdapter.OnListItemLongSelectedInterface, CustomListAdapter.OnListItemSwipedInterface {
     // Request Codes for intent 객체
     static final int BARCODE_ADD = 1;
     static final int MANUAL_ADD = 2;
     static final int ITEM_INFO = 3;
    // static final int

     public static final int ITEM_ADDED = RESULT_FIRST_USER;  // 아이텝 추가 시 데이터베이스까지 입력이 잘 된 경우
     public static final int ITEM_EDITED = RESULT_FIRST_USER+1;  // 아이템 수정 시 "
     public static final int ITEM_DELETED = RESULT_FIRST_USER+2;
     public static final int ITEM_USED = RESULT_FIRST_USER+3;

     //11.07 앱 설정값 저장
     public static final String PREFS_NAME = "categories";
     public SharedPreferences appData;

     // PART3: DB part (name: ITEMS, ItemDB.java)
     ItemsDBControl itemsDB;
     public Cursor cursor;

     //11/15 (CODE: 포토) 사진 받아올 절대경로 파일 부분 변수로 저장
     // f:/TODO
     public static File imagePath;

     // 11/10 Cateogry 관련
     ArrayList<String> categoryList = new ArrayList<>();     // 식재료 아이템 카테고리 목록
     ArrayAdapter<String> categoryAdapter;                      // 의 어댑터
     public String currentCategory = "CATEGORY";        // 초기값은 '전체' 카테고리로

     // 11/10 Items 관련
     public CustomListAdapter customListAdapter;

     // CODE 9009 (검색하면 됨, 그부분 고쳐)
     //TODO: ITEMS랑 겹쳐서 일단은 둘 다 쓰지만, 둘 중 하나 골라
     public List<Item> categoryItemList;      // '특정 카테고리' 아이템 담아오는 곳
     public static List<Item> ITEMS = new ArrayList<Item>();               // '전체' 아이템 담아오는 곳 (id 제외)
     ///////9009 끝 //

     //TODO: id map에 매핑해서 저장
     public static final Map<Integer,Item> ITEM_MAP = new HashMap<Integer, Item>();   // 아이템 관리 위해서 (key: id)
     private static int COUNT = 0;                                     // 총 아이템 수 표시를 위해서


     private final static int CAMERA_PERMISSIONS_GRANTED = 100;





     // 각종 뷰( 일단은 다른 클래스에서 쓰이는 것들만 전역으로 선언)
     Spinner categoryChange;
     RecyclerView recyclerView;
     ActionMenuView menuBtn;
     private FloatingActionButton addFab, barcodeAddFab, manualAddFab;
     TextView fabText1, fabText2;
     boolean isRotate = false;


     // 교재 7장 p.6
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);


         // PART4: 각종 권한 설정 확인 및 받아옴, 라이브러리 이용
         tedPermission();

         //CODE: 포토
         //  file:/data/user/
         imagePath = getExternalFilesDir("Pictures");  // 이미지 저장된 파일 공간
         if(!imagePath.exists()){   // 없으면 만듦
             imagePath.mkdirs();
         }


         // PART2: Prefs part (설정값: 카테고리 불러오기)
         load();      // 카테고리 목록 불러옴
         categoryAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, categoryList);

         // PART3: DB - 현재 카테고리로 ItemList 받아옴
         itemsDB = new ItemsDBControl(this);


         ITEMS = getItemList(currentCategory);   // 모든 아이템을 받아와 'ITEMS'에 저장
         categoryItemList = ITEMS;

         customListAdapter = new CustomListAdapter(this, currentCategory, categoryItemList, this::onItemSelected, this::onItemLongHold, this::onItemSwipe);       // 첫 화면은 전체 아이템 볼 수 있음
         customListAdapter.setHasStableIds(true);        // 리사이클러 어댑터의 뷰 홀더에게, 고정 id 있으니 새로 모든 것을 매핑하지 않아도 된다고 알림(카테고리 변경 시  깜빡임 방지)


         // PART1: layout part
         setContentView(R.layout.activity_main);
         CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

         // 상단 바 버튼들의 이벤트 리스너 모아서 정의해둔 클래스
         BtnOnClickListener onClickListener = new BtnOnClickListener();

         // 처음 메인 페이지에서의 버튼들
         recyclerView = findViewById(R.id.itemList);
         // Set the adapter
         recyclerView.setLayoutManager(new LinearLayoutManager(this));
         recyclerView.setAdapter(customListAdapter);

         menuBtn = findViewById(R.id.menu_btn);
         menuBtn.setOnClickListener(onClickListener);
         TextView categoryItemCount = findViewById(R.id.item_count_display);
         categoryItemCount.setText(Integer.toString(COUNT));
         TextView buttooon = findViewById(R.id.category_change);

         categoryChange = findViewById(R.id.category_changeeeeeee);
         categoryChange.setAdapter(categoryAdapter);
         categoryChange.setSelection(categoryAdapter.getPosition("CATEGORY"));     // default: "CATEGORY(전체)' 카테고리, 11/17 '0'으로 하면 list의 순서가 add() 시 무작위이기 때문에 전체 카테고리로 안됨
         // 11.12 교재 p.301참고
         categoryChange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 // 선택된 카테고리에 따라 아이템 목록 다시 받아옴
                 currentCategory = categoryChange.getSelectedItem().toString();
                 categoryItemList = getItemList(currentCategory);
                 customListAdapter.notifyDataSetChanged();
                 buttooon.setText(currentCategory);
                 categoryItemCount.setText(Integer.toString(COUNT));
             }

             @Override
             public void onNothingSelected(AdapterView<?> parent) {
             }
         });

         buttooon.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 categoryChange.performClick();
             }
         });

         addFab = (FloatingActionButton) findViewById(R.id.add_item_btn);
         addFab.setOnClickListener(onClickListener);

         // addFab 버튼 inflate 되었을 떄의 버튼들
         barcodeAddFab = (FloatingActionButton) findViewById(R.id.barcode_add_btn);
         barcodeAddFab.setOnClickListener(onClickListener);
         manualAddFab = (FloatingActionButton) findViewById(R.id.manual_add_btn);
         manualAddFab.setOnClickListener(onClickListener);
         fabText1 = (TextView) findViewById(R.id.barcode_add_btn_text);
         fabText2 = (TextView) findViewById(R.id.manual_add_btn_text);
         ViewAnimation.init(barcodeAddFab);  // 처음 앱 실행시 fab 두개 숨김
         ViewAnimation.init(manualAddFab);
         ViewAnimation.init(fabText1);
         ViewAnimation.init(fabText2);
         //TODO 이 부분이랑 xml 파일 삭제하기(안씀)


         //11.17 data binding 방식 도전: gradle 설정 -> 타겟 xml과 연결
         //bi = DataBindingUtil.setContentView(this, R.layout.activity_main);


         /// TODO: 테스트용 카테고리, 지웡어ㅓ어ㅓㅇ
         // 이런식으로 카테고리 추가하면 됨
         save("test category");
         save("test category2");
         save("test category3");
         /// 여기까지 테스트

     }

     // 설정값 불러오는 함수, 처음에만 호출(안그러면 categoryList 중복되어 저장됨)
     private ArrayList<String> load() {
         appData = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
         Set<String> set = appData.getStringSet(PREFS_NAME, null);
         if(set == null){        // 전체 카테고리 없을 때 대비(App 첫 실행)
             save("CATEGORY");
         } else
             categoryList.addAll(set);

         return categoryList;
     }

     // 설정값 저장하는 함수
     private boolean save(String newCategory) {        // 지금은 카테고리만 저장하는 역할(설정값 저장할게 이것 밖에없음 아직), 나중에 알림기능이나, 통계 on/off 블라블라
         SharedPreferences.Editor editor = appData.edit();       // SharedPreferences (설정 저장용 파일) 열기
         Set<String> duplicate_prevention = new HashSet<String>(categoryList);    // 중복 카테고리 add 방지하기 위해 HashSet 사용

         if(duplicate_prevention.add(newCategory)){           // 새로운 카테고리 추가 시도, 카테고리명 중복 방지를 위해
             editor.clear();
             editor.remove(PREFS_NAME);      // 기존의 목록 삭제
             categoryList.add(newCategory);
             editor.putStringSet(PREFS_NAME, duplicate_prevention);          // 새로운 카테고리 파일에 저장 (putStringSet하면 중복 저장)

             editor.apply();                                     // 적용
             return true;
         } else          // 이미 존재하는 카테고리
             return false;
     }

 //    @Override
 //    protected void onResume() {
 //        Log.i("여기에서 리쥼을 왜해", "아");
 //        super.onResume();
 //        if(firstAttempt) {
 //            customListAdapter.notifyDataSetChanged();
 //            categoryAdapter.notifyDataSetChanged();
 //            //restartActivity();
 //            recyclerView.swapAdapter(customListAdapter, false);
 //        }
 //        firstAttempt = true;
 //    }


     @Override
     protected void onDestroy() {
         super.onDestroy();
         itemsDB.db_close();
     }

     private void restartActivity() {        // refresh the AppMain (카테고리 추가/삭제, 아이템추가 등등)
         this.recreate();
     }// 카테고리 추가되거나 삭제된 경우(Menu.java)


     @Override
     public void onItemSelected(int itemID, View v, int position) {
         Toast.makeText(this, "Single Click on position:"+position,
                 Toast.LENGTH_SHORT).show();
         Intent intent = new Intent(AppMain.this, InfoItem.class);
         intent.putExtra("ITEM_ID", itemID);
         intent.putExtra("TAB_POSITION", position);
         startActivityForResult(intent, ITEM_INFO);
     }

     @Override
     public void onItemLongHold(int itemID, View v, int position, int portion) {
         Toast.makeText(this, "Hold on position:" + position, Toast.LENGTH_SHORT).show();
         if(portion / 10 == portion - (portion / 10)) {  // 여기 1회분 사용에서는 메모리 절약을 위해서 바로 계산
             portion++;
             boolean dbCheck = itemsDB.usePortion(itemID, portion);       // 1회분 사용 처리 함수
             if (!dbCheck) {
                 Toast.makeText(this, "1회분 사용 fail", Toast.LENGTH_SHORT).show();
             } else {
                 //TODO
                 //progressBar.setProgress(used / divided * 100);
                 // TODO: refresh recycler view
             }
         }
     }

     @Override
     public void onItemSwipe(int itemID, View v, int position) {
         // TODO: 모두 먹음 혹은 삭제 처리
         Toast.makeText(this, "Swipe on position:"+position,
                 Toast.LENGTH_SHORT).show();
     }



     class BtnOnClickListener implements View.OnClickListener {
         @Override
         public void onClick(View view) {
             Intent intent = null;
             switch (view.getId()){
                 case R.id.menu_btn:
                     //getMenuInflater().inflate(R.menu.menu_scrolling, menuBtn);
                     intent = new Intent(AppMain.this, Menu.class);
                     // TODO: (edit below) 현재 페이지의 카테고리 이름, 아이템 추가시 자동으로 입력되게
                     intent.putExtra("INITIAL_CATEGORY", categoryChange.getSelectedItemPosition());
                     //startActivityForResult(intent, MENU_ADD);
                     break;


                 case R.id.add_item_btn:
                     isRotate = ViewAnimation.rotateFab(view, !isRotate);
                     if(isRotate) {  // X 모양일 때: fab 닫음
                         ViewAnimation.showIn(barcodeAddFab);
                         ViewAnimation.showIn(manualAddFab);
                         ViewAnimation.showIn(fabText1);
                         ViewAnimation.showIn(fabText2);
                     } else {        // + 모양일 때: fab 펼침
                         ViewAnimation.showOut(barcodeAddFab);
                         ViewAnimation.showOut(manualAddFab);
                         ViewAnimation.showOut(fabText1);
                         ViewAnimation.showOut(fabText2);
                     }
                     break;


                     // 버튼 3개 생성, 이 리스너 연결(안에서 자기가 자기 참조가 가능한가..?) yes11.16

                     //
                 case R.id.barcode_add_btn:
                     getCameraPermission();          // 혹시 모를 오류 방지를 위해.. 카메라 권한 체크
                     // 이제 여기에서 BarcodeScanningActivity 실행 후
                     // 인텐트--> AddItem으로 넘어가기
                     intent = new Intent(AppMain.this, BarcodeScanningActivity.class);
                     // TODO: (edit below) 현재 페이지의 카테고리 이름, 아이템 추가시 자동으로 입력되게
                     intent.putExtra("INITIAL_CATEGORY", categoryChange.getSelectedItemPosition());
                     startActivityForResult(intent, BARCODE_ADD);
                     break;


                 case R.id.manual_add_btn:
                     // 이제 여기에서 인텐트--> AddItem으로 넘어가기
                     intent = new Intent(AppMain.this, AddItem.class);
                     //intent.putExtra("INITIAL_CATEGORY", categoryList.indexOf(currentCategory)-1);
                     // TODO: 이거 categoryList.indexOf(itemInfo.category)로 위에 test처럼 바꿀까
                     intent.putExtra("INITIAL_CATEGORY", categoryChange.getSelectedItemPosition());
                     intent.putExtra("EXTRA_SESSION_ID", "manual");
                     startActivityForResult(intent, MANUAL_ADD);
                     break;

             }
         }
     }
 // TODO : 임시
     @Override
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {       // 각 클래스에서 db 처리까지 해줬으니, 여기에선 1.categoryItemList<>, 2. recyclerView adapter 갱신
         super.onActivityResult(requestCode, resultCode, data);

         switch (requestCode) {
 //            // AddItem.java
 //            case BARCODE_ADD:
 //                if (resultCode == RESULT_OK) {       // 단순히 사용자가 인식 취소 누른 경우
 //                    break;
 //                } else if (resultCode == RESULT_CANCELED) {    // 식품안전나라 탐색에서 ERROR
 //                    // 바코드가 없거나
 //                    // 카메라 권한 denied
 //                    Log.i("아아아악:Something went wrong with the API..", "식품안전나라API");
 //                }
 //                break;

             case MANUAL_ADD:        // 바코드 추가인 경우도 여기에서 처리됨
                 if ((resultCode == ITEM_ADDED)) {       // 데이터베이스에 추가까지 마친상태
                     Item newItem = (Item) data.getParcelableExtra("NEW_ITEM");
                     // TODO: 여기에 문제, 아이템 추가 안된다 리사이클러에 문제있다하고
                     //categoryItemList.add(newItem);
                     //customListAdapter.notifyItemInserted(0);
                 }
                 break;

             // TODO
             // InfoItem.java
 //            case ITEM_INFO:
 //                int itemID = data.getIntExtra("ITEM_ID", 0);
 //                int position = data.getIntExtra("TAB_POSITION", -1);
 //                if(resultCode == ITEM_EDITED) {
 //                    categoryItemList.remove(itemID);
 //                    //categoryItemList.add();
 //                    customListAdapter.notifyItemChanged(position);
 //                } else if(resultCode == ITEM_DELETED) {
 //                    customListAdapter.notifyItemRemoved(position);
 //                } else if(resultCode == ITEM_USED){
 //                    int updatedPortion = data.getIntExtra("USED_PORTION", 0);    // n# 형태의 값
 //                    categoryItemList.remove(categoryItemList.indexOf(itemID));
 //                    customListAdapter.notifyItemChanged(position);
 //
 //                }
 //                break;
         }


 //        if(requestCode == ITEM_INFO) {
 //            //임시 clickedItemId;
 //            if(resultCode == RESULT_OK) {       // 데이터베이스에서 삭제
 //                // TODO: 그 아이템을 ITEMS 리스트에서 삭제
 //                //삭제할 아이템 아이디: data.getIntExtra("DELETED_ITEM_ID", 0);
 //                //ㅣㅇ건 또 뭐야 refreshItemList();
 //
 //                switch(data.getStringExtra("ITEM_USED")){
 //                    case "all":         // 모두 먹음
 //
 //                        break;
 //                    case "portion":     // 1회분 먹음, 단순 업데이트
 //                        //ITEM_MAP.get(clickedItemId).portion =
 //                        break;
 //                }
 ////            } else {
 //            }
 //        }
     }



     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.menu_scrolling, menu);
         return true;
     }

     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle action bar item clicks here. The action bar will
         // automatically handle clicks on the Home/Up button, so long
         // as you specify a parent activity in AndroidManifest.xml.
         int id = item.getItemId();

         //noinspection SimplifiableIfStatement
         if (id == R.id.action_settings) {
             return true;
         }
         return super.onOptionsItemSelected(item);
     }


     // 카메라 권한 설정 체크를 위한 함수
     private boolean getCameraPermission() {
         if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
             return true;
         } else {        // 권한 없는 경우 사용자에게 카메라 권한 묻는 다이얼로그 띄움 --> 확인버튼 클릭 필요
             if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                     Manifest.permission.CAMERA)) {
                 Toast.makeText(this, "CAMERA PERMISSION OFF", Toast.LENGTH_SHORT).show();
             } else {
                 ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSIONS_GRANTED);
             }
             return true;
         }
     }



     public void tedPermission() {        // ted 라이브러리 사용해서 권한 체크
         PermissionListener permissionListener = new PermissionListener() {
             @Override
             public void onPermissionGranted() {
                 Toast.makeText(AppMain.this, "권한 허가", Toast.LENGTH_SHORT).show();
             }

             @Override
             public void onPermissionDenied(List<String> deniedPermissions) {
                 Toast.makeText(AppMain.this, "권한 거부: "+deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
             }
         };

         TedPermission.with(this)
                 .setPermissionListener(permissionListener)
                 .setRationaleMessage("ZEKAK 사용을 위해서는 카메라, 갤러리 접근 권한이 필요합니다")
                 .setDeniedMessage("[설정] > [권한] 에서 권한을 다시 허용할 수 있습니다")
                 .setDeniedMessage(Manifest.permission.CAMERA)
                 .setPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                         Manifest.permission.WRITE_EXTERNAL_STORAGE,
                         Manifest.permission.CAMERA,
                         Manifest.permission.INTERNET})
                 .check();
     }

     public List<Item> getItemList (String category) {       // 해당 카테고리에 있는 아이템들을 데이터베이스로부터 받아오는 함수
         COUNT = 0;
         List<Item> newITEMS = new ArrayList<Item>();
         cursor = itemsDB.search("category", category);      // (응 안해~ 다 카테고리별로만 불러올거야) 해당 카테고리의 아이템을 불러옴 ("all" / "(카테고리명)" / "eaten")

         // 불러온 아이템들을 Item 구조체 리스트 안에 넣음
         if(cursor != null){
             // 커서를 첫 번쨰 레코드로 이동한다
             if(cursor.moveToFirst()){
                 do {
                     int id = cursor.getInt(cursor.getColumnIndex(itemsDB.helper.ID));
                     String name = cursor.getString(cursor.getColumnIndex(itemsDB.helper.NAME));
                     String product = cursor.getString(cursor.getColumnIndex(itemsDB.helper.PRODUCT));
                     String barcode = cursor.getString(cursor.getColumnIndex(itemsDB.helper.BARCODE));
                     String exp = cursor.getString(cursor.getColumnIndex(itemsDB.helper.EXP));
                     int portion = cursor.getInt(cursor.getColumnIndex(itemsDB.helper.PORTION));
                     // 이미 알고있음: String category = cursor.getString(cursor.getColumnIndex(itemsDB.helper.CATEGORY));
                     String photo = cursor.getString(cursor.getColumnIndex(itemsDB.helper.PHOTO));
                     String memo = cursor.getString(cursor.getColumnIndex(itemsDB.helper.MEMO));
                     boolean pin = false;
                     if(cursor.getInt(cursor.getColumnIndex(itemsDB.helper.FLAG)) > 0){
                         pin = true;
                     }
                     Item singleItem = new Item(id, name, product, barcode, exp, portion, category, photo, memo, pin);
                     newITEMS.add(singleItem);          // 갱신될 공간에 ist<Item> 구조체에 추가

                     //  해시맵 사용, 아이템 추가 시 아이디 없이 할 수 있게 id 쌍 따로 저장, 구조체 자체에는 아이디 미포함
                     ITEM_MAP.put(id, singleItem);
                     COUNT++;
                 } while (cursor.moveToNext());
             }
         }
         return newITEMS;
     }
 }