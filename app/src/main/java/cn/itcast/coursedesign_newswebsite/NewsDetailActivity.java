package cn.itcast.coursedesign_newswebsite;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NewsDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvCategory, tvTime, tvContent, tvCommentHeader, btnBack;
    private ListView lvRelated, lvComments;
    private EditText etComment;
    private Button btnSubmitComment;
    private DatabaseHelper dbHelper;
    private int newsId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        newsId = getIntent().getIntExtra("news_id", 0);
        username = getIntent().getStringExtra("username");
        dbHelper = new DatabaseHelper(this);

        tvTitle = findViewById(R.id.tv_title);
        tvCategory = findViewById(R.id.tv_category);
        tvTime = findViewById(R.id.tv_time);
        tvContent = findViewById(R.id.tv_content);
        tvCommentHeader = findViewById(R.id.tv_comment_header);
        lvRelated = findViewById(R.id.lv_related);
        lvComments = findViewById(R.id.lv_comments);
        etComment = findViewById(R.id.et_comment);
        btnBack = findViewById(R.id.btn_back);
        btnSubmitComment = findViewById(R.id.btn_submit_comment);

        loadNewsDetail();
        loadComments();

        btnBack.setOnClickListener(v -> finish());

        btnSubmitComment.setOnClickListener(v -> {
            String content = etComment.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show();
                return;
            }
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            dbHelper.addComment(newsId, username, content, time);
            etComment.setText("");
            Toast.makeText(this, "评论成功", Toast.LENGTH_SHORT).show();
            loadComments();
        });

        lvRelated.setOnItemClickListener((parent, view, position, id) -> {
            List<DatabaseHelper.News> related = (List<DatabaseHelper.News>) lvRelated.getTag();
            if (related != null && position < related.size()) {
                Intent intent = new Intent(this, NewsDetailActivity.class);
                intent.putExtra("news_id", related.get(position).id);
                intent.putExtra("username", username);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loadNewsDetail() {
        Cursor c = dbHelper.getNewsById(newsId);
        if (c.moveToFirst()) {
            tvTitle.setText(c.getString(1));
            tvContent.setText(c.getString(2));
            tvCategory.setText(c.getString(c.getColumnIndexOrThrow("category_name")));
            tvTime.setText(c.getString(4));
            int catId = c.getInt(3);
            c.close();

            // 相关新闻
            List<DatabaseHelper.News> related = dbHelper.getRelatedNews(newsId, catId);
            lvRelated.setTag(related);
            List<Map<String, String>> data = new ArrayList<>();
            for (DatabaseHelper.News n : related) {
                Map<String, String> item = new HashMap<>();
                item.put("title", n.title);
                item.put("time", n.publishTime);
                data.add(item);
            }
            SimpleAdapter adapter = new SimpleAdapter(this, data,
                    android.R.layout.simple_list_item_2,
                    new String[]{"title", "time"},
                    new int[]{android.R.id.text1, android.R.id.text2});
            lvRelated.setAdapter(adapter);
        } else {
            c.close();
        }
    }

    private void loadComments() {
        Cursor c = dbHelper.getCommentsByNewsId(newsId);
        int count = c.getCount();
        tvCommentHeader.setText("评论 (" + count + ")");

        List<Map<String, String>> data = new ArrayList<>();
        while (c.moveToNext()) {
            Map<String, String> item = new HashMap<>();
            item.put("user", c.getString(2));
            item.put("content", c.getString(3));
            item.put("time", c.getString(4));
            data.add(item);
        }
        c.close();

        SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.item_comment,
                new String[]{"user", "content", "time"},
                new int[]{R.id.tv_comment_user, R.id.tv_comment_content, R.id.tv_comment_time});
        lvComments.setAdapter(adapter);
    }
}
