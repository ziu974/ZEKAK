package kr.ac.konkuk.zekak;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Settings extends AppMain {


    Boolean alertSettingBefore;
    Boolean alertSettingAfter;

    ArrayList<String> categoryListForEdit;      // 수정가능한 카테고리들('전체' 제외)
    ListView manageCategory;
    ListAdapter adapter;
    Boolean categoryChangeFlag = false; // 'true' if category list has been modified, intent result에 사용됨

    Intent returnIntent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        alertSettingBefore = getIntent().getBooleanExtra("ALERT_SETTINGS", true);
        categoryListForEdit = getIntent().getStringArrayListExtra("CATEGORY_LIST");
        categoryListForEdit.remove("CATEGORY");    // '전체' 카테고리는 삭제할 수 없음

        adapter = new ListAdapter(Settings.this);
        manageCategory = findViewById(R.id.list_category);
        manageCategory.setAdapter(adapter);


        Switch alertSettings = findViewById(R.id.alert_switch);
        alertSettings.setChecked(alertSettingBefore);
        alertSettings.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){      // alert 기능 on
                    alertSettingAfter = true;
                } else {            // alert 기능 off
                    alertSettingAfter = false;
                }
            }
        });

        Button addCategoryBtn = findViewById(R.id.add_category_btn);
        addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog createCategory = new Dialog(Settings.this);
                createCategory.setContentView(R.layout.add_category_dialog);
                EditText inputName = createCategory.findViewById(R.id.new_category_name);
                Button createBtn = createCategory.findViewById(R.id.create_category_btn);
                Button cancelBtn = createCategory.findViewById(R.id.cancel_btn);
                createBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newCategoryName = inputName.getText().toString();
                        if(!newCategoryName.isEmpty()){
                            categoryChangeFlag = true;
                            categoryListForEdit.add(newCategoryName);
                            adapter.notifyDataSetChanged();
                            // 카테고리 추가는 이렇게 함
                            Boolean result = save(newCategoryName);     // [카테고리 추가]
                            if(!result) {// 중복 이름을 가진 카테고리
                                Toast.makeText(Settings.this, "Existing Category", Toast.LENGTH_SHORT).show();
                            }
                            createCategory.dismiss();
                        }
                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createCategory.dismiss();
                    }
                });
                createCategory.show();

            }
        });

        ImageView closeBtn = findViewById(R.id.settings_close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 변경된 설정값들을 내보내고 ativity distroy
                if(alertSettingBefore != alertSettingAfter)     // 알림 설정 바꾼 경우
                    setResult(ALERT_SETTINGS_CHANGED, returnIntent);

                if(categoryChangeFlag) {       // 카테고리 목록 변경된 경우
                    categoryListForEdit.add(0,"CATEGORY");     // 아까 잠깐 뺴뒀던 전체 카테고리 다시 추가해서 보냄
                    returnIntent.putExtra("REFRESHED_CATEGORY_LIST", categoryListForEdit);
                    // TODO OR AppMain에서 다시 load() (code:5555)
                    setResult(CATEGORY_LIST_CHANGED, returnIntent);
                }
                finish();
            }
        });

    }


    private class ListAdapter extends ArrayAdapter<String>{
        private final Activity context;

        private ListAdapter(Activity context){
            super(context, R.layout.list_category_item, categoryListForEdit);
            this.context = context;
        }



        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View categoryTab = inflater.inflate(R.layout.list_category_item, null, true);
            TextView categoryName = categoryTab.findViewById(R.id.text_category_name);
            ImageButton deleteCategoryBtn = categoryTab.findViewById(R.id.delete_category_btn);
            categoryName.setText(categoryListForEdit.get(position));
            deleteCategoryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog askAgain = new Dialog(Settings.this);
                    askAgain.setContentView(R.layout.delete_category_dialog);
                    TextView targetName = askAgain.findViewById(R.id.item_name_text);
                    Button cancelBtn = askAgain.findViewById(R.id.cancel_btn);
                    Button permenentDeleteBtn = askAgain.findViewById(R.id.category_delete_btn);
                    targetName.setText(categoryName.getText()+" 카테고리를");
                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            askAgain.dismiss();
                        }
                    });
                    permenentDeleteBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {       // 총 3단계 삭제
                            // 1. 리스트에서 삭제
                            categoryListForEdit.remove(categoryName.getText());
                            adapter.notifyDataSetChanged();
                            // 2. 디비에서 삭제 --> 함께 삭제된 아이템 수 받아옴(toast 출력)
                            int dbCheck = itemsDB.delete("category", categoryName.getText().toString());
                            Toast.makeText(Settings.this, "총 "+dbCheck+" 아이템 삭제됨", Toast.LENGTH_LONG).show();
                            // 3. Shared pref에서 삭제
                            SharedPreferences.Editor editor = appData.edit();       // SharedPreferences (설정 저장용 파일) 열기
                            editor.remove(PREFS_NAME);
                            Set<String> newCategoryList = new HashSet<String>(categoryListForEdit);    // 중복 카테고리 add 방지하기 위해 HashSet 사용
                            newCategoryList.add("CATEGORY");
                            editor.putStringSet(PREFS_NAME, newCategoryList);
                            editor.apply();

                            categoryChangeFlag = true;
                            askAgain.dismiss();
                        }
                    });
                    askAgain.show();
                }
            });

            return categoryTab;
        }
    }
}
