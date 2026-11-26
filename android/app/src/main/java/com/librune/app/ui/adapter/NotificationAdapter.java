package com.librune.app.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.librune.app.R;
import com.librune.app.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotifViewHolder> {

    public interface OnClickListener {
        void onNotificationClick(Notification notification);
    }

    private final OnClickListener listener;
    private List<Notification> items = new ArrayList<>();

    public NotificationAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<Notification> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        Notification n = items.get(position);
        holder.tvTitle.setText(n.getTitle());
        holder.tvMessage.setText(n.getMessage());
        holder.tvDate.setText(n.getCreatedAt() != null && n.getCreatedAt().length() >= 16
                ? n.getCreatedAt().substring(0, 16).replace("T", " ")
                : "");

        boolean isRead = Boolean.TRUE.equals(n.getRead());
        holder.container.setBackgroundColor(isRead ? Color.WHITE : Color.parseColor("#EFF6FF"));
        holder.itemView.setOnClickListener(v -> listener.onNotificationClick(n));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NotifViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout container;
        final TextView tvTitle;
        final TextView tvMessage;
        final TextView tvDate;

        NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.notificationContainer);
            tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvMessage = itemView.findViewById(R.id.tvNotifMessage);
            tvDate = itemView.findViewById(R.id.tvNotifDate);
        }
    }
}
