package cn.itcast.coursedesign_newswebsite;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnnouncementManageActivity extends AppCompatActivity {

    private ListView lvAnnouncements;
    private Button btnAddAnnouncement;
    private DatabaseHelper dbHelper;
    private Cursor announcementCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_manage);

        dbHelper = new DatabaseHelper(this);
        lvAnnouncements = findViewById(R.id.lv_announcements);
        btnAddAnnouncement = findViewById(R.id.btn_add_announcement);

        btnAddAnnouncement.setOnClickListener(v -> showAnnouncementEditDialog(-1, "", ""));

        loadAnnouncementList();

        lvAnnouncements.setOnItemClickListener((parent, view, position, id) -> {
            announcementCursor.moveToPosition(position);
            int annId = announcementCursor.getInt(0);
            String title = announcementCursor.getString(1);
            String content = announcementCursor.getString(2);
            showAnnouncementActionDialog(annId, title, content);
        });
    }

    private void loadAnnouncementList() {
        announcementCursor = dbHelper.getAllAnnouncements();
        List<Map<String, String>> data = new ArrayList<>();
        while (announcementCursor.moveToNext()) {
            Map<String, String> item = new HashMap<>();
            item.put("title", announcementCursor.getString(1));
            item.put("time", announcementCursor.getString(3));
            data.add(item);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, data,
                android.R.layout.simple_list_item_2,
                new String[]{"title", "time"},
                new int[]{android.R.id.text1, android.R.id.text2});
        lvAnnouncements.setAdapter(adapter);
    }

    private void showAnnouncementActionDialog(int annId, String title, String content) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(new String[]{"编辑", "删除"}, (dialog, which) -> {
                    if (which == 0) {
                        showAnnouncementEditDialog(annId, title, content);
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("确认删除")
                                .setMessage("确定要删除该公告吗？")
                                .setPositiveButton("删除", (d, w) -> {
                                    dbHelper.deleteAnnouncement(annId);
                                    Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                                    loadAnnouncementList();
                                })
                                .setNegativeButton("取消", null)
                                .show();
                    }
                })
                .show();
    }

    private void showAnnouncementEditDialog(int annId, String initTitle, String initContent) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 0);

        final EditText etTitle = new EditText(this);
        etTitle.setHint("公告标题");
        etTitle.setText(initTitle);
        layout.addView(etTitle);

        final EditText etContent = new EditText(this);
        etContent.setHint("公告内容");
        etContent.setText(initContent);
        etContent.setMinLines(3);
        layout.addView(etContent);

        String title = annId == -1 ? "添加公告" : "编辑公告";

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(layout)
                .setPositiveButton("保存", (dialog, which) -> {
                    String name = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "请输入公告标题", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (annId == -1) {
                        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                        dbHelper.addAnnouncement(name, content, time);
                        Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                    } else {
                        dbHelper.updateAnnouncement(annId, name, content);
                        Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
                    }
                    loadAnnouncementList();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (announcementCursor != null && !announcementCursor.isClosed()) announcementCursor.close();
    }
}
