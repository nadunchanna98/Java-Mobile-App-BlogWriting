package com.example.myblogs;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText textName, textBody, searchEditText;
    private Button addBtn, uploadImageBtn, selectAllBtn, deleteSelectedBtn, actionBtn;
    private TextView blogsTextView;
    private LinearLayout blogsContainer;
    private ScrollView blogsScrollView;
    private ImageView imageView;
    private SQLiteDatabase db;

    private static final int BLOG_DETAIL_REQUEST_CODE = 1;
    private static final int ACTION_SEARCH = 1;
    private static final int ACTION_CLEAR = 2;
    private int currentAction = ACTION_SEARCH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textName = findViewById(R.id.textName);
        textBody = findViewById(R.id.textBody);
        addBtn = findViewById(R.id.addBtn);
        uploadImageBtn = findViewById(R.id.uploadImageBtn);
        selectAllBtn = findViewById(R.id.selectAllBtn);
        deleteSelectedBtn = findViewById(R.id.deleteSelectedBtn);
        blogsTextView = findViewById(R.id.blogsTextView);
        blogsContainer = findViewById(R.id.blogsContainer);
        blogsScrollView = findViewById(R.id.blogsScrollView);
        imageView = findViewById(R.id.imageView);
        searchEditText = findViewById(R.id.searchEditText);
        actionBtn = findViewById(R.id.searchBtn);

        db = openOrCreateDatabase("blogsdb", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS blogtable (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, body TEXT)");

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addBlog();
            }
        });

        uploadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Implement image upload logic
            }
        });

        selectAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectAllBlogs();
            }
        });

        deleteSelectedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSelectedBlogs();
            }
        });

        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentAction == ACTION_SEARCH) {
                    performSearch();
                } else if (currentAction == ACTION_CLEAR) {
                    clearSearch();
                }
            }
        });

        displayBlogs();
    }

    private void addBlog() {
        String name = textName.getText().toString().trim();
        String body = textBody.getText().toString().trim();

        if (name.isEmpty() || body.isEmpty()) {
            Toast.makeText(this, "Please enter both name and body", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("body", body);

        long result = db.insert("blogtable", null, values);

        if (result != -1) {
            Toast.makeText(this, "Blog added successfully", Toast.LENGTH_SHORT).show();
            textName.getText().clear();
            textBody.getText().clear();
            displayBlogs();
        } else {
            Toast.makeText(this, "Error adding blog", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayBlogs() {
        blogsContainer.removeAllViews();

        Cursor cursor = db.rawQuery("SELECT * FROM blogtable", null);

        int idColumnIndex = cursor.getColumnIndex("id");
        int nameColumnIndex = cursor.getColumnIndex("name");
        int bodyColumnIndex = cursor.getColumnIndex("body");

        while (cursor.moveToNext()) {
            if (idColumnIndex != -1 && nameColumnIndex != -1 && bodyColumnIndex != -1) {
                final int id = cursor.getInt(idColumnIndex);
                String name = cursor.getString(nameColumnIndex);
                String body = cursor.getString(bodyColumnIndex);

                RelativeLayout blogEntryLayout = new RelativeLayout(this);
                blogEntryLayout.setPadding(0, 0, 0, 20);

                CheckBox checkBox = new CheckBox(this);
                checkBox.setId(id);

                TextView blogTextView = new TextView(this);
                blogTextView.setText("\nBlog Name: " + "\n\n" + name + "\n\n");
                blogTextView.setTextSize(16);
                blogTextView.setTypeface(null, Typeface.BOLD);

                Button selectBtn = new Button(this);
                selectBtn.setText("Select");
                selectBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectIndividualBlog(id);
                    }
                });

                Button showBtn = new Button(this);
                showBtn.setText("Show");
                showBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showBlogDetails(id, name, body);
                    }
                });

                RelativeLayout.LayoutParams paramsCheckBox = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                paramsCheckBox.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                paramsCheckBox.setMargins(0, 0, 2, 0);

                RelativeLayout.LayoutParams paramsBlogText = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                paramsBlogText.addRule(RelativeLayout.CENTER_HORIZONTAL);

                RelativeLayout.LayoutParams paramsSelectBtn = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                paramsSelectBtn.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                paramsSelectBtn.setMargins(0, 0, 2, 0);

                RelativeLayout.LayoutParams paramsShowBtn = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                paramsShowBtn.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                paramsShowBtn.addRule(RelativeLayout.BELOW, blogTextView.getId());
                paramsShowBtn.setMargins(0, 8, 2, 0);

                blogEntryLayout.addView(checkBox, paramsCheckBox);
                blogEntryLayout.addView(blogTextView, paramsBlogText);
                blogEntryLayout.addView(selectBtn, paramsSelectBtn);
                blogEntryLayout.addView(showBtn, paramsShowBtn);

                blogsContainer.addView(blogEntryLayout);
            } else {
                Log.e("MainActivity", "Invalid column indices");
            }
        }

        cursor.close();
    }

    private void selectAllBlogs() {
        for (int i = 0; i < blogsContainer.getChildCount(); i++) {
            View child = blogsContainer.getChildAt(i);
            if (child instanceof RelativeLayout) {
                RelativeLayout blogEntryLayout = (RelativeLayout) child;
                for (int j = 0; j < blogEntryLayout.getChildCount(); j++) {
                    View innerChild = blogEntryLayout.getChildAt(j);
                    if (innerChild instanceof CheckBox) {
                        CheckBox checkBox = (CheckBox) innerChild;
                        checkBox.setChecked(true);
                    }
                }
            }
        }
    }

    private void selectIndividualBlog(int blogId) {
        CheckBox checkBox = findViewById(blogId);
        if (checkBox != null) {
            checkBox.setChecked(true);
        }
    }

    private void deleteSelectedBlogs() {
        for (int i = 0; i < blogsContainer.getChildCount(); i++) {
            View child = blogsContainer.getChildAt(i);
            if (child instanceof RelativeLayout) {
                RelativeLayout blogEntryLayout = (RelativeLayout) child;
                for (int j = 0; j < blogEntryLayout.getChildCount(); j++) {
                    View innerChild = blogEntryLayout.getChildAt(j);
                    if (innerChild instanceof CheckBox) {
                        CheckBox checkBox = (CheckBox) innerChild;
                        if (checkBox.isChecked()) {
                            int blogId = checkBox.getId();
                            deleteBlog(blogId);
                        }
                    }
                }
            }
        }

        displayBlogs();
    }

    private void deleteBlog(int blogId) {
        int result = db.delete("blogtable", "id=?", new String[]{String.valueOf(blogId)});

        if (result > 0) {
            Toast.makeText(this, "Blog deleted successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error deleting blog", Toast.LENGTH_SHORT).show();
        }
    }

    private void showBlogDetails(int blogId, String blogName, String blogBody) {
        Intent intent = new Intent(this, BlogDetailActivity.class);
        intent.putExtra("blogId", blogId);
        intent.putExtra("blogName", blogName);
        intent.putExtra("blogBody", blogBody);
        startActivityForResult(intent, BLOG_DETAIL_REQUEST_CODE);
    }

    private void performSearch() {
        String searchQuery = searchEditText.getText().toString().trim();
        blogsContainer.removeAllViews();

        Cursor cursor = db.rawQuery("SELECT * FROM blogtable WHERE name LIKE ?", new String[]{"%" + searchQuery + "%"});

        int idColumnIndex = cursor.getColumnIndex("id");
        int nameColumnIndex = cursor.getColumnIndex("name");
        int bodyColumnIndex = cursor.getColumnIndex("body");

        while (cursor.moveToNext()) {
            if (idColumnIndex != -1 && nameColumnIndex != -1 && bodyColumnIndex != -1) {
                final int id = cursor.getInt(idColumnIndex);
                String name = cursor.getString(nameColumnIndex);
                String body = cursor.getString(bodyColumnIndex);

                displayBlogDetails(name, body);
            } else {
                Log.e("MainActivity", "Invalid column indices");
            }
        }

        cursor.close();

        actionBtn.setText("Clear");
        currentAction = ACTION_CLEAR;
    }

    private void clearSearch() {
        displayBlogs();
        actionBtn.setText("Search");
        currentAction = ACTION_SEARCH;
    }

    // Modify the displayBlogDetails method to include the body
    private void displayBlogDetails(String name, String body) {
        RelativeLayout blogEntryLayout = new RelativeLayout(this);
        blogEntryLayout.setPadding(0, 0, 0, 20);

        TextView blogBodyTextView = new TextView(this);
        blogBodyTextView.setText("Blog Body: " + "\n\n" + body + "\n\n");
        blogBodyTextView.setTextSize(14);

        blogEntryLayout.addView(blogBodyTextView);

        blogsContainer.addView(blogEntryLayout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BLOG_DETAIL_REQUEST_CODE && resultCode == RESULT_OK) {
            displayBlogs();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}
