# 新闻网 · Android 新闻资讯客户端

> 移动应用软件开发课程设计 · 个人独立完成
> Java + Android SDK + SQLite，数据全部存储在本地，无需服务端即可完整运行

## 项目简介

一个完整的新闻资讯应用，包含普通用户与管理员两种角色。用户端可以按栏目浏览新闻、关键词搜索、查看正文与相关推荐、发表评论；管理端可以维护新闻、栏目和公告。全部数据通过 SQLite 落在本地，首次启动自动建表并填充示例数据，装上就能跑。

## 功能特性

### 用户端
- 注册与登录，账号密码校验，角色自动区分普通用户 / 管理员
- 首页按栏目切换新闻列表，展示每条新闻的标题、所属栏目与发布时间
- 关键词搜索，同时匹配标题与正文内容
- 新闻详情页：正文全文、发布信息、同栏目相关新闻推荐（最多 5 条，点击可直接跳转）
- 评论区发表评论，提交后列表与评论计数实时刷新

### 管理员端
- 新闻管理：新增、编辑、删除，删除新闻时级联清理其下所有评论
- 栏目管理：新增、编辑、删除栏目
- 公告管理：发布、编辑、删除公告

## 技术栈

| 项目 | 说明 |
| --- | --- |
| 开发语言 | Java |
| 运行平台 | Android，minSdk 24（Android 7.0）/ targetSdk 36 |
| 数据存储 | SQLite（SQLiteOpenHelper + ContentValues / Cursor） |
| UI 实现 | XML 布局 + ListView + SimpleAdapter |
| 依赖库 | AndroidX AppCompat、Material Components、ConstraintLayout |

未引入任何网络库、ORM 框架或第三方数据库组件，全部基于 Android 原生 API 实现。

## 数据库设计

共 5 张表，随应用首次启动自动创建：

| 表名 | 用途 | 字段 |
| --- | --- | --- |
| `users` | 用户账号与角色 | id, username, password, role |
| `categories` | 新闻栏目 | id, name, description |
| `news` | 新闻正文 | id, title, content, category_id, publish_time |
| `comments` | 评论 | id, news_id, username, content, time |
| `announcements` | 公告 | id, title, content, publish_time |

初始化时预置管理员账号、5 个栏目（国内 / 国际 / 科技 / 体育 / 娱乐）、8 条示例新闻与 2 条公告。新闻列表与详情通过 `news` 与 `categories` 左连接查询，一次取回栏目名称，避免在 Adapter 中反复查库。

## 项目结构

```
app/src/main/java/cn/itcast/coursedesign_newswebsite/
├── DatabaseHelper.java              数据访问层：建表、预置数据、全部增删改查
├── LoginActivity.java               登录
├── RegisterActivity.java            注册
├── MainActivity.java                用户端首页：栏目切换、新闻列表、搜索
├── NewsDetailActivity.java          新闻详情：正文、相关新闻、评论区
├── AdminMainActivity.java           管理端首页
├── NewsManageActivity.java          新闻管理列表
├── NewsEditActivity.java            新闻新增 / 编辑
├── CategoryManageActivity.java      栏目管理
└── AnnouncementManageActivity.java  公告管理
```

## 默认账号

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `admin123` |
| 普通用户 | 在注册页自行注册 | — |

## 运行方式

1. 用 Android Studio 打开项目根目录
2. 等待 Gradle 同步完成（`local.properties` 里的 SDK 路径由 IDE 自动生成，未纳入版本控制）
3. 连接真机或启动模拟器（需 Android 7.0 及以上）
4. 点击 Run

## 开发中定位的一个典型问题

### ListView 嵌套在 ScrollView 中，评论列表只渲染首条数据

**现象**

同一篇新闻下发表多条评论后，评论区标题显示「评论 (3)」，但列表里只出现 1 条评论。

**根因**

`activity_news_detail.xml` 中的 `lv_comments` 位于 `ScrollView` 内部，且声明为 `layout_height="wrap_content"`。

`ScrollView` 在测量子 View 时传入的 heightMeasureSpec 模式是 `UNSPECIFIED`，即不做高度约束；而 `ListView` 在 `UNSPECIFIED` 模式下只会测量并布局第一个 item，用它推算自身高度，后续 item 不参与渲染，于是列表看起来只有一条。

评论计数走的是数据库的 `COUNT(*)` 查询，与列表渲染完全无关，因此出现了「计数正确、列表缺项」这种看似矛盾的组合。

`lv_related`（相关新闻列表）存在完全相同的问题。

**修复方案（已实施）**

采用方案一：在 `NewsDetailActivity` 中新增 `fitListViewHeight(ListView)` 方法，每次 `setAdapter()` 之后调用。核心逻辑：

```java
// 宽度必须给准，否则文本换行行数不对，算出的高度也会偏
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
totalHeight += listView.getDividerHeight() * (itemCount - 1);  // 分隔线也要算进去

ViewGroup.LayoutParams params = listView.getLayoutParams();
params.height = totalHeight;
listView.setLayoutParams(params);
```

同样的处理也应用到了 `lvRelated`。

**修复结果**

- `lv_comments` 完整渲染全部评论，条目数与「评论 (N)」的计数一致
- `lv_related` 同步修复，相关新闻不再只显示一条
- 评论区无数据时高度归零，不再留下空白占位
- 改动只涉及 `NewsDetailActivity.java`，未修改布局结构；`./gradlew compileDebugJavaWithJavac` 编译通过

**其他可选方案**

2. 改用 `RecyclerView`，并设置 `nestedScrollingEnabled=false`
3. 外层容器换成 `LinearLayout`，动态添加子 View

## 说明

本项目为课程设计作业，用于练习 Android 原生开发、SQLite 本地存储与界面布局调试。
