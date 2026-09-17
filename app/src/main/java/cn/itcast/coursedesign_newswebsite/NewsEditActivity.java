package cn.itcast.coursedesign_newswebsite;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NewsEditActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etContent;
    private Spinner spinnerCategory;
    private Button btnSave;
    private TextView tvEditTitle;
    private DatabaseHelper dbHelper;
    private String mode;
    private int editNewsId;
    private List<Map<String, Integer>> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_edit);

        dbHelper = new DatabaseHelper(this);
        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        spinnerCategory = findViewById(R.id.spinner_category);
        btnSave = findViewById(R.id.btn_save);
        tvEditTitle = findViewById(R.id.tv_edit_title);

        mode = getIntent().getStringExtra("mode");

        loadCategorySpinner();

        if ("edit".equals(mode)) {
            editNewsId = getIntent().getIntExtra("news_id", 0);
            tvEditTitle.setText("编辑新闻");
            loadNewsData(editNewsId);
        } else {
            tvEditTitle.setText("添加新闻");
        }

        btnSave.setOnClickListener(v -> saveNews());
    }

    private void loadCategorySpinner() {
        Cursor c = dbHelper.getAllCategories();
        List<String> names = new ArrayList<>();
        categoryList.clear();
        while (c.moveToNext()) {
            names.add(c.getString(1));
            Map<String, Integer> map = new HashMap<>();
            map.put("id", c.getInt(0));
            categoryList.add(map);
        }
        c.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void loadNewsData(int newsId) {
        Cursor c = dbHelper.getNewsById(newsId);
        if (c.moveToFirst()) {
            etTitle.setText(c.getString(1));
            etContent.setText(c.getString(2));
            int catId = c.getInt(3);
            for (int i = 0; i < categoryList.size(); i++) {
                if (categoryList.get(i).get("id") == catId) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }
        c.close();
    }

    private void saveNews() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "请填写标题和内容", Toast.LENGTH_SHORT).show();
            return;
        }

        int catIndex = spinnerCategory.getSelectedItemPosition();
        if (catIndex < 0 || catIndex >= categoryList.size()) {
            Toast.makeText(this, "请选择栏目", Toast.LENGTH_SHORT).show();
            return;
        }
        int catId = categoryList.get(catIndex).get("id");

        if ("edit".equals(mode)) {
            dbHelper.updateNews(editNewsId, title, content, catId);
            Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
        } else {
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            dbHelper.addNews(title, content, catId, time);
            Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
