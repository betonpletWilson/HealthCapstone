package com.fitwizard.fitwizard;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<NotifData.NotificationItem> notificationItems;

    public NotificationAdapter(Context context, List<NotifData.NotificationItem> notificationItems) {
        this.context = context;
        this.notificationItems = notificationItems;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notif_item_layout, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotifData.NotificationItem item = notificationItems.get(position);

        holder.titleTextView.setText(item.getTitle());

        // Set time if available, otherwise hide it
        if (item.getTime() != null && !item.getTime().equals("N/A")) {
            holder.timeTextView.setVisibility(View.VISIBLE);
            holder.timeTextView.setText(item.getTime());
        } else {
            holder.timeTextView.setVisibility(View.GONE);
        }

        holder.durationTextView.setText(item.getDuration());

        // Set background color
        try {
            holder.containerLayout.setBackgroundColor(Color.parseColor(item.getBackgroundColor()));
        } catch (Exception e) {
            // If color parsing fails, use default color
            holder.containerLayout.setBackgroundColor(Color.parseColor("#FFF2D9"));
        }
    }

    @Override
    public int getItemCount() {
        return notificationItems.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, timeTextView, durationTextView;
        ConstraintLayout containerLayout;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_notification_title);
            timeTextView = itemView.findViewById(R.id.tv_notification_time);
            durationTextView = itemView.findViewById(R.id.tv_notification_duration);
            containerLayout = itemView.findViewById(R.id.container);
        }
    }
}