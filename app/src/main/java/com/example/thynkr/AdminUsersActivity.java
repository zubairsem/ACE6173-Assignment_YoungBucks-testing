package com.example.thynkr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private List<AppUser> users;
    private AdminUsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        ListView userList = findViewById(R.id.user_list);
        Button btnLogout = findViewById(R.id.btn_admin_logout);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        users = new ArrayList<>();
        adapter = new AdminUsersAdapter(this, users, this::toggleBlockUser);
        userList.setAdapter(adapter);

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();

            Intent i = new Intent(AdminUsersActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });

        loadUsers();
    }

    private void loadUsers() {
        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    users.clear();
                    querySnapshot.getDocuments().forEach(doc -> {
                        String uid = doc.getId();
                        String email = doc.getString("email");
                        Boolean blocked = doc.getBoolean("blocked");
                        if (blocked == null) blocked = false;

                        users.add(new AppUser(uid, email, blocked));
                    });
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load users error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void toggleBlockUser(int position, AppUser user) {
        boolean newBlocked = !user.isBlocked();

        db.collection("users")
                .document(user.getUid())
                .update("blocked", newBlocked)
                .addOnSuccessListener(aVoid -> {
                    user.setBlocked(newBlocked);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this,
                            newBlocked ? "User blocked" : "User unblocked",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
