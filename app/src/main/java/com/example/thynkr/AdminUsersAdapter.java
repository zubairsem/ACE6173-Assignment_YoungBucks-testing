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

public class AdminUsersAdapter extends ArrayAdapter<AppUser> {

    public interface UserActions {
        void onToggleBlock(int position, AppUser user);
    }

    private final UserActions actions;

    public AdminUsersAdapter(@NonNull Context context,
                             @NonNull List<AppUser> users,
                             @NonNull UserActions actions) {
        super(context, 0, users);
        this.actions = actions;
    }

    @NonNull
    @Override
    public View getView(int position,
                        @Nullable View convertView,
                        @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.user_list_item, parent, false);
        }

        AppUser user = getItem(position);

        TextView textEmail = convertView.findViewById(R.id.text_user_email);
        TextView textStatus = convertView.findViewById(R.id.text_user_status);
        Button btnToggle = convertView.findViewById(R.id.btn_toggle_block);

        if (user != null) {
            textEmail.setText(user.getEmail());
            if (user.isBlocked()) {
                textStatus.setText("Blocked");
                btnToggle.setText("Unblock");
            } else {
                textStatus.setText("Active");
                btnToggle.setText("Block");
            }
        }

        btnToggle.setOnClickListener(v -> actions.onToggleBlock(position, user));

        return convertView;
    }
}
