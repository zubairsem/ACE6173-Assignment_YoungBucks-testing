package com.example.thynkr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NotesAdapter extends ArrayAdapter<Note> {

    public interface NoteActions {
        void onOpen(int position, Note note);
        void onDelete(int position, Note note);
    }

    private final NoteActions actions;

    private float titleSizeSp = 16f;
    private float subSizeSp = 12f;

    // formatter for timestamp; force Malaysia time
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public NotesAdapter(@NonNull Context context,
                        @NonNull List<Note> items,
                        @NonNull NoteActions actions) {
        super(context, 0, items);
        this.actions = actions;
        timeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
    }

    public void setFontSizes(float titleSizeSp, float subSizeSp) {
        this.titleSizeSp = titleSizeSp;
        this.subSizeSp = subSizeSp;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position,
                        @Nullable View convertView,
                        @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.note_list_item, parent, false);
        }

        Note note = getItem(position);

        TextView textTitle = convertView.findViewById(R.id.text_title);
        TextView textSub   = convertView.findViewById(R.id.text_sub);
        TextView textLast  = convertView.findViewById(R.id.text_last_edited);
        Button btnOpen     = convertView.findViewById(R.id.btn_open);
        Button btnDelete   = convertView.findViewById(R.id.btn_delete);

        if (note != null) {
            textTitle.setText(note.getTitle());

            String full = note.getContent() == null ? "" : note.getContent();
            int newline = full.indexOf("\n");
            String preview = newline >= 0 ? full.substring(0, newline) : full;

            int MAX = 20;
            if (preview.length() > MAX) {
                preview = preview.substring(0, MAX) + "...";
            }
            textSub.setText(preview);

            Timestamp ts = note.getLastEdited();
            if (ts != null) {
                String formatted = timeFormat.format(ts.toDate());
                textLast.setText("Last edited: " + formatted);
            } else {
                textLast.setText("");
            }
        }

        textTitle.setTextSize(titleSizeSp);
        textSub.setTextSize(subSizeSp);

        btnOpen.setOnClickListener(v -> actions.onOpen(position, note));
        btnDelete.setOnClickListener(v -> actions.onDelete(position, note));

        return convertView;
    }
}
