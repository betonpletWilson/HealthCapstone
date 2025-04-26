package com.fitwizard.fitwizard;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


//TODO: fix layout of bottom nav
//TODO: add padding to the TODAY sections between Morning /  Afternoon / Night time


public class NotificationsActivity extends AppCompatActivity {
    // Add this constant at the class level
    private static final int CREATE_NOTIFICATION_REQUEST = 1001;

    // Constants for time categories
    private static final String CATEGORY_MORNING = "Morning";
    private static final String CATEGORY_AFTERNOON = "Afternoon";
    private static final String CATEGORY_NIGHT = "Night Time";

    private RecyclerView notificationsRecyclerView;
    private SectionedNotificationAdapter notificationAdapter;
//    private SectionedNotificationAdapter sectNotificationAdapter;
    private List<NotifData.NotificationItem> notificationItems;
    private List<NotifData.NotificationItem> filteredItems;

    // Filter buttons
    private Button btnFiltertoday, btnFilterUpcoming, btnFilterAllMonth;
    private Chip chipMedication;

    // Bottom navigation
    private FloatingActionButton addFab;
    private LinearLayout addMenu;
    private Button createNotifButton;
    private Button homeButton;

    // Current filter state
    private String currentTimeFilter = "today"; // Changed default to "today"
    private List<String> currentCategoryFilters = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        setupBackButton();
        setupFilterButtons();
        setupNotificationsList();
        setupPopupNavMenu();
    }

    private void initViews() {
        // Back button
        ImageButton btnBack = findViewById(R.id.btn_back);


        // Filter buttons
        btnFiltertoday = findViewById(R.id.btn_filter_today);
        btnFilterUpcoming = findViewById(R.id.btn_filter_upcoming);
        btnFilterAllMonth = findViewById(R.id.btn_filter_all_month);

        // Category chips
        chipMedication = findViewById(R.id.chip_medication);

        // Notifications list
        notificationsRecyclerView = findViewById(R.id.rv_notifications);

        // Bottom Navigation
        addFab = findViewById(R.id.fab_add);
        addMenu = findViewById(R.id.add_menu);
        createNotifButton = findViewById(R.id.btn_add_notif);
        homeButton = findViewById(R.id.nav_home);
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
        btnFiltertoday.setOnClickListener(v -> {
            updateTimeFilter("today");
            updateFilterButtonAppearance();
            filterNotifications();
        });

        btnFilterUpcoming.setOnClickListener(v -> {
            updateTimeFilter("upcoming");
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
        resetButtonStyle(btnFiltertoday);
        resetButtonStyle(btnFilterUpcoming);
        resetButtonStyle(btnFilterAllMonth);

        // Set active button to filled style
        switch (currentTimeFilter) {
            case "today":
                setActiveButtonStyle(btnFiltertoday);
                break;
            case "upcoming":
                setActiveButtonStyle(btnFilterUpcoming);
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

    private void setupNotificationsList() {
        // Initialize notification items list
        notificationItems = new ArrayList<>();
        filteredItems = new ArrayList<>();

        // Add sample notifications
        notificationItems.add(new NotifData.NotificationItem("Exercise", "1:40 PM", "0/1 hr", "#FFFDD0", "exercise", "Weekly"));
        notificationItems.add(new NotifData.NotificationItem("Exercise", "2:00 PM", "0/1 hr", "#FFE4B5", "exercise", "Monthly"));
        notificationItems.add(new NotifData.NotificationItem("Exercise", "4:00 PM", "0/1 hr", "#FFCBC4", "exercise", "Monthly"));
        notificationItems.add(new NotifData.NotificationItem("Exercise", "9:30 AM", "0/1 hr", "#FFD0CF", "exercise", "Monthly"));
        notificationItems.add(new NotifData.NotificationItem("Exercise", "8:00 PM", "0/1 hr", "#E6E6FA", "exercise", "Weekly"));
        notificationItems.add(new NotifData.NotificationItem("Medication", "7:00 AM", "1 pill", "#FFE6FA", "medication", "Weekly"));
        notificationItems.add(new NotifData.NotificationItem("Medication", "11:00 PM", "1 pill", "#E6E6FA", "medication", "Weekly"));
        notificationItems.add(new NotifData.NotificationItem("Exercise", "9:30 AM", "0/1 hr", "#FFD0CF", "exercise", "Weekly"));
        notificationItems.add(new NotifData.NotificationItem("FLU meds", "9:00 PM", "0/1 hr", "#E6E6FA", "exercise", "Weekly"));
        notificationItems.add(new NotifData.NotificationItem("Medication", "7:00 PM", "1 pill", "#FFE6FA", "medication", "Weekly"));
        notificationItems.add(new NotifData.NotificationItem("Medication", "11:00 PM", "1 pill", "#E6E6FA", "medication", "Weekly"));



        // Set up adapter with empty list (will be populated in filterNotifications)
        notificationAdapter = new SectionedNotificationAdapter(this, filteredItems);
        notificationsRecyclerView.setAdapter(notificationAdapter);

        // Apply initial filters
        filterNotifications();
    }


    private void filterNotifications() {
        filteredItems.clear();

        if (currentTimeFilter.equals("today")) {
            // For "Today" filter, organize by time periods
            // First, filter by category if needed
            List<NotifData.NotificationItem> categoryFiltered = new ArrayList<>();

            if (currentCategoryFilters.isEmpty()) {
                categoryFiltered.addAll(notificationItems);
            } else {
                for (NotifData.NotificationItem item : notificationItems) {
                    if (currentCategoryFilters.contains(item.getCategory())) {
                        categoryFiltered.add(item);
                    }
                }
            }

            // Sort items by time
            Collections.sort(categoryFiltered, (item1, item2) -> {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
                    Date time1 = sdf.parse(item1.getTime());
                    Date time2 = sdf.parse(item2.getTime());
                    return time1.compareTo(time2);
                } catch (Exception e) {
                    return 0;
                }
            });

            // Group by time period and add section headers
            Map<String, List<NotifData.NotificationItem>> timeGroups = new HashMap<>();
            timeGroups.put(CATEGORY_MORNING, new ArrayList<>());
            timeGroups.put(CATEGORY_AFTERNOON, new ArrayList<>());
            timeGroups.put(CATEGORY_NIGHT, new ArrayList<>());

            for (NotifData.NotificationItem item : categoryFiltered) {
                String timeCategory = getTimeCategory(item.getTime());
                timeGroups.get(timeCategory).add(item);
            }

            // Add morning section if it has items
            if (!timeGroups.get(CATEGORY_MORNING).isEmpty()) {
                // Add section header
                filteredItems.add(new NotifData.NotificationItem(CATEGORY_MORNING, "", null, "#FFFFFF", "header", ""));
                filteredItems.addAll(timeGroups.get(CATEGORY_MORNING));
            }

            // Add afternoon section if it has items
            if (!timeGroups.get(CATEGORY_AFTERNOON).isEmpty()) {
                filteredItems.add(new NotifData.NotificationItem(CATEGORY_AFTERNOON, "", null, "#FFFFFF", "header", ""));
                filteredItems.addAll(timeGroups.get(CATEGORY_AFTERNOON));
            }

            // Add night section if it has items
            if (!timeGroups.get(CATEGORY_NIGHT).isEmpty()) {
                filteredItems.add(new NotifData.NotificationItem(CATEGORY_NIGHT, "", null, "#FFFFFF", "header", ""));
                filteredItems.addAll(timeGroups.get(CATEGORY_NIGHT));
            }
        } else if (currentTimeFilter.equals("upcoming")) {
            // For "Upcoming" filter, show notifications after current time

            // Get current time and round down to nearest hour
            Calendar now = Calendar.getInstance();
            int currentHour = now.get(Calendar.HOUR_OF_DAY);

            // Create a calendar with the rounded hour
            Calendar roundedTime = Calendar.getInstance();
            roundedTime.set(Calendar.HOUR_OF_DAY, currentHour);
            roundedTime.set(Calendar.MINUTE, 0);
            roundedTime.set(Calendar.SECOND, 0);
            roundedTime.set(Calendar.MILLISECOND, 0);

            Date roundedDate = roundedTime.getTime();

            // Filter by category and time
            List<NotifData.NotificationItem> upcomingItems = new ArrayList<>();

            for (NotifData.NotificationItem item : notificationItems) {
                if (currentCategoryFilters.isEmpty() || currentCategoryFilters.contains(item.getCategory())) {
                    // Parse the notification time
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
                        Date itemTime = sdf.parse(item.getTime());

                        // Create calendar for the item's time on today's date
                        Calendar itemCalendar = Calendar.getInstance();
                        itemCalendar.set(Calendar.HOUR_OF_DAY, 0);
                        itemCalendar.set(Calendar.MINUTE, 0);
                        itemCalendar.set(Calendar.SECOND, 0);
                        itemCalendar.set(Calendar.MILLISECOND, 0);

                        // Add the hours and minutes from the parsed time
                        Calendar tempCal = Calendar.getInstance();
                        tempCal.setTime(itemTime);
                        itemCalendar.set(Calendar.HOUR_OF_DAY, tempCal.get(Calendar.HOUR_OF_DAY));
                        itemCalendar.set(Calendar.MINUTE, tempCal.get(Calendar.MINUTE));

                        // Check if this time is after the rounded current time
                        if (itemCalendar.getTime().after(roundedDate) || itemCalendar.getTime().equals(roundedDate)) {
                            upcomingItems.add(item);
                        }
                    } catch (Exception e) {
                        // In case of parsing errors, we'll skip this item
                        Log.e("NotificationsActivity", "Error parsing time: " + e.getMessage());
                    }
                }
            }

            // Sort by time
            Collections.sort(upcomingItems, (item1, item2) -> {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
                    Date time1 = sdf.parse(item1.getTime());
                    Date time2 = sdf.parse(item2.getTime());
                    return time1.compareTo(time2);
                } catch (Exception e) {
                    return 0;
                }
            });

            // Add to filtered items
            if (!upcomingItems.isEmpty()) {

                filteredItems.addAll(upcomingItems);
            } else {
                // If no upcoming items, add a message
                filteredItems.add(new NotifData.NotificationItem("No upcoming notifications", "", "", "#FFFFFF", "header", ""));
            }
        } else {
            // Handle all_month filter with existing logic
            for (NotifData.NotificationItem item : notificationItems) {
                if (currentCategoryFilters.isEmpty() || currentCategoryFilters.contains(item.getCategory())) {
                    filteredItems.add(item);
                }
            }
        }

        notificationAdapter.notifyDataSetChanged();
    }

    private String getTimeCategory(String timeString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
            Date time = sdf.parse(timeString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);

            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

            if (hourOfDay >= 1 && hourOfDay < 12) {
                return CATEGORY_MORNING;
            } else if (hourOfDay >= 12 && hourOfDay < 18) {
                return CATEGORY_AFTERNOON;
            } else {
                return CATEGORY_NIGHT;
            }
        } catch (Exception e) {
            return CATEGORY_MORNING; // Default fallback
        }
    }

    private void setupPopupNavMenu() {
        // Move the popup menu outside the bottom nav card
        addFab.setOnClickListener(v -> togglePopupMenu());

        createNotifButton.setOnClickListener(v -> {
            // Hide the popup menu if showing
            addMenu.setVisibility(View.GONE);

            // Launch the notification creation activity
            Intent intent = new Intent(NotificationsActivity.this, NotifCreationActivity.class);
            startActivityForResult(intent, CREATE_NOTIFICATION_REQUEST);
        });


        homeButton.setOnClickListener(v -> {
            Intent homeIntent = new Intent(NotificationsActivity.this, HomeActivity.class);
            startActivity(homeIntent);
            finish();
        });
    }



    //TODO: UPDATE THE NOTIFICATION TYPE
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_NOTIFICATION_REQUEST && resultCode == RESULT_OK && data != null) {
            // Extract notification data
            int notificationId = data.getIntExtra("notification_id", -1);
            String notificationName = data.getStringExtra("notification_name");
            String notificationTime = data.getStringExtra("notification_time");
            String backgroundColor = data.getStringExtra("notification_background");
            String notifTypeWeeklyOrMonth = "";     //THIS IS KEPT BLANK

            // Create and add the new notification item
            NotifData.NotificationItem newItem = new NotifData.NotificationItem(
                    notificationId,
                    notificationName,
                    notificationTime,
                    "0/1 hr", // Default value, update as needed
                    backgroundColor,
                    "exercise", // Default category, update as needed
                    notifTypeWeeklyOrMonth

            );

            // Add the new item to the list
            notificationItems.add(newItem);

            // Update the recycler view
            notificationAdapter.notifyDataSetChanged();

            // Optionally apply filters
            filterNotifications();
        }
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

}