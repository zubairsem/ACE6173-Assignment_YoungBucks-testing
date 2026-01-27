package com.example.thynkr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText username, password;
    Button loginbtn, regisbtn, btn_forgot_password;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    private static final String ADMIN_EMAIL = "admin@thynkr.com";
    private static final String ADMIN_PASSWORD = "Admin1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginbtn = findViewById(R.id.loginbtn);
        regisbtn = findViewById(R.id.regisbtn);
        btn_forgot_password = findViewById(R.id.btn_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            goToHome();
            finish();
            return;
        }

        loginbtn.setOnClickListener(v -> {
            String email = username.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (email.equals(ADMIN_EMAIL) && pass.equals(ADMIN_PASSWORD)) {
                Toast.makeText(this, "Admin login successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, AdminUsersActivity.class));
                finish();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user == null) {
                                Toast.makeText(this, "Login error: user missing", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String uid = user.getUid();

                            db.collection("users")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener(doc -> {
                                        if (!doc.exists()) {
                                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                                            goToHome();
                                            finish();
                                            return;
                                        }

                                        Boolean blocked = doc.getBoolean("blocked");
                                        if (blocked != null && blocked) {
                                            mAuth.signOut();
                                            Toast.makeText(this, "Your account has been blocked.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                                            goToHome();
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        mAuth.signOut();
                                        Toast.makeText(this, "Error checking account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });

                        } else {
                            String msg = (task.getException() != null)
                                    ? task.getException().getMessage()
                                    : "Login failed";
                            Toast.makeText(this, "Error: " + msg, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        regisbtn.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity1.class)));

        btn_forgot_password.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void goToHome() {
        Intent intent = new Intent(LoginActivity.this, Homepage.class);
        startActivity(intent);
    }
}
