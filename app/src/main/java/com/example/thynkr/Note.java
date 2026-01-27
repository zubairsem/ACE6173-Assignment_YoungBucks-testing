package com.example.thynkr;

import com.google.firebase.Timestamp;

public class Note {

    private String id;
    private String title;
    private String content;
    private Timestamp lastEdited;   // NEW

    public Note() {
        // needed for Firestore
    }

    public Note(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Timestamp getLastEdited() { return lastEdited; }   // NEW

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setLastEdited(Timestamp lastEdited) { this.lastEdited = lastEdited; } // NEW
}
