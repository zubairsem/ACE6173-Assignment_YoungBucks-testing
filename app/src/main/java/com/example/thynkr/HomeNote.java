package com.example.thynkr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeNote extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD_NOTE = 1;
    private static final int REQUEST_CODE_EDIT_NOTE = 2;

    private NotesAdapter adapter;
    private List<Note> items;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String PREFS_NAME = "thynkr_prefs";
    private static final String KEY_FONT_SIZE = "font_size";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_main);

        ListView listView = findViewById(R.id.listView);
        Button addNoteButton = findViewById(R.id.add_button);
        Button btnBack = findViewById(R.id.btn_back_category); // can rename id later
        TextView notesTitle = findViewById(R.id.notes_title);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(HomeNote.this, LoginActivity.class));
            finish();
            return;
        }

        items = new ArrayList<>();

        adapter = new NotesAdapter(this, items, new NotesAdapter.NoteActions() {
            @Override
            public void onOpen(int position, Note note) {
                Intent intent = new Intent(HomeNote.this, OpenNoteActivity.class);
                intent.putExtra("note_id", note.getId());
                intent.putExtra("note_title", note.getTitle());
                intent.putExtra("note_content", note.getContent());
                intent.putExtra("POSITION", position);
                startActivityForResult(intent, REQUEST_CODE_EDIT_NOTE);
            }

            @Override
            public void onDelete(int position, Note note) {
                deleteNoteFromFirestore(position, note);
            }
        });

        listView.setAdapter(adapter);

        addNoteButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeNote.this, AddNoteActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
        });

        btnBack.setOnClickListener(v -> finish());

        applyFontSize(notesTitle);

        loadNotesFromFirestore();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView notesTitle = findViewById(R.id.notes_title);
        applyFontSize(notesTitle);
    }

    private void applyFontSize(TextView notesTitle) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String size = prefs.getString(KEY_FONT_SIZE, "medium");

        float titleSize;
        float itemTitleSize;
        float itemSubSize;

        switch (size) {
            case "small":
                titleSize = 18f;
                itemTitleSize = 14f;
                itemSubSize = 12f;
                break;
            case "large":
                titleSize = 26f;
                itemTitleSize = 20f;
                itemSubSize = 18f;
                break;
            default: // medium
                titleSize = 22f;
                itemTitleSize = 16f;
                itemSubSize = 14f;
                break;
        }

        notesTitle.setText("Home Notes");
        notesTitle.setTextSize(titleSize);
        adapter.setFontSizes(itemTitleSize, itemSubSize);
    }


    private void loadNotesFromFirestore() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("homeNotes")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    items.clear();
                    querySnapshot.getDocuments().forEach(doc -> {
                        Note n = new Note(
                                doc.getId(),
                                doc.getString("title"),
                                doc.getString("content")
                        );
                        n.setLastEdited(doc.getTimestamp("lastEdited"));
                        items.add(n);
                    });
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveNewNoteToFirestore(String title, String content) {
        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("content", content);
        map.put("lastEdited", FieldValue.serverTimestamp());

        db.collection("users").document(uid).collection("homeNotes")
                .add(map)
                .addOnSuccessListener(docRef -> {
                    Note n = new Note(docRef.getId(), title, content);
                    n.setLastEdited(new Timestamp(System.currentTimeMillis() / 1000, 0));
                    items.add(n);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Save error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateNoteInFirestore(String id, String title, String content, int pos) {
        if (id == null || pos < 0) return;

        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("content", content);
        map.put("lastEdited", FieldValue.serverTimestamp());

        db.collection("users").document(uid).collection("homeNotes")
                .document(id)
                .set(map)
                .addOnSuccessListener(aVoid -> {
                    Note n = new Note(id, title, content);
                    n.setLastEdited(new Timestamp(System.currentTimeMillis() / 1000, 0));
                    items.set(pos, n);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteNoteFromFirestore(int position, Note note) {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("homeNotes")
                .document(note.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    items.remove(position);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Delete error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == REQUEST_CODE_ADD_NOTE) {
            String title = data.getStringExtra("note_title");
            String content = data.getStringExtra("note_content");
            if (title != null) {
                saveNewNoteToFirestore(title, content);
            }
        } else if (requestCode == REQUEST_CODE_EDIT_NOTE) {
            String id = data.getStringExtra("note_id");
            String title = data.getStringExtra("note_title");
            String content = data.getStringExtra("note_content");
            int pos = data.getIntExtra("POSITION", -1);
            if (id != null && title != null && pos >= 0) {
                updateNoteInFirestore(id, title, content, pos);
            }
        }
    }
}
