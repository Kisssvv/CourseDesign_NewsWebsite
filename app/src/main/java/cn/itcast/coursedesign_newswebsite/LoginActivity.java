package cn.itcast.coursedesign_newswebsite;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor cursor = dbHelper.login(username, password);
            if (cursor.moveToFirst()) {
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
                cursor.close();
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                if ("admin".equals(role)) {
                    startActivity(new Intent(this, AdminMainActivity.class));
                } else {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                }
                finish();
            } else {
                cursor.close();
                Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
}
