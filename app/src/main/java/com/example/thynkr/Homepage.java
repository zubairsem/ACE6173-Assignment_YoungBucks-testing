package com.example.thynkr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class Homepage extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        mAuth = FirebaseAuth.getInstance();

        Button btnGoCategory = findViewById(R.id.btn_go_category);
        Button btnGoHomeNote = findViewById(R.id.btn_go_homenote);   // NEW
        Button btnLogout     = findViewById(R.id.btn_logout);

        btnGoCategory.setOnClickListener(v -> {
            Intent intent = new Intent(Homepage.this, CategoryMainActivity.class);
            startActivity(intent);
        });

        btnGoHomeNote.setOnClickListener(v -> {
            Intent intent = new Intent(Homepage.this, HomeNote.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();

            Intent i = new Intent(Homepage.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }
}
