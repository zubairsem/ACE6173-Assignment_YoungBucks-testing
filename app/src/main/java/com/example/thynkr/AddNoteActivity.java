package com.example.thynkr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddNoteActivity extends AppCompatActivity {

    EditText edtTitle, edtContent;
    Button btnSave, btnCancel;

    private static final String PREFS_NAME = "thynkr_prefs";
    private static final String KEY_FONT_SIZE = "font_size";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        edtTitle = findViewById(R.id.edt_title);
        edtContent = findViewById(R.id.edt_content);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);


        applyFontSize();

        btnSave.setOnClickListener(v -> saveNote());
        btnCancel.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyFontSize();
    }

    private void applyFontSize() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String size = prefs.getString(KEY_FONT_SIZE, "medium");

        float titleSize;
        float contentSize;

        switch (size) {
            case "small":
                titleSize = 16f;
                contentSize = 14f;
                break;
            case "large":
                titleSize = 24f;
                contentSize = 20f;
                break;
            default: // medium
                titleSize = 20f;
                contentSize = 16f;
                break;
        }

        edtTitle.setTextSize(titleSize);
        edtContent.setTextSize(contentSize);
    }

    private void saveNote() {
        String title = edtTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Title required", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent data = new Intent();
        data.putExtra("note_title", title);
        data.putExtra("note_content", content);
        setResult(RESULT_OK, data);
        finish();
    }
}
