package com.example.thynkr;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity1 extends AppCompatActivity {

    EditText regEmail, regPassword, regConfirmPassword;
    Button regButton, backToLogin;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register1);

        regEmail = findViewById(R.id.reg_email);
        regPassword = findViewById(R.id.reg_password);
        regConfirmPassword = findViewById(R.id.reg_confirm_password);
        regButton = findViewById(R.id.reg_button);
        backToLogin = findViewById(R.id.back_to_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        regButton.setOnClickListener(v -> registerUser());
        backToLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String email = regEmail.getText().toString().trim();
        String password = regPassword.getText().toString().trim();
        String confirm = regConfirmPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        Map<String, Object> user = new HashMap<>();
                        user.put("email", email);
                        user.put("uid", uid);
                        user.put("blocked", false);

                        db.collection("users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Registered!", Toast.LENGTH_SHORT).show();
                                    finish(); // back to LoginActivity
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    } else {
                        Toast.makeText(this, "Auth error: " +
                                        (task.getException() != null ? task.getException().getMessage() : "unknown"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
