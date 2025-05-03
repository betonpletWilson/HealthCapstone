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

public class SectionedNotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SECTION_HEADER = 0;
    private static final int VIEW_TYPE_NOTIFICATION = 1;

    private Context context;
    private List<NotifData.NotificationItem> notificationItems;

    public SectionedNotificationAdapter(Context context, List<NotifData.NotificationItem> notificationItems) {
        this.context = context;
        this.notificationItems = notificationItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SECTION_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.section_header_layout, parent, false);
            return new SectionHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.notif_item_layout, parent, false);
            return new NotificationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotifData.NotificationItem item = notificationItems.get(position);

        if (holder instanceof SectionHeaderViewHolder) {
            SectionHeaderViewHolder headerHolder = (SectionHeaderViewHolder) holder;
            headerHolder.headerTextView.setText(item.getTitle());
            // Only use the title for header, ignore all other fields
        } else if (holder instanceof NotificationViewHolder) {
            NotificationViewHolder notifHolder = (NotificationViewHolder) holder;

            notifHolder.titleTextView.setText(item.getTitle());

            // Set time if available, otherwise hide it
            if (item.getTime() != null && !item.getTime().isEmpty()) {
                notifHolder.timeTextView.setVisibility(View.VISIBLE);
                notifHolder.timeTextView.setText(item.getTime());
            } else {
                notifHolder.timeTextView.setVisibility(View.GONE);
            }

            // Only set duration for regular notification items
            if (item.getDuration() != null) {
                notifHolder.medicationTextView.setVisibility(View.VISIBLE);
                notifHolder.medicationTextView.setText(item.getDuration());
            } else {
                notifHolder.medicationTextView.setVisibility(View.GONE);
            }

            // Set background color
            try {
                notifHolder.containerLayout.setBackgroundColor(Color.parseColor(item.getBackgroundColor()));
            } catch (Exception e) {
                // If color parsing fails, use default color
                notifHolder.containerLayout.setBackgroundColor(Color.parseColor("#FFF2D9"));
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        NotifData.NotificationItem item = notificationItems.get(position);
        if ("header".equals(item.getCategory())) {
            return VIEW_TYPE_SECTION_HEADER;
        } else {
            return VIEW_TYPE_NOTIFICATION;
        }
    }

    @Override
    public int getItemCount() {
        return notificationItems.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, timeTextView, medicationTextView;
        ConstraintLayout containerLayout;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_notification_title);
            timeTextView = itemView.findViewById(R.id.tv_notification_time);
            medicationTextView = itemView.findViewById(R.id.tv_notification_medication);
            containerLayout = itemView.findViewById(R.id.container);
        }
    }

    public static class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTextView;

        public SectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTextView = itemView.findViewById(R.id.tv_section_header);
        }
    }
}