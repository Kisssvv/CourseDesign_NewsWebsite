package cn.itcast.coursedesign_newswebsite;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
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
            fitListViewHeight(lvRelated);
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
        fitListViewHeight(lvComments);
    }

    /**
     * 手动修正 ListView 的高度。
     *
     * 问题背景：
     * lvComments / lvRelated 位于 ScrollView 内部，且布局里声明为 layout_height="wrap_content"。
     * ScrollView 测量子 View 时传入的 heightMeasureSpec 模式是 UNSPECIFIED（不做高度约束），
     * 而 ListView 在 UNSPECIFIED 模式下只会测量并布局第一个 item，用它推算自身高度，
     * 后续 item 不参与渲染 —— 于是出现「评论计数是对的，列表却只显示一条」这种看似矛盾的组合。
     *
     * 解决思路：
     * 遍历 adapter 里的每一个 item，逐个取出单独测量，累加出真实总高度，再写回 ListView 的
     * LayoutParams。相当于直接告诉父容器「我需要这么高」，父容器就会按这个高度分配空间。
     *
     * 注意：必须放在 setAdapter() 之后调用，此时 adapter 里的数据才是最新的。
     */
    private void fitListViewHeight(ListView listView) {
        ListAdapter adapter = listView.getAdapter();
        if (adapter == null) {
            return;
        }

        int itemCount = adapter.getCount();
        ViewGroup.LayoutParams params = listView.getLayoutParams();

        // 没有数据时把高度归零，避免评论区留下一块空白
        if (itemCount == 0) {
            params.height = 0;
            listView.setLayoutParams(params);
            return;
        }

        // 测量 item 时的宽度约束：
        // 优先取 ListView 自身宽度；若此时尚未完成布局（getWidth() 返回 0），
        // 退而使用「屏幕宽度 - 外层 ScrollView 左右各 16dp 的 padding」作为可用宽度。
        // 宽度必须给准，否则文本换行行数不对，算出来的高度也会偏。
        int width = listView.getWidth();
        if (width <= 0) {
            float density = getResources().getDisplayMetrics().density;
            width = getResources().getDisplayMetrics().widthPixels - (int) (32 * density + 0.5f);
        }
        int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        int totalHeight = 0;
        for (int i = 0; i < itemCount; i++) {
            View item = adapter.getView(i, null, listView);
            item.measure(widthSpec, heightSpec);
            totalHeight += item.getMeasuredHeight();
        }
        // 别忘了 item 之间的分隔线也要占高度
        totalHeight += listView.getDividerHeight() * (itemCount - 1);

        params.height = totalHeight;
        listView.setLayoutParams(params);
    }
}
