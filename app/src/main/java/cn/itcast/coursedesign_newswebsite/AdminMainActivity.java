package cn.itcast.coursedesign_newswebsite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AdminMainActivity extends AppCompatActivity {

    private Button btnNewsManage, btnCategoryManage, btnAnnouncementManage, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        btnNewsManage = findViewById(R.id.btn_news_manage);
        btnCategoryManage = findViewById(R.id.btn_category_manage);
        btnAnnouncementManage = findViewById(R.id.btn_announcement_manage);
        btnLogout = findViewById(R.id.btn_logout);

        btnNewsManage.setOnClickListener(v ->
                startActivity(new Intent(this, NewsManageActivity.class)));

        btnCategoryManage.setOnClickListener(v ->
                startActivity(new Intent(this, CategoryManageActivity.class)));

        btnAnnouncementManage.setOnClickListener(v ->
                startActivity(new Intent(this, AnnouncementManageActivity.class)));

        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
