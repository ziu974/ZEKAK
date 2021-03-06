package kr.ac.konkuk.zekak;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.components.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


//[IMPORTANT]: 아이템 수정도 함꼐 담당하는 클래스임
public class AddItem extends AppMain {
    public final String SERVER_ADDRESS = ""; // AWS?
    public final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;

    //11.07 갤러리 사진 가져오는 것을 위해서 / 카메라 사진 촬영 가져오는 것을 위해서
    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;
    //11.15 https://developer.android.com/reference/androidx/core/content/FileProvider#SpecifyFiles
    // CODE: 포토
    private File tempFile;      // 갤러리 사진


    // PART3: DB part (use methods here for db)
    ItemsDBControl itemsDB;
    //임시:Cursor cursor;


    // Variables for saving ITEMS data
    String product = null;
    String barcode = null;
    String productExp = null;

    String name = null;
    String exp = null;
    int portionText = 0;    // 디스플레이되는 실제 portion 설정값
    int portion = -1;    // DB용 portion 설정값(format에 맞게): -1(설정안됨) / 1~10 --> 0~9로 변환되어 DB에 저장
    String category = null;
    String photo = null;
    String memo = null;
    boolean flag = false;


    private static final int NOT_NEEDED = 0;       // 아이디 아직 할당 안받았으므로
    String sessionId;
    boolean dbCheck = false;
    boolean addType;        // 아이템 추가 타입 구분>>    false: Manual    true: Barcode


    /////////////////// View, Buttons etc. //////////////////
    ImageButton btnAddImage;
    TextView viewBarcode;
    TextView viewProduct;
    EditText editItemName;
    Button btnSetExp;
    TextView viewExp;
    Spinner spinnerSetCategory;
    Button btnSetPortion;
    Switch pinItem;
    EditText viewMemo;

    ImageView btnCancelAdd;
    TextView btnAddItem;


