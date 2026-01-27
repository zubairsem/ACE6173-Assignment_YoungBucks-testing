package com.example.thynkr;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "thynkr_prefs";
    public static final String KEY_FONT_SIZE = "font_size";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        RadioGroup rgFontSize = findViewById(R.id.rg_font_size);
        RadioButton rbSmall = findViewById(R.id.rb_small);
        RadioButton rbMedium = findViewById(R.id.rb_medium);
        RadioButton rbLarge = findViewById(R.id.rb_large);
        Button btnBack = findViewById(R.id.btn_back_settings);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String saved = prefs.getString(KEY_FONT_SIZE, "medium");

        if ("small".equals(saved)) {
            rbSmall.setChecked(true);
        } else if ("large".equals(saved)) {
            rbLarge.setChecked(true);
        } else {
            rbMedium.setChecked(true);
        }

        rgFontSize.setOnCheckedChangeListener((group, checkedId) -> {
            String value = "medium";
            if (checkedId == R.id.rb_small) {
                value = "small";
            } else if (checkedId == R.id.rb_large) {
                value = "large";
            }

            prefs.edit().putString(KEY_FONT_SIZE, value).apply();
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
