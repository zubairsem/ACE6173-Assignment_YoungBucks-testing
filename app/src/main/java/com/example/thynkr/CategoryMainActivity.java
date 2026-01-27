package com.example.thynkr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryMainActivity extends AppCompatActivity {

    private List<Category> categories;
    private CategoryAdapter categoryAdapter;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String PREFS_NAME = "thynkr_prefs";
    private static final String KEY_FONT_SIZE = "font_size";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_main);

        ListView listView = findViewById(R.id.category_list);
        Button btnAddCategory = findViewById(R.id.btn_add_category);
        Button btnSettings = findViewById(R.id.btn_settings);
        Button btnBackHome = findViewById(R.id.btn_back_home);   // NEW
        TextView categoryTitle = findViewById(R.id.category_title);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(CategoryMainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        categories = new ArrayList<>();

        categoryAdapter = new CategoryAdapter(
                this,
                categories,
                new CategoryAdapter.CategoryActions() {
                    @Override
                    public void onOpen(int position, Category category) {
                        Intent intent = new Intent(CategoryMainActivity.this, NotesMainActivity.class);
                        intent.putExtra("CATEGORY_ID", category.getId());
                        intent.putExtra("CATEGORY_NAME", category.getName());
                        startActivity(intent);
                    }

                    @Override
                    public void onDelete(int position, Category category) {
                        showDeleteDoubleConfirm(position, category);
                    }

                    @Override
                    public void onRename(int position, Category category) {
                        showRenameCategoryDialog(position, category);
                    }
                });

        listView.setAdapter(categoryAdapter);

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());



        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryMainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // NEW: go back to Homepage
        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryMainActivity.this, Homepage.class);
            startActivity(intent);
            finish();   // close this screen so Back from Homepage doesn’t return here
        });  // [web:206][web:211]

        applyFontSize(categoryTitle);
        loadCategories();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView categoryTitle = findViewById(R.id.category_title);
        applyFontSize(categoryTitle);
    }

    private void applyFontSize(TextView categoryTitle) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String size = prefs.getString(KEY_FONT_SIZE, "medium");

        float titleSize;
        float listItemSize;

        switch (size) {
            case "small":
                titleSize = 18f;
                listItemSize = 14f;
                break;
            case "large":
                titleSize = 26f;
                listItemSize = 20f;
                break;
            default:
                titleSize = 22f;
                listItemSize = 16f;
                break;
        }

        categoryTitle.setTextSize(titleSize);
        categoryAdapter.setFontSize(listItemSize);
    }

    private void loadCategories() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("categories")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    categories.clear();
                    querySnapshot.getDocuments().forEach(doc -> {
                        Category c = new Category(
                                doc.getId(),
                                doc.getString("name")
                        );
                        categories.add(c);
                    });
                    categoryAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load categories error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showAddCategoryDialog() {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Category name");

        new AlertDialog.Builder(this)
                .setTitle("Add Category")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        saveCategory(name);
                    } else {
                        Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveCategory(String name) {
        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> map = new HashMap<>();
        map.put("name", name);

        db.collection("users").document(uid).collection("categories")
                .add(map)
                .addOnSuccessListener(docRef -> {
                    Category c = new Category(docRef.getId(), name);
                    categories.add(c);
                    categoryAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Save category error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDeleteDoubleConfirm(int position, Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete category")
                .setMessage("Are you sure you want to delete \"" + category.getName() + "\"?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    new AlertDialog.Builder(CategoryMainActivity.this)
                            .setTitle("Confirm delete")
                            .setMessage("This cannot be undone. Delete \"" + category.getName() + "\" permanently?")
                            .setPositiveButton("Delete", (dialog2, which2) -> {
                                deleteCategory(position, category);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();

                })
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteCategory(int position, Category category) {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .collection("categories")
                .document(category.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    categories.remove(position);
                    categoryAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Delete category error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showRenameCategoryDialog(int position, Category category) {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setText(category.getName());
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(this)
                .setTitle("Rename Category")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateCategoryName(position, category, newName);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateCategoryName(int position, Category category, String newName) {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .collection("categories")
                .document(category.getId())
                .update("name", newName)
                .addOnSuccessListener(aVoid -> {
                    category.setName(newName);
                    categories.set(position, category);
                    categoryAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Category renamed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Rename error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