    // [Edit Items] 아이템 수정 목적으로 이 클래스 사용할 경우
    int position;   // 메인화면에서의 tab index
    int editItemID = -1;
    int previousPortion;    // portion 이전 설정값: 1~10
    int previousUsage;      // 이전 사용값: n# 형태(n: previousPortion / #: used횟수)
    ImageView btnCancelEdit;
    TextView btnEditItem;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_add);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /////////PART0: 데이터베이스 control 위해서
        itemsDB = new ItemsDBControl(this);


        /////////PART1: 어디에서 왔는지에 따라서... (핵심)
        sessionId = getIntent().getStringExtra("EXTRA_SESSION_ID");


        BtnOnClickListener onClickListener = new BtnOnClickListener();


        ////////PART2: 레이아웃에 정의된 뷰(버튼, 텍스트필드 등) 선언 & 리스너 등록
        if(sessionId.equals("edit")){   // i) item edit
            TextView title = findViewById(R.id.label_add_item);
            title.setText("Edit Item");

            btnCancelEdit = findViewById(R.id.cancel_edit_btn);
            btnEditItem = findViewById(R.id.item_edit_btn);
            btnCancelEdit.setVisibility(View.VISIBLE);
            btnEditItem.setVisibility(View.VISIBLE);
            btnCancelEdit.setOnClickListener(onClickListener);
            btnEditItem.setOnClickListener(onClickListener);

        } else {        // ii) item add
            btnCancelAdd = findViewById(R.id.cancel_add_btn);
            btnAddItem = findViewById(R.id.item_add_btn);
            btnCancelAdd.setVisibility(View.VISIBLE);
            btnAddItem.setVisibility(View.VISIBLE);
            btnCancelAdd.setOnClickListener(onClickListener);
            btnAddItem.setOnClickListener(onClickListener);
        }

        btnAddImage = findViewById(R.id.image_item_photo);
        viewBarcode = findViewById(R.id.item_barcode);
        viewProduct = findViewById(R.id.item_product);
        editItemName = findViewById(R.id.edit_item_name);
        btnSetExp = findViewById(R.id.edit_item_exp);
        viewExp = findViewById(R.id.item_product_exp);
        spinnerSetCategory = findViewById(R.id.edit_item_category);
        btnSetPortion = findViewById(R.id.edit_item_portion);
        pinItem = findViewById(R.id.edit_item_pin);
        viewMemo =  findViewById(R.id.edit_item_memo);


        // 리스너 클래스 등록(연결)
        btnAddImage.setOnClickListener(onClickListener);
        btnSetExp.setOnClickListener(onClickListener);
        btnSetPortion.setOnClickListener(onClickListener);



        ///////// PART4: 카테고리 이름 목록(categoryList) 받아와서 어댑터(AppMain이 제공)에 연결
        spinnerSetCategory.setAdapter(categoryAdapter);
        spinnerSetCategory.setSelection(getIntent().getIntExtra("INITIAL_CATEGORY", 0));    // 사용자가 어떤 카테고리인 상태에서 아이템을 눌렀는지 (manual: AppMain / barcode: BarcodeScanningActivity에서)
        spinnerSetCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = spinnerSetCategory.getSelectedItem().toString();
                Log.i("카테고리", category);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        switch (sessionId){
            case "INFO-000":
                addType = true;

                barcode = getIntent().getStringExtra("barcodeValue");
                product = getIntent().getStringExtra("productName");
                productExp = getIntent().getStringExtra("productExp");

                // 인식된 제품의 정보를 mapping
                viewBarcode.setText(barcode);
                viewProduct.setText(product);
                viewExp.setText(productExp);
                break;

            case "INFO-200":
            case "INFO-300":
            case "manual":
                addType = false;
                break;
            case "edit":
                editItemID = getIntent().getIntExtra("ITEM_ID", 0);
                position = getIntent().getIntExtra("TAB_POSITION", -1);
                Item editItem = ITEM_MAP.get(editItemID);

                ///// [수정 전의 아이템 정보 값들 뷰에 매핑]////

                if(editItem.barcode != null){       // 수정하려는 아이템이 바코드 식재료인 경우
                    viewBarcode.setText(editItem.barcode);
                    viewProduct.setText(editItem.product);
                }

                // 사용량 무조건적인 초기화 방지를 위해 저장
                previousUsage = editItem.portion;
                previousPortion = previousUsage / 10 + 1; // 0~9의 환산되었던 값을 1~10로 되돌림
                btnSetPortion.setText(Integer.toString(previousPortion));
                portion = previousUsage;

                //(CODE: 포토) 절대경로를 임시 파일과 연결 (CODE: 포토, 절대경로로 저장 안하기로, *.jpg로)
                if(editItem.photo != null){
                    tempFile = new File(imagePath, editItem.photo);
                    try{
                        setImage(false);
                    } catch (IOException e){}

                }

                editItemName.setText(editItem.name);
                viewExp.setText(editItem.exp);
                pinItem.setChecked(editItem.flag);
                viewMemo.setText(editItem.memo);
                break;

        }




        // viewMemo: 스크롤과 관련
        viewMemo.setMovementMethod(new ScrollingMovementMethod());

        // pinItem: switch 리스너
        pinItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                flag = isChecked;
                // true: pin 기능 on
                // false: pin 기능 off
            }
        });

    }


    // 클릭 리스너 정의
    class BtnOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            Intent returnIntent = new Intent();
            switch (view.getId()){
                case R.id.image_item_photo:
                    // 아이템의 사진 첨부
                    final Dialog photoDialog = new Dialog(AddItem.this);
                    photoDialog.setContentView(R.layout.photo_dialog);

                    Button galleryPhotoBtn = photoDialog.findViewById(R.id.gallery_photo_btn);
                    Button cameraPhotoBtn = photoDialog.findViewById(R.id.camera_photo_btn);
                    Button deletePhotoBtn = photoDialog.findViewById(R.id.delete_photo_btn);

                    galleryPhotoBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getPhoto();
                            photoDialog.dismiss();
                        }
                    });
                    cameraPhotoBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            takePhoto();
                            photoDialog.dismiss();
                        }
                    });
                    deletePhotoBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            btnAddImage.setImageBitmap(null);
                            photoDialog.dismiss();
                        }
                    });
                    photoDialog.show();

                    break;
                case R.id.edit_item_exp:
                    // 캘린더 다이얼로그(모프 과제2)
                    final Calendar c = Calendar.getInstance();      // java의 캘린더 객체를 가져온다
                    int mYear = c.get(Calendar.YEAR);               // 사용자가 누른 날짜의 연, 월, 일 을 저장하기 위한 변수
                    int mMonth = c.get(Calendar.MONTH);
                    int mDay = c.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(AddItem.this,                  // '날짜 설정'의 캘린더 다이얼로그 뷰에 이벤트 리스너를 무명 클래스로 등록함
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {     // 사용자가 날짜를 누르면 이 콜백 함수가 실행됨
                                    btnSetExp.setText(year + "." + (monthOfYear+1) + "." + dayOfMonth);                 // 유통기한 날짜 format: YEAR.MM.DD

                                }
                            }, mYear, mMonth, mDay);
                    datePickerDialog.show();
                    break;

                case R.id.edit_item_category:
                    spinnerSetCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            category = categoryList.get(position).toString();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                    break;

                case R.id.edit_item_portion:
                    final Dialog portionDialog = new Dialog(AddItem.this);
                    portionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    portionDialog.setContentView(R.layout.portion_dialog);

                    Button btnSetPortionCustom = (Button) portionDialog.findViewById(R.id.portion_set_btn);
                    Button btnSetPortionDefault = (Button) portionDialog.findViewById(R.id.portion_cancel_btn);

                    final NumberPicker portionPicker = (NumberPicker) portionDialog.findViewById(R.id.portion_picker);
                    portionPicker.setMinValue(1);
                    portionPicker.setMaxValue(10);
                    portionPicker.setDescendantFocusability(NumberPicker.FOCUS_BEFORE_DESCENDANTS);
                    portionPicker.setWrapSelectorWheel(false);
                    if(sessionId.equals("edit")) {      // 수정의 경우 원래 설정값 유지
                        portionPicker.setValue(previousPortion);
                    }
                    portionPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                        @Override
                        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        }
                    });

                    btnSetPortionCustom.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            portionText = portionPicker.getValue();
                            if(sessionId.equals("edit") && (portionText == previousPortion)) {        // 지금까지 사용한 1회분 횟수 없어지는 거 방지하기 위해, 동일한 값이면 무효
                                portion = previousUsage;
                                Log.i("이전 포션 유지", String.valueOf(portion));

                            } else {
                                btnSetPortion.setText(String.valueOf(portionText));
                                portion = (portionText-1) * 10;                                       // [설명]: 사용량을 초기에 0으로 설정하기 위해, (i.e. (n-1)0 형태) + db 저장 format으로 변형
                            }
                            portionDialog.dismiss();
                        }
                    });
                    btnSetPortionDefault.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            portionDialog.dismiss();
                        }
                    });
                    portionDialog.show();
                    break;


                case R.id.cancel_add_btn:
                    // main 화면으로 돌아감
                    setResult(RESULT_CANCELED, returnIntent);
                    finish();
                    break;

                case R.id.item_add_btn:
                    // category, (product), (barcode), (exp)는 이미 전역변수에 저장되어있음
                    // flag는 onCreate()에서 리스너 통해서 저장되어있음
                    // portion은 picker 진행 시 전역변수에 저장 (line 275)
                    name = editItemName.getText().toString();
                    exp = btnSetExp.getText().toString();
                    memo = viewMemo.getText().toString();

                    Item newItem;

                    // name, exp, portion이 null이 아니라면 데이터베이스에 insert() --> main화면으로 돌아감
                    if(name.isEmpty() || exp.isEmpty() || portion==-1){
                        Toast.makeText(AddItem.this, "이름/유통기한/양 입력필수", Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        if (addType){   // Barcode Item Add
                            newItem = new Item(NOT_NEEDED, name, product, barcode, exp, portion, category, photo, memo, flag);
                            BarcodeItemAdd(newItem);
                        } else {        // Manual Item Add
                            newItem = new Item(NOT_NEEDED, name, null, null, exp, portion, category, photo, memo, flag);
                            ManualItemAdd(newItem);
                        }


                        if(dbCheck){
                            returnIntent.putExtra("NEW_ITEM", newItem);
                            setResult(ITEM_ADDED, returnIntent);
                            finish();
                        } else {
                            Log.i("zekak", "Error adding item to DB");
                            Toast.makeText(AddItem.this, "Error saving data to DB", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;


                // 아이템 수정용 파트
                case R.id.cancel_edit_btn:
                    // 원래 아이템 상세페이지 화면으로 돌아감
                    setResult(RESULT_CANCELED);
                    finish();
                    break;

                case R.id.item_edit_btn:
                    name = editItemName.getText().toString();
                    exp = btnSetExp.getText().toString();
                    memo = viewMemo.getText().toString();


                    if(name.isEmpty() || exp.isEmpty() || portion==-1){
                        Toast.makeText(AddItem.this, "이름/유통기한/양 입력필수", Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        Item updatedItem = new Item(NOT_NEEDED, name, null, null, exp, portion, category, photo, memo, flag);
                        ItemEdit(updatedItem);
                    }

                    if(dbCheck){        // item edit successful
                        Intent intent = new Intent(AddItem.this, InfoItem.class);
                        intent.putExtra("ITEM_ID", editItemID);
                        intent.putExtra("TAB_POSITION", position);      // TODO intent하는 클래스에서 준 값이니까 이미 알고 있나...
                        startActivity(intent);
                        setResult(ITEM_EDITED);
                        finish();
                        break;
                    } else {
                        Log.i("zekak", "Error updating item info to DB");
                        Toast.makeText(AddItem.this, "Error saving data to DB", Toast.LENGTH_SHORT).show();
                    }

            }
        }
    }



    public void BarcodeItemAdd(Item barcodeNewItem){
         dbCheck = itemsDB.insert(barcodeNewItem);
    }

    public void ManualItemAdd(Item manualNewItem){
         dbCheck = itemsDB.insert(manualNewItem);
    }

    public void ItemEdit (Item updatedItem){
        dbCheck = itemsDB.edit(updatedItem, editItemID);
    }

    // 갤러리 접속 또는 카메라 키기
    public void getPhoto(){
        //black-jin0427.tistory.com/120 (CODE:11.07)
        //11.29 이거 말고 black-jin0427.tistory.com/121 보삼
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(galleryIntent, PICK_FROM_ALBUM);
    }



    private void setImage(boolean makeCopy) throws IOException {
        // 사진의 절대경로 불러와 bitmap 파일로 변형 --> ImageView에 이미지 넣음
        Bitmap originalBm = null;          // 변환된 bitmap
        Bitmap croppedBm;            // 크롭된 bitmap (thumbnail용, 저장용)
        if(Build.VERSION.SDK_INT >= 29){
            try {
                originalBm = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), Uri.fromFile(tempFile))); // 이 파일 내용을 bitmap으로 decode
            } catch (IOException e){
                    e.printStackTrace();
            }
        } else {
            try {
                originalBm = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(tempFile));
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        if(originalBm != null){
            //croppedBm = Bitmap.createScaledBitmap(originalBm, 130, 130, true);
            btnAddImage.setImageBitmap(originalBm);
        }

//        if(makeCopy){       // 1. 갤러리에서 고른 사진일 경우, 자체 폴더에 복사본 저장 (for 나중 서버 추가) or 2. 카메라 촬영 사진
//            File copyFile = createImageFile();          // 복사할 file
//            FileOutputStream fOut = new FileOutputStream(copyFile);     // file's path
//            //originalBm.compress(Bitmap.CompressFormat.JPEG, 10, fOut); //  10%대 손실로 .jpeg 파일로 저장
//            //test//////////originalBm.reconfigure();
//            btnAddImage.getDrawingCache(true).compress(Bitmap.CompressFormat.JPEG, 100, fOut);
//            fOut.flush();
//            fOut.close();
//            add_photo_background.xml = copyFile.getName();
//        } else {            // 3. 아이템 수정 시 원래 사진
//            //add_photo_background.xml = tempFile.getAbsolutePath();     // 하나의 디바이스에서만 사용가능
//            // 11/15 CODE: 포토 -- 아래 걸로 바꿈 (절대경로 아닌, 그냥 "*.jpg"만)
//            add_photo_background.xml = tempFile.getName();
//        }
    }

    private void compressImage() throws IOException {       // 현재 버튼 썸네일을 파일로 저장(원본X)
        File copyFile = createImageFile();          // 복사할 file
        FileOutputStream fOut = new FileOutputStream(copyFile);
        Bitmap temp = ((BitmapDrawable) btnAddImage.getDrawable()).getBitmap();
        temp.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        fOut.flush();
        fOut.close();
        photo = copyFile.getName();
    }


    public void takePhoto() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //사진찍고+crop+rotate(-90도)+reduce size 까지 한 다음에 그거에다가 파일명 주고 저장할거라 원본은 저장X)
        startActivityForResult(cameraIntent, PICK_FROM_CAMERA);
    }

    private File createImageFile() throws IOException {     // 카메라 추가인 경우
        // 이미지 파일 이름 (CODE: 포토) 즉 DB의 photo에 들어갈 것: "zekak_(시간)*.jpg")
        String timeStamp = new SimpleDateFormat("yyMMdd_HHmm").format(new Date());
        String ImageFileName = "zekak_"+timeStamp;

        // 이미지 저장될 폴더 이름, xml/filepaths참고 (Android/data/com.example.zekak/files/)
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if(!storageDir.exists()){   // 없으면 만듦
            storageDir.mkdirs();
        }

        // 빈 파일 생성
        File image = File.createTempFile(ImageFileName, ".jpg", storageDir);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Photo from gallery
        if(requestCode == PICK_FROM_ALBUM) {
            if(data == null){
                return;
            }
            Uri photoUri = data.getData();      // 갤러리에서 선택한 이미지의 Uri 받아옴
            // (CODE: 포토) photoUri 형태: content://(도메인 com.example.zekak).fileprovider/photos/zekak_(시간).jpg.
            Cursor cursor = null;               // cursor를 통해 스키마를 content://에서 file://로 변경할 것임 (사진이 저장된 절대경로 받아오는 과정)

            try {
                String[] proj = {MediaStore.Images.Media.DATA };
                assert photoUri != null;
                cursor = getContentResolver().query(photoUri, proj, null, null, null);

                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                cursor.moveToFirst();

                tempFile = new File(cursor.getString(column_index));    // 사진을 임시파일에 저장, 뷰 thumbnail위해서
            } finally {
                if(cursor != null) {
                    cursor.close();
                }
            }

            try{
                setImage(true);         // 갤러리 사진 뷰에 적용, 복사본 생성
                compressImage();
            } catch (IOException e){}
        }

        // Photo from camera
        if(requestCode == PICK_FROM_CAMERA) {
            //Uri photoUri = data.getData();
            if (resultCode == RESULT_OK) {      // 이부분 https://developer.android.com/training/camera/photobasics?hl=ko#java 참고
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                btnAddImage.setImageBitmap(imageBitmap);
                try {
                    compressImage();     // just
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//
            // 사진이 너무 커서 크롭, 회전, 크기줄이기 다 한담에 그거 우리 어플 파일에 새로 복사 <아래 사용X>
//            try{
//                tempFile = new File(photoUri.getPath().toString());
//                setImage(true);
//            } catch (IOException e){}
        }

        // ERROR 처리
        if (resultCode != Activity.RESULT_OK) {

            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();

            if(tempFile != null) {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.e("zekak", tempFile.getAbsolutePath() + " 삭제 성공");
                        tempFile = null;
                    }
                }
            }
        }
    }

}
