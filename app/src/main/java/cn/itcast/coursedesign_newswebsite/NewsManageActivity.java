package cn.itcast.coursedesign_newswebsite;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsManageActivity extends AppCompatActivity {

    private ListView lvNews;
    private Button btnAddNews;
    private DatabaseHelper dbHelper;
    private Cursor newsCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_manage);

        dbHelper = new DatabaseHelper(this);
        lvNews = findViewById(R.id.lv_news);
        btnAddNews = findViewById(R.id.btn_add_news);

        btnAddNews.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewsEditActivity.class);
            intent.putExtra("mode", "add");
            startActivity(intent);
        });

        loadNewsList();

        lvNews.setOnItemClickListener((parent, view, position, id) -> {
            newsCursor.moveToPosition(position);
            int newsId = newsCursor.getInt(0);
            String title = newsCursor.getString(1);
            showNewsActionDialog(newsId, title);
        });
    }

    private void loadNewsList() {
        newsCursor = dbHelper.getAllNews();
        List<Map<String, String>> data = new ArrayList<>();
        while (newsCursor.moveToNext()) {
            Map<String, String> item = new HashMap<>();
            item.put("title", newsCursor.getString(1));
            item.put("category", newsCursor.getString(newsCursor.getColumnIndexOrThrow("category_name")));
            item.put("time", newsCursor.getString(4));
            data.add(item);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.item_news_list,
                new String[]{"title", "category", "time"},
                new int[]{R.id.tv_title, R.id.tv_category, R.id.tv_time});
        lvNews.setAdapter(adapter);
    }

    private void showNewsActionDialog(int newsId, String title) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(new String[]{"编辑", "删除"}, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(this, NewsEditActivity.class);
                        intent.putExtra("mode", "edit");
                        intent.putExtra("news_id", newsId);
                        startActivity(intent);
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("确认删除")
                                .setMessage("确定要删除这条新闻吗？")
                                .setPositiveButton("删除", (d, w) -> {
                                    dbHelper.deleteNews(newsId);
                                    Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                                    loadNewsList();
                                })
                                .setNegativeButton("取消", null)
                                .show();
                    }
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNewsList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (newsCursor != null && !newsCursor.isClosed()) newsCursor.close();
    }
}
