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

import java.util.List;

public class CategoryAdapter extends ArrayAdapter<Category> {

    public interface CategoryActions {
        void onOpen(int position, Category category);
        void onDelete(int position, Category category);
        void onRename(int position, Category category);   // NEW
    }

    private final CategoryActions actions;
    private float fontSizeSp = 16f;

    public CategoryAdapter(@NonNull Context context,
                           @NonNull List<Category> items,
                           @NonNull CategoryActions actions) {
        super(context, 0, items);
        this.actions = actions;
    }

    public void setFontSize(float sizeSp) {
        this.fontSizeSp = sizeSp;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position,
                        @Nullable View convertView,
                        @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.category_list_item, parent, false);
        }

        Category category = getItem(position);

        TextView textName = convertView.findViewById(R.id.text_category_name);
        Button btnOpen = convertView.findViewById(R.id.btn_open_category);
        Button btnDelete = convertView.findViewById(R.id.btn_delete_category);

        if (category != null) {
            textName.setText(category.getName());
        }

        textName.setTextSize(fontSizeSp);

        btnOpen.setOnClickListener(v -> actions.onOpen(position, category));
        btnDelete.setOnClickListener(v -> actions.onDelete(position, category));

        // Tap the name to rename
        textName.setOnClickListener(v -> actions.onRename(position, category));  // [web:179][web:190]

        return convertView;
    }
}
