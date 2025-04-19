package com.fitwizard.fitwizard;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


//TODO: fix layout of bottom nav


public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView notificationsRecyclerView;
    private NotificationAdapter notificationAdapter;
    private List<NotifData.NotificationItem> notificationItems;

    // Filter buttons
    private Button btnFilterDaily, btnFilterUpcoming, btnFilterOverall, btnFilterAllMonth;
    private Chip chipMedication;

    // Bottom navigation
    private TextView navHome, navReports;
    private FloatingActionButton addFab;
    private LinearLayout addMenu;
    private Button createNotifButton;

    // Current filter state
    private String currentTimeFilter = "upcoming";
    private List<String> currentCategoryFilters = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        setupDate();
        setupBackButton();
        setupFilterButtons();
        setupNotificationsList();
        setupBottomNavigation();
        setupPopupNavMenu();
    }

    private void initViews() {
        // Back button
        ImageButton btnBack = findViewById(R.id.btn_back);

        // Date display
        TextView tvTime = findViewById(R.id.tv_time);
        TextView tvDate = findViewById(R.id.tv_date);

        // Filter buttons
        btnFilterDaily = findViewById(R.id.btn_filter_daily);
        btnFilterUpcoming = findViewById(R.id.btn_filter_upcoming);
        btnFilterOverall = findViewById(R.id.btn_filter_overall);
        btnFilterAllMonth = findViewById(R.id.btn_filter_all_month);

        // Category chips
        chipMedication = findViewById(R.id.chip_medication);

        // Notifications list
        notificationsRecyclerView = findViewById(R.id.rv_notifications);

        // Bottom Navigation
        navHome = findViewById(R.id.nav_home);
        navReports = findViewById(R.id.nav_reports);
        addFab = findViewById(R.id.fab_add);
        addMenu = findViewById(R.id.add_menu);
        createNotifButton = findViewById(R.id.btn_add_notif);
    }

    private void setupDate() {
        // Set current date and time in the date display
        TextView tvTime = findViewById(R.id.tv_time);
        TextView tvDate = findViewById(R.id.tv_date);

        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());

        tvTime.setText(timeFormat.format(currentDate));
        tvDate.setText(dateFormat.format(currentDate));
    }

    //go back to home screen
    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void setupFilterButtons() {
        // Time filter buttons
        btnFilterDaily.setOnClickListener(v -> {
            updateTimeFilter("daily");
            updateFilterButtonAppearance();
            filterNotifications();
        });

        btnFilterUpcoming.setOnClickListener(v -> {
            updateTimeFilter("upcoming");
            updateFilterButtonAppearance();
            filterNotifications();
        });

        btnFilterOverall.setOnClickListener(v -> {
            updateTimeFilter("overall");
            updateFilterButtonAppearance();
            filterNotifications();
        });

        btnFilterAllMonth.setOnClickListener(v -> {
            updateTimeFilter("all_month");
            updateFilterButtonAppearance();
            filterNotifications();
        });

        // Category filter chips
        chipMedication.setOnClickListener(v -> {
            if (currentCategoryFilters.contains("medication")) {
                currentCategoryFilters.remove("medication");
                chipMedication.setChecked(false);
            } else {
                currentCategoryFilters.add("medication");
                chipMedication.setChecked(true);
            }
            filterNotifications();
        });
    }

    private void updateTimeFilter(String filter) {
        currentTimeFilter = filter;
    }

    private void updateFilterButtonAppearance() {
        // Reset all buttons to outlined style
        resetButtonStyle(btnFilterDaily);
        resetButtonStyle(btnFilterUpcoming);
        resetButtonStyle(btnFilterOverall);
        resetButtonStyle(btnFilterAllMonth);

        // Set active button to filled style
        switch (currentTimeFilter) {
            case "daily":
                setActiveButtonStyle(btnFilterDaily);
                break;
            case "upcoming":
                setActiveButtonStyle(btnFilterUpcoming);
                break;
            case "overall":
                setActiveButtonStyle(btnFilterOverall);
                break;
            case "all_month":
                setActiveButtonStyle(btnFilterAllMonth);
                break;
        }
    }

    private void resetButtonStyle(Button button) {
        button.setBackgroundTintList(getColorStateList(android.R.color.white));
        button.setTextColor(getResources().getColor(R.color.black, null));
    }

    private void setActiveButtonStyle(Button button) {
        button.setBackgroundTintList(getColorStateList(R.color.green));
        button.setTextColor(getResources().getColor(R.color.white, null));
    }

    //placeholder notification
    private void setupNotificationsList() {
        // Initialize notification items list
        notificationItems = new ArrayList<>();

        // Add sample notifications
        notificationItems.add(new NotifData.NotificationItem("Exercise", "1:40 PM", "0/1 hr", "#FFFDD0", "exercise"));
        notificationItems.add(new NotifData.NotificationItem("Exercise", "2:00 PM", "0/1 hr", "#FFE4B5", "exercise"));
        notificationItems.add(new NotifData.NotificationItem("Exercise", "4:00 PM", "0/1 hr", "#FFCBC4", "exercise"));
        notificationItems.add(new NotifData.NotificationItem("Exercise", "4:30 PM", "0/1 hr", "#FFD0CF", "exercise"));
        notificationItems.add(new NotifData.NotificationItem("Exercise", "8:00 PM", "0/1 hr", "#E6E6FA", "exercise"));

        // Set up adapter
        notificationAdapter = new NotificationAdapter(this, notificationItems);
        notificationsRecyclerView.setAdapter(notificationAdapter);

        // Apply initial filters
        filterNotifications();
    }

    private void filterNotifications() {
        // Filter logic

        notificationAdapter.notifyDataSetChanged();
    }

    private void setupBottomNavigation() {
        navHome.setOnClickListener(v -> {
            // Navigate to HomeActivity
            Intent intent = new Intent(NotificationsActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        navReports.setOnClickListener(v -> {
            // Navigate to ReportsActivity

        });
    }

    private void setupPopupNavMenu() {
        // Move the popup menu outside the bottom nav card
        addFab.setOnClickListener(v -> togglePopupMenu());
        createNotifButton.setOnClickListener(v -> openActivity(NotifCreationActivity.class));
    }

    private void togglePopupMenu() {
        if (addMenu.getVisibility() == View.GONE) {
            // Show popup menu with animation
            addMenu.setVisibility(View.VISIBLE);
            addMenu.setAlpha(0f);
            addMenu.setTranslationY(100f); // Start slightly below
            addMenu.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(200)
                    .start();
        } else {
            // Hide popup menu with animation
            addMenu.animate()
                    .alpha(0f)
                    .translationY(100f)
                    .setDuration(200)
                    .withEndAction(() -> addMenu.setVisibility(View.GONE))
                    .start();
        }
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(NotificationsActivity.this, activityClass);
        startActivity(intent);
        if (activityClass != NotificationsActivity.class) {
            finish();
        }
    }
}