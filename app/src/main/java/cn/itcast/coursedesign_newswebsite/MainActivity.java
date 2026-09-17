package cn.itcast.coursedesign_newswebsite;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText etSearch;
    private TextView btnSearch;
    private Button btnAnnouncements, btnLogout;
    private LinearLayout llCategories;
    private ListView lvNews;
    private DatabaseHelper dbHelper;
    private String username;
    private int currentCategoryId = -1; // -1 表示全部
    private Cursor newsCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = getIntent().getStringExtra("username");
        dbHelper = new DatabaseHelper(this);

        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);
        btnAnnouncements = findViewById(R.id.btn_announcements);
        btnLogout = findViewById(R.id.btn_logout);
        llCategories = findViewById(R.id.ll_categories);
        lvNews = findViewById(R.id.lv_news);

        loadCategories();
        loadNews(-1);

        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            if (keyword.isEmpty()) {
                Toast.makeText(this, "请输入搜索关键词", Toast.LENGTH_SHORT).show();
                return;
            }
            newsCursor = dbHelper.searchNews(keyword);
            refreshNewsList();
        });

        btnAnnouncements.setOnClickListener(v -> showAnnouncementsDialog());

        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        lvNews.setOnItemClickListener((parent, view, position, id) -> {
            newsCursor.moveToPosition(position);
            int newsId = newsCursor.getInt(0);
            Intent intent = new Intent(this, NewsDetailActivity.class);
            intent.putExtra("news_id", newsId);
            intent.putExtra("username", username);
            startActivity(intent);
        });
    }

    private void loadCategories() {
        llCategories.removeAllViews();
        // "全部"按钮
        Button btnAll = createCategoryButton("全部", -1);
        llCategories.addView(btnAll);

        Cursor c = dbHelper.getAllCategories();
        while (c.moveToNext()) {
            int id = c.getInt(0);
            String name = c.getString(1);
            Button btn = createCategoryButton(name, id);
            llCategories.addView(btn);
        }
        c.close();
    }

    private Button createCategoryButton(String name, int categoryId) {
        Button btn = new Button(this);
        btn.setText(name);
        btn.setTextSize(12);
        btn.setMinWidth(0);
        btn.setPadding(16, 4, 16, 4);
        btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                categoryId == currentCategoryId ? 0xFF1A73E8 : 0xFFE0E0E0));
        btn.setTextColor(categoryId == currentCategoryId ? 0xFFFFFFFF : 0xFF616161);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(4, 0, 4, 0);
        btn.setLayoutParams(params);
        btn.setOnClickListener(v -> {
            currentCategoryId = categoryId;
            loadNews(categoryId);
            refreshCategoryButtons();
        });
        return btn;
    }

    private void refreshCategoryButtons() {
        for (int i = 0; i < llCategories.getChildCount(); i++) {
            Button btn = (Button) llCategories.getChildAt(i);
            Object tag = btn.getTag();
            // We'll just reload categories
        }
        loadCategories();
        loadNews(currentCategoryId);
    }

    private void loadNews(int categoryId) {
        if (categoryId == -1) {
            newsCursor = dbHelper.getAllNews();
        } else {
            newsCursor = dbHelper.getNewsByCategory(categoryId);
        }
        refreshNewsList();
    }

    private void refreshNewsList() {
        List<Map<String, String>> data = new ArrayList<>();
        if (newsCursor != null) {
            newsCursor.moveToPosition(-1);
            while (newsCursor.moveToNext()) {
                Map<String, String> item = new HashMap<>();
                item.put("title", newsCursor.getString(1));
                String catName;
                if (newsCursor.getColumnIndex("category_name") != -1) {
                    catName = newsCursor.getString(newsCursor.getColumnIndexOrThrow("category_name"));
                } else {
                    catName = dbHelper.getCategoryName(newsCursor.getInt(3));
                }
                item.put("category", catName);
                item.put("time", newsCursor.getString(4));
                data.add(item);
            }
        }

        SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.item_news_list,
                new String[]{"title", "category", "time"},
                new int[]{R.id.tv_title, R.id.tv_category, R.id.tv_time});
        lvNews.setAdapter(adapter);
    }

    private void showAnnouncementsDialog() {
        Cursor c = dbHelper.getAllAnnouncements();
        List<String> items = new ArrayList<>();
        while (c.moveToNext()) {
            items.add("【" + c.getString(3) + "】\n" + c.getString(1) + "\n" + c.getString(2));
        }
        c.close();

        if (items.isEmpty()) {
            Toast.makeText(this, "暂无公告", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("站内公告");
        builder.setItems(items.toArray(new String[0]), null);
        builder.setPositiveButton("关闭", null);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (newsCursor != null && !newsCursor.isClosed()) newsCursor.close();
    }
}
