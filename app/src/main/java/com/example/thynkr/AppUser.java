package com.example.thynkr;

public class AppUser {
    private String uid;
    private String email;
    private boolean blocked;

    public AppUser() {}

    public AppUser(String uid, String email, boolean blocked) {
        this.uid = uid;
        this.email = email;
        this.blocked = blocked;
    }

    public String getUid() { return uid; }
    public String getEmail() { return email; }
    public boolean isBlocked() { return blocked; }

    public void setUid(String uid) { this.uid = uid; }
    public void setEmail(String email) { this.email = email; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
}
