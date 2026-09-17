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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryManageActivity extends AppCompatActivity {

    private ListView lvCategories;
    private Button btnAddCategory;
    private DatabaseHelper dbHelper;
    private Cursor categoryCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_manage);

        dbHelper = new DatabaseHelper(this);
        lvCategories = findViewById(R.id.lv_categories);
        btnAddCategory = findViewById(R.id.btn_add_category);

        btnAddCategory.setOnClickListener(v -> showCategoryEditDialog(-1, "", ""));

        loadCategoryList();

        lvCategories.setOnItemClickListener((parent, view, position, id) -> {
            categoryCursor.moveToPosition(position);
            int catId = categoryCursor.getInt(0);
            String name = categoryCursor.getString(1);
            String desc = categoryCursor.getString(2);
            showCategoryActionDialog(catId, name, desc);
        });
    }

    private void loadCategoryList() {
        categoryCursor = dbHelper.getAllCategories();
        List<Map<String, String>> data = new ArrayList<>();
        while (categoryCursor.moveToNext()) {
            Map<String, String> item = new HashMap<>();
            item.put("name", categoryCursor.getString(1));
            item.put("desc", categoryCursor.getString(2));
            data.add(item);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, data,
                android.R.layout.simple_list_item_2,
                new String[]{"name", "desc"},
                new int[]{android.R.id.text1, android.R.id.text2});
        lvCategories.setAdapter(adapter);
    }

    private void showCategoryActionDialog(int catId, String name, String desc) {
        new AlertDialog.Builder(this)
                .setTitle(name)
                .setItems(new String[]{"编辑", "删除"}, (dialog, which) -> {
                    if (which == 0) {
                        showCategoryEditDialog(catId, name, desc);
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("确认删除")
                                .setMessage("删除栏目将同时删除该栏目下的所有新闻，确定删除吗？")
                                .setPositiveButton("删除", (d, w) -> {
                                    dbHelper.deleteCategory(catId);
                                    Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                                    loadCategoryList();
                                })
                                .setNegativeButton("取消", null)
                                .show();
                    }
                })
                .show();
    }

    private void showCategoryEditDialog(int catId, String initName, String initDesc) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 0);

        final EditText etName = new EditText(this);
        etName.setHint("栏目名称");
        etName.setText(initName);
        layout.addView(etName);

        final EditText etDesc = new EditText(this);
        etDesc.setHint("栏目描述");
        etDesc.setText(initDesc);
        layout.addView(etDesc);

        String title = catId == -1 ? "添加栏目" : "编辑栏目";

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(layout)
                .setPositiveButton("保存", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String desc = etDesc.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "请输入栏目名称", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (catId == -1) {
                        dbHelper.addCategory(name, desc);
                        Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                    } else {
                        dbHelper.updateCategory(catId, name, desc);
                        Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
                    }
                    loadCategoryList();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (categoryCursor != null && !categoryCursor.isClosed()) categoryCursor.close();
    }
}
