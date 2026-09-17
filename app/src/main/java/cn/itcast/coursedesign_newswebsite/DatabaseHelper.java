package cn.itcast.coursedesign_newswebsite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "NewsWebsite.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT DEFAULT 'user')");

        db.execSQL("CREATE TABLE categories (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "description TEXT)");

        db.execSQL("CREATE TABLE news (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "content TEXT NOT NULL, " +
                "category_id INTEGER, " +
                "publish_time TEXT)");

        db.execSQL("CREATE TABLE comments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "news_id INTEGER, " +
                "username TEXT, " +
                "content TEXT, " +
                "time TEXT)");

        db.execSQL("CREATE TABLE announcements (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "content TEXT, " +
                "publish_time TEXT)");

        // 预置管理员账号 admin/admin123
        ContentValues cv = new ContentValues();
        cv.put("username", "admin");
        cv.put("password", "admin123");
        cv.put("role", "admin");
        db.insert("users", null, cv);

        // 预置栏目
        String[] cats = {"国内新闻", "国际新闻", "科技新闻", "体育新闻", "娱乐新闻"};
        for (String name : cats) {
            cv.clear();
            cv.put("name", name);
            cv.put("description", name + "栏目");
            db.insert("categories", null, cv);
        }

        // 预置几条新闻
        addSampleNews(db, 1, "国务院发布2026年经济工作报告",
                "国务院近日发布2026年经济工作报告，报告指出我国经济持续回升向好，高质量发展扎实推进。", "2026-06-01 09:00");
        addSampleNews(db, 1, "全国高考报名人数再创新高",
                "教育部公布2026年全国高考报名人数达1350万人，比去年增加40万人，再创历史新高。", "2026-06-01 14:30");
        addSampleNews(db, 2, "联合国气候变化大会在日内瓦召开",
                "联合国气候变化框架公约第31次缔约方大会在瑞士日内瓦开幕，来自近200个国家的代表参会。", "2026-06-02 08:00");
        addSampleNews(db, 2, "中欧班列累计开行突破10万列",
                "中国国家铁路集团宣布，中欧班列累计开行突破10万列，成为共建“一带一路”的标志性品牌。", "2026-06-02 16:00");
        addSampleNews(db, 3, "国产大模型通过图灵测试新标准",
                "国内多款AI大模型在最新图灵测试标准中取得突破性成绩，标志着我国人工智能技术达到国际领先水平。", "2026-06-01 10:00");
        addSampleNews(db, 3, "量子计算机实现1000量子比特里程碑",
                "中国科学技术大学研究团队成功研制出超过1000量子比特的量子计算原型机。", "2026-06-03 07:00");
        addSampleNews(db, 4, "2026世界杯亚洲区预选赛中国队出线",
                "中国男足在世预赛亚洲区18强赛中以小组第一身份直接晋级2026年世界杯决赛圈。", "2026-06-02 22:00");
        addSampleNews(db, 5, "2026年暑期档电影票房突破百亿",
                "据国家电影局数据，2026年暑期档电影总票房已突破100亿元，创历史同期新高。", "2026-06-03 11:00");

        // 预置公告
        cv.clear();
        cv.put("title", "新闻网正式上线公告");
        cv.put("content", "欢迎访问本新闻网！本网站提供最新、最全面的新闻资讯，涵盖国内、国际、科技、体育、娱乐等多个栏目。");
        cv.put("publish_time", "2026-06-01 00:00");
        db.insert("announcements", null, cv);

        cv.clear();
        cv.put("title", "用户须知");
        cv.put("content", "请广大用户文明上网，理性发言。评论区禁止发布违法和不良信息，违规者将封禁账号。");
        cv.put("publish_time", "2026-06-02 00:00");
        db.insert("announcements", null, cv);
    }

    private void addSampleNews(SQLiteDatabase db, int catId, String title, String content, String time) {
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("content", content);
        cv.put("category_id", catId);
        cv.put("publish_time", time);
        db.insert("news", null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS comments");
        db.execSQL("DROP TABLE IF EXISTS news");
        db.execSQL("DROP TABLE IF EXISTS categories");
        db.execSQL("DROP TABLE IF EXISTS announcements");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // ==================== 用户相关 ====================

    public long registerUser(String username, String password) {
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password", password);
        cv.put("role", "user");
        return getWritableDatabase().insert("users", null, cv);
    }

    public Cursor login(String username, String password) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM users WHERE username=? AND password=?",
                new String[]{username, password});
    }

    public boolean isUsernameExist(String username) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id FROM users WHERE username=?", new String[]{username});
        boolean exist = c.getCount() > 0;
        c.close();
        return exist;
    }

    // ==================== 栏目相关 ====================

    public Cursor getAllCategories() {
        return getReadableDatabase().rawQuery("SELECT * FROM categories ORDER BY id", null);
    }

    public long addCategory(String name, String description) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("description", description);
        return getWritableDatabase().insert("categories", null, cv);
    }

    public int updateCategory(int id, String name, String description) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("description", description);
        return getWritableDatabase().update("categories", cv, "id=?", new String[]{String.valueOf(id)});
    }

    public int deleteCategory(int id) {
        return getWritableDatabase().delete("categories", "id=?", new String[]{String.valueOf(id)});
    }

    public String getCategoryName(int id) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT name FROM categories WHERE id=?", new String[]{String.valueOf(id)});
        String name = "";
        if (c.moveToFirst()) name = c.getString(0);
        c.close();
        return name;
    }

    // ==================== 新闻相关 ====================

    public Cursor getNewsByCategory(int categoryId) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM news WHERE category_id=? ORDER BY publish_time DESC",
                new String[]{String.valueOf(categoryId)});
    }

    public Cursor getLatestNewsPerCategory() {
        return getReadableDatabase().rawQuery(
                "SELECT n.* FROM news n WHERE n.id IN (" +
                        "SELECT MAX(id) FROM news GROUP BY category_id" +
                        ") ORDER BY publish_time DESC", null);
    }

    public Cursor getAllNews() {
        return getReadableDatabase().rawQuery(
                "SELECT n.*, c.name as category_name FROM news n " +
                        "LEFT JOIN categories c ON n.category_id=c.id " +
                        "ORDER BY n.publish_time DESC", null);
    }

    public Cursor getNewsById(int id) {
        return getReadableDatabase().rawQuery(
                "SELECT n.*, c.name as category_name FROM news n " +
                        "LEFT JOIN categories c ON n.category_id=c.id " +
                        "WHERE n.id=?", new String[]{String.valueOf(id)});
    }

    public List<News> getRelatedNews(int newsId, int categoryId) {
        List<News> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM news WHERE category_id=? AND id!=? ORDER BY publish_time DESC LIMIT 5",
                new String[]{String.valueOf(categoryId), String.valueOf(newsId)});
        while (c.moveToNext()) {
            list.add(new News(c.getInt(0), c.getString(1), c.getString(2),
                    c.getInt(3), c.getString(4)));
        }
        c.close();
        return list;
    }

    public long addNews(String title, String content, int categoryId, String time) {
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("content", content);
        cv.put("category_id", categoryId);
        cv.put("publish_time", time);
        return getWritableDatabase().insert("news", null, cv);
    }

    public int updateNews(int id, String title, String content, int categoryId) {
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("content", content);
        cv.put("category_id", categoryId);
        return getWritableDatabase().update("news", cv, "id=?", new String[]{String.valueOf(id)});
    }

    public int deleteNews(int id) {
        getWritableDatabase().delete("comments", "news_id=?", new String[]{String.valueOf(id)});
        return getWritableDatabase().delete("news", "id=?", new String[]{String.valueOf(id)});
    }

    public Cursor searchNews(String keyword) {
        return getReadableDatabase().rawQuery(
                "SELECT n.*, c.name as category_name FROM news n " +
                        "LEFT JOIN categories c ON n.category_id=c.id " +
                        "WHERE n.title LIKE ? OR n.content LIKE ? " +
                        "ORDER BY n.publish_time DESC",
                new String[]{"%" + keyword + "%", "%" + keyword + "%"});
    }

    // ==================== 评论相关 ====================

    public Cursor getCommentsByNewsId(int newsId) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM comments WHERE news_id=? ORDER BY time DESC",
                new String[]{String.valueOf(newsId)});
    }

    public int getCommentCount(int newsId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM comments WHERE news_id=?", new String[]{String.valueOf(newsId)});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    public long addComment(int newsId, String username, String content, String time) {
        ContentValues cv = new ContentValues();
        cv.put("news_id", newsId);
        cv.put("username", username);
        cv.put("content", content);
        cv.put("time", time);
        return getWritableDatabase().insert("comments", null, cv);
    }

    public int deleteComment(int id) {
        return getWritableDatabase().delete("comments", "id=?", new String[]{String.valueOf(id)});
    }

    // ==================== 公告相关 ====================

    public Cursor getAllAnnouncements() {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM announcements ORDER BY publish_time DESC", null);
    }

    public long addAnnouncement(String title, String content, String time) {
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("content", content);
        cv.put("publish_time", time);
        return getWritableDatabase().insert("announcements", null, cv);
    }

    public int updateAnnouncement(int id, String title, String content) {
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("content", content);
        return getWritableDatabase().update("announcements", cv, "id=?", new String[]{String.valueOf(id)});
    }

    public int deleteAnnouncement(int id) {
        return getWritableDatabase().delete("announcements", "id=?", new String[]{String.valueOf(id)});
    }

    // ==================== 简易模型类 ====================

    public static class News {
        public int id;
        public String title, content, publishTime;
        public int categoryId;

        public News(int id, String title, String content, int categoryId, String publishTime) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.categoryId = categoryId;
            this.publishTime = publishTime;
        }
    }
}
