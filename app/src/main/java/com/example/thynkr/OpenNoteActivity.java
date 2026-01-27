package com.example.thynkr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class OpenNoteActivity extends AppCompatActivity {

    EditText edtTitle, edtContent;
    Button btnSave, btnCancel;

    private static final String PREFS_NAME = "thynkr_prefs";
    private static final String KEY_FONT_SIZE = "font_size";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_note);

        edtTitle = findViewById(R.id.edt_title);
        edtContent = findViewById(R.id.edt_content);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        // apply font size based on settings
        applyFontSize();

        String id = getIntent().getStringExtra("note_id");
        String title = getIntent().getStringExtra("note_title");
        String content = getIntent().getStringExtra("note_content");
        int position = getIntent().getIntExtra("POSITION", -1);

        edtTitle.setText(title);
        edtContent.setText(content);

        btnSave.setOnClickListener(v -> {
            String newTitle = edtTitle.getText().toString().trim();
            String newContent = edtContent.getText().toString().trim();

            Intent data = new Intent();
            data.putExtra("note_id", id);
            data.putExtra("note_title", newTitle);
            data.putExtra("note_content", newContent);
            data.putExtra("POSITION", position);
            setResult(RESULT_OK, data);
            finish();
        });

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
}
