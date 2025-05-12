package Reminders;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitwizard.fitwizard.HomeActivity;
import com.fitwizard.fitwizard.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


//TODO: fix layout of bottom nav
//TODO: add padding to the TODAY sections between Morning /  Afternoon / Night time


public class NotificationsActivity extends AppCompatActivity {

    private NotifManager notifManager;

    // Add this constant at the class level
    private static final int CREATE_NOTIFICATION_REQUEST = 1001;

    // Constants for time categories
    private static final String CATEGORY_MORNING = "Morning";
    private static final String CATEGORY_AFTERNOON = "Afternoon";
    private static final String CATEGORY_NIGHT = "Night Time";

    private RecyclerView notificationsRecyclerView;
    private SectionedNotificationAdapter notificationAdapter;
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

    //Daily Notifications
    private TextView[] dayTextViews;
    private final String[] dayNames = {"sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"};

    // Calendar variables
    private Calendar currentCalendar;
    private TextView selectedDayTextView;
    private int selectedDayOfWeek = -1; // -1 means no day selected
    private int selectedDayOfMonth = -1; // -1 means no day selected


    // Added variables for month calendar view


    private ConstraintLayout monthCalendarContainer;
    private TextView currentMonthText;
    private GridLayout calendarGrid;
    private HorizontalScrollView calendarScrollView;
    private List<TextView> calendarDayCells = new ArrayList<>();
    private int selectedCalendarDay = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        initViews();
        setupBackButton();
        notifManager = new NotifManager(this);
        // Initialize list to prevent null crashes
        notificationItems = new ArrayList<>();
        filteredItems = new ArrayList<>();

        // Initialize calendar with current date
        currentCalendar = Calendar.getInstance();
        setupCalendarView(); // Upper horizontal scroll calendar
        setupMonthCalendarView(); // All month calendar view
        setupFilterButtons();
        setupNotificationsList();
        setupPopupNavMenu();
    }

    //YOUR REMINDERS // FULL CALENDAR VIEW
    private void adjustRecyclerViewMargin(boolean isAllMonthView) {
        RecyclerView recyclerView = findViewById(R.id.rv_notifications);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();

        if (isAllMonthView) {
            params.topMargin = getResources().getDimensionPixelSize(R.dimen.recycler_margin_all_month);
        } else {
            params.topMargin = getResources().getDimensionPixelSize(R.dimen.recycler_margin_default);
        }

        recyclerView.setLayoutParams(params);
    }

    private void initViews() {
        // Back button
        ImageButton btnBack = findViewById(R.id.btn_back);


        // Filter buttons
        btnFiltertoday = findViewById(R.id.btn_filter_today);
        btnFilterUpcoming = findViewById(R.id.btn_filter_upcoming);
        btnFilterAllMonth = findViewById(R.id.btn_filter_all_month);

        // Initialize day text views
        dayTextViews = new TextView[7];
        for (int i = 0; i < dayNames.length; i++) {
            int resId = getResources().getIdentifier("day_text_" + dayNames[i], "id", getPackageName());
            dayTextViews[i] = findViewById(resId);
        }

        // Category chips
        chipMedication = findViewById(R.id.chip_medication);

        // Notifications list
        notificationsRecyclerView = findViewById(R.id.rv_notifications);

        // Month calendar views
        monthCalendarContainer = findViewById(R.id.notif_calendar_month_view);
        currentMonthText = findViewById(R.id.tv_current_month);
        calendarScrollView = findViewById(R.id.calendar_scroll_view);

        // Add month navigation buttons
        ImageButton btnPrevMonth = findViewById(R.id.btn_prev_month);
        ImageButton btnNextMonth = findViewById(R.id.btn_next_month);

        btnPrevMonth.setOnClickListener(v -> changeMonth(-1));
        btnNextMonth.setOnClickListener(v -> changeMonth(1));

        // Bottom Navigation
        addFab = findViewById(R.id.fab_add);
        addMenu = findViewById(R.id.add_menu);
        createNotifButton = findViewById(R.id.btn_add_notif);
        homeButton = findViewById(R.id.nav_home);

    }

    // Method to change the month
    private void changeMonth(int monthOffset) {
        // Update the calendar
        currentCalendar.add(Calendar.MONTH, monthOffset);

        // Update month title
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        currentMonthText.setText(monthFormat.format(currentCalendar.getTime()));

        // Update CalendarView date
        CalendarView calendarView = findViewById(R.id.calendar_monthly);
        calendarView.setDate(currentCalendar.getTimeInMillis(), true, true);

        // Reset selection
        selectedCalendarDay = -1;
        TextView selectedDaysText = findViewById(R.id.tv_selected_days);
        selectedDaysText.setText("No days selected");

        // Update notifications if using a date filter
        if ("selected_day".equals(currentTimeFilter)) {
            // Reset to all month view since no day is selected
            updateTimeFilter("all_month");
            updateFilterButtonAppearance();
        }

        filterNotifications();
    }

    // Updated calendar setup method to work with CalendarView instead of GridLayout
    private void setupMonthCalendarView() {
        // Initialize current month text
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        currentMonthText.setText(monthFormat.format(currentCalendar.getTime()));

        // Initialize CalendarView
        CalendarView calendarView = findViewById(R.id.calendar_monthly);

        // Set the initial date to the current date
        calendarView.setDate(currentCalendar.getTimeInMillis());

        // Set date change listener
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Update the calendar to the selected date
            currentCalendar.set(Calendar.YEAR, year);
            currentCalendar.set(Calendar.MONTH, month);
            currentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Update selected day
            selectedCalendarDay = dayOfMonth;

            // Update selection tracking variables
            Calendar cal = (Calendar) currentCalendar.clone();
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            selectedDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0-based
            selectedDayOfMonth = dayOfMonth;

            // Update selected days text
            updateSelectedDaysText(dayOfMonth);

            // Update filter to show selected day's notifications
            updateTimeFilter("selected_day");
            updateFilterButtonAppearance();
            filterNotifications();
        });
    }

    // Helper method to update the selected days text view
    private void updateSelectedDaysText(int dayOfMonth) {
        TextView selectedDaysText = findViewById(R.id.tv_selected_days);

        // Format date as "Month Day" (e.g., "May 15")
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d", Locale.getDefault());
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String formattedDate = dateFormat.format(cal.getTime());

        // Check if the selected day has notifications
        if (dayHasNotifications(dayOfMonth)) {
            selectedDaysText.setText("Selected: " + formattedDate + " (has notifications)");
        } else {
            selectedDaysText.setText("Selected: " + formattedDate);
        }
    }

    private void updateMonthCalendarTitle() {
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
        currentMonthText.setText(monthYearFormat.format(currentCalendar.getTime()));
    }

    private void populateCalendarGrid() {
        // Clear existing cells
        calendarGrid.removeAllViews();
        calendarDayCells.clear();

        // Get current month details
        Calendar calendar = (Calendar) currentCalendar.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int monthStartDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // Adjust for 0-based indexing
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Get current day for highlighting
        Calendar today = Calendar.getInstance();
        boolean isCurrentMonth = (today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == calendar.get(Calendar.MONTH));
        int currentDay = today.get(Calendar.DAY_OF_MONTH);

        // If a day was previously selected, try to reselect it
        if (selectedCalendarDay > 0 && selectedCalendarDay <= daysInMonth) {
            selectCalendarDay(selectedCalendarDay);
        }
    }


    private void selectCalendarDay(int dayOfMonth) {
        // Update UI to show selected day
        selectedCalendarDay = dayOfMonth;

        // Clear previous selection
        for (TextView cell : calendarDayCells) {
            if (cell != null) {
                // Reset background (you may need a more sophisticated approach)
                cell.setBackground(null);
            }
        }

        // Highlight selected day
        int dayIndex = dayOfMonth - 1 + getMonthStartOffset();
        if (dayIndex >= 0 && dayIndex < calendarDayCells.size() && calendarDayCells.get(dayIndex) != null) {
            TextView selectedCell = calendarDayCells.get(dayIndex);

            // Apply selection style
            GradientDrawable bgShape = new GradientDrawable();
            bgShape.setShape(GradientDrawable.OVAL);

            // Different color if the day has notifications
            if (dayHasNotifications(dayOfMonth)) {
                bgShape.setColor(Color.parseColor(getColorForNotification(dayOfMonth)));
            } else {
                bgShape.setColor(Color.parseColor("#FF4081")); // Pink
            }

            selectedCell.setBackground(bgShape);
        }



        // Update day selection variables
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        selectedDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0-based
        selectedDayOfMonth = dayOfMonth;

        // Update filter to show selected day's notifications
        updateTimeFilter("selected_day");
        updateFilterButtonAppearance();
        filterNotifications();
    }

    private int getMonthStartOffset() {
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.get(Calendar.DAY_OF_WEEK) - 1; // Adjust for 0-based indexing
    }
    private boolean dayHasNotifications(int dayOfMonth) {
        // Check if any notification is scheduled for this day of the month
        for (NotifData.NotificationItem item : notificationItems) {
            // Check monthly notifications
            if ("Monthly".equals(item.getTypeMonthOrWeek()) &&
                    item.getActiveDaysOfMonth() != null &&
                    dayOfMonth <= item.getActiveDaysOfMonth().length &&
                    item.getActiveDaysOfMonth()[dayOfMonth - 1]) {
                return true;
            }

            // Check weekly notifications for the current month (more complex)
            if ("Weekly".equals(item.getTypeMonthOrWeek()) && item.getActiveDaysOfWeek() != null) {
                // Get the day of week for this day of month
                Calendar cal = (Calendar) currentCalendar.clone();
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // Convert to 0-based index

                if (dayOfWeek >= 0 && dayOfWeek < 7 && item.getActiveDaysOfWeek()[dayOfWeek]) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getColorForNotification(int dayOfMonth) {
        // Find the notification color for the first notification on this day
        // This is simplified - you might want to prioritize certain notification types
        for (NotifData.NotificationItem item : notificationItems) {
            // Check monthly notifications
            if ("Monthly".equals(item.getTypeMonthOrWeek()) &&
                    item.getActiveDaysOfMonth() != null &&
                    dayOfMonth <= item.getActiveDaysOfMonth().length &&
                    item.getActiveDaysOfMonth()[dayOfMonth - 1]) {
                return item.getBackgroundColor();
            }

            // Check weekly notifications
            if ("Weekly".equals(item.getTypeMonthOrWeek()) && item.getActiveDaysOfWeek() != null) {
                Calendar cal = (Calendar) currentCalendar.clone();
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;

                if (dayOfWeek >= 0 && dayOfWeek < 7 && item.getActiveDaysOfWeek()[dayOfWeek]) {
                    return item.getBackgroundColor();
                }
            }
        }

        // Default color
        return "#FF9800"; // Orange
    }


    //go back to home screen
    private void setupBackButton() {
        // Back button navigation
        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            // upper left button < , return to home page
            Intent intent = new Intent(NotificationsActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // closes the current activity
        });
    }

    //HORIZONTAL SCROLL CALENDAR AT TOP OF SCREEN
    private void setupCalendarView() {
        // First ensure all day text views are properly initialized
        if (dayTextViews == null || dayTextViews.length != 7) {
            // Handle potential initialization error
            Log.e("NotificationsActivity", "Day text views not properly initialized");
            return;
        }

        // Initialize the calendar with current date
        currentCalendar = Calendar.getInstance();

        // Update all day text views with the correct dates for the current week
        updateAllDayTexts();

        // Add click listeners to day textviews
        for (int i = 0; i < dayTextViews.length; i++) {
            final int dayIndex = i;

            // Skip if the TextView is null
            if (dayTextViews[i] == null) {
                Log.e("NotificationsActivity", "Day text view at index " + i + " is null");
                continue;
            }

            final TextView dayTextView = dayTextViews[i];

            dayTextView.setOnClickListener(v -> {
                // Toggle selection
                if (selectedDayTextView == dayTextView) {
                    // If already selected, deselect it
                    deselectDay();
                } else {
                    // Select the new day
                    selectDay(dayTextView, dayIndex);
                }
            });
        }

        try {
            // Set up the default selected day (today)
            int todayDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK) - 1; // Adjust to 0-based index
            if (todayDayOfWeek >= 0 && todayDayOfWeek < dayTextViews.length && dayTextViews[todayDayOfWeek] != null) {
                selectDay(dayTextViews[todayDayOfWeek], todayDayOfWeek);
            }
        } catch (Exception e) {
            Log.e("NotificationsActivity", "Error selecting today: " + e.getMessage());
            // Don't select any day if there's an error
        }
    }

    /** HORIZONTAL CALENDAR VIEW
     * Updates all day text views with the correct dates for the current week
     */
    private void updateAllDayTexts() {
        // Get the first day of the week (Sunday)
        Calendar weekStart = (Calendar) currentCalendar.clone();
        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        // Update each day text view
        for (int i = 0; i < dayTextViews.length; i++) {
            if (dayTextViews[i] != null) {
                Calendar dayCalendar = (Calendar) weekStart.clone();
                dayCalendar.add(Calendar.DAY_OF_MONTH, i);
                dayTextViews[i].setText(String.valueOf(dayCalendar.get(Calendar.DAY_OF_MONTH)));

                // Check if this is today and style it differently
                Calendar today = Calendar.getInstance();
                if (dayCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        dayCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    dayTextViews[i].setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                } else {
                    dayTextViews[i].setTextColor(getResources().getColor(android.R.color.black));
                }

                // Reset background (will be set in selectDay if needed)
                dayTextViews[i].setBackgroundResource(R.drawable.circle_outline_pink);
            }
        }
    }

    /** HORIZONTAL CALENDAR VIEW
     * Selects a day and updates the UI and filters
     */
    private void selectDay(TextView dayTextView, int dayIndex) {
        // First deselect previous day if any
        deselectDay();

        // Select the new day
        selectedDayTextView = dayTextView;

        try {
            dayTextView.setBackgroundResource(R.drawable.circle_outline_pink_double);
            dayTextView.setTextColor(getResources().getColor(android.R.color.white));

            // Calculate the actual date for the selected day
            Calendar tempCal = (Calendar) currentCalendar.clone();
            tempCal.set(Calendar.DAY_OF_WEEK, dayIndex + 1); // Convert 0-based index to Calendar.DAY_OF_WEEK

            // Store selected day information
            selectedDayOfWeek = dayIndex;
            selectedDayOfMonth = tempCal.get(Calendar.DAY_OF_MONTH);

            // Update filter to show only notifications for this day
            updateTimeFilter("selected_day");
            filterNotifications();
        } catch (Exception e) {
            Log.e("NotificationsActivity", "Error selecting day: " + e.getMessage());
            deselectDay(); // Reset if there's an error
        }
    }

    /** HORIZONTAL CALENDAR VIEW
     * Deselects the currently selected day
     */
    private void deselectDay() {
        if (selectedDayTextView != null) {
            try {
                selectedDayTextView.setBackgroundResource(R.drawable.circle_outline_pink);
                selectedDayTextView.setTextColor(getResources().getColor(android.R.color.black));

                // Check if it's today and restore its special color
                int todayDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
                if (selectedDayOfWeek == todayDayOfWeek) {
                    selectedDayTextView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                }
            } catch (Exception e) {
                Log.e("NotificationsActivity", "Error deselecting day: " + e.getMessage());
            }
        }

        selectedDayTextView = null;
        selectedDayOfWeek = -1;
        selectedDayOfMonth = -1;

        // Reset to default filter if a day was previously selected
        if (currentTimeFilter.equals("selected_day")) {
            updateTimeFilter("today");
            filterNotifications();
        }
    }

    private void setupFilterButtons() {
        // Time filter buttons
        btnFiltertoday.setOnClickListener(v -> {
            // If currently in all month view, hide it
            if (monthCalendarContainer.getVisibility() == View.VISIBLE) {
                monthCalendarContainer.setVisibility(View.GONE);
            }

            updateTimeFilter("today");
            updateFilterButtonAppearance();
            filterNotifications();
        });

        btnFilterUpcoming.setOnClickListener(v -> {
            // If currently in all month view, hide it
            if (monthCalendarContainer.getVisibility() == View.VISIBLE) {
                monthCalendarContainer.setVisibility(View.GONE);
            }

            updateTimeFilter("upcoming");
            updateFilterButtonAppearance();
            filterNotifications();
        });

        btnFilterAllMonth.setOnClickListener(v -> {
            boolean isMonthViewVisible = monthCalendarContainer.getVisibility() == View.VISIBLE;
            monthCalendarContainer.setVisibility(isMonthViewVisible ? View.GONE : View.VISIBLE);

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

        // Handle visibility of calendar views
        switch (filter) {
            case "all_month":
                monthCalendarContainer.setVisibility(View.VISIBLE);
                calendarScrollView.setVisibility(View.GONE);
                adjustRecyclerViewMargin(true);
                break;
            case "today":
            case "upcoming":
                monthCalendarContainer.setVisibility(View.GONE);
                calendarScrollView.setVisibility(View.VISIBLE);
                adjustRecyclerViewMargin(false);
                break;
            case "selected_day":
                // Keep current calendar view state
                break;
        }

        // Reset day selection when filter changes (except for selected_day)
        if (!"selected_day".equals(filter)) {
            selectedDayOfWeek = -1;
            selectedDayOfMonth = -1;
            selectedCalendarDay = -1;

            // Deselect any selected day in the week view
            if (selectedDayTextView != null) {
                selectedDayTextView.setBackgroundResource(R.drawable.circle_outline_pink);
                selectedDayTextView.setTextColor(getResources().getColor(android.R.color.black));
                selectedDayTextView = null;
            }
        }
    }


    /**
     * Checks if a notification is scheduled for a specific day
     * @param item The notification item to check
     * @param dayOfWeek Day of week (0-6, Sunday to Saturday)
     * @param dayOfMonth Day of month (1-31)
     * @return True if the notification is scheduled for the given day
     */
    private boolean isNotificationScheduledForDay(NotifData.NotificationItem item, int dayOfWeek, int dayOfMonth) {
        // Check if it's a weekly notification active on this day of week
        if ("Weekly".equals(item.getTypeMonthOrWeek()) &&
                item.getActiveDaysOfWeek() != null &&
                dayOfWeek >= 0 && dayOfWeek < 7 &&
                item.getActiveDaysOfWeek()[dayOfWeek]) {
            return true;
        }

        // Check if it's a monthly notification active on this day of month
        if ("Monthly".equals(item.getTypeMonthOrWeek()) &&
                item.getActiveDaysOfMonth() != null &&
                dayOfMonth > 0 && dayOfMonth <= 31 &&
                item.getActiveDaysOfMonth()[dayOfMonth - 1]) {
            return true;
        }

        return false;
    }

    /**
     * Gets notifications scheduled for a specific day
     * @param allItems The full list of notification items
     * @param dayOfWeek Day of week (0-6, Sunday to Saturday)
     * @param dayOfMonth Day of month (1-31)
     * @return List of notifications scheduled for the given day
     */
    private List<NotifData.NotificationItem> getNotificationsForDay(List<NotifData.NotificationItem> allItems,
                                                                    int dayOfWeek, int dayOfMonth) {
        List<NotifData.NotificationItem> result = new ArrayList<>();

        for (NotifData.NotificationItem item : allItems) {
            if (isNotificationScheduledForDay(item, dayOfWeek, dayOfMonth)) {
                result.add(item);
            }
        }

        // Sort by time
        Collections.sort(result, (item1, item2) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
                Date time1 = sdf.parse(item1.getTime());
                Date time2 = sdf.parse(item2.getTime());
                assert time1 != null;
                return time1.compareTo(time2);
            } catch (Exception e) {
                return 0;
            }
        });

        return result;
    }



    /**
     * Formats a date for display
     * @param calendar The calendar with the date to format
     * @return Formatted date string (e.g., "Monday, May 4")
     */
    private String formatDate(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.US);
        return dateFormat.format(calendar.getTime());
    }

    /**
     * Gets the day of week name
     * @param dayOfWeek Day of week (0-6, Sunday to Saturday)
     * @return Name of the day
     */
    private String getDayOfWeekName(int dayOfWeek) {
        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        if (dayOfWeek >= 0 && dayOfWeek < dayNames.length) {
            return dayNames[dayOfWeek];
        }
        return "";
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

        if (notificationItems == null) {
            notificationItems = new ArrayList<>();
        }

        // Load saved notifications from NotifManager
        notificationItems.clear();
        notificationItems.addAll(notifManager.getAllNotifications());

        // Filtered list
        filteredItems = new ArrayList<>();

        // Set up the RecyclerView with the loaded data
        notificationAdapter = new SectionedNotificationAdapter(this, filteredItems);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationsRecyclerView.setAdapter(notificationAdapter);

        // Initial filtering
        filterNotifications();
    }

    private void filterNotifications() {
        filteredItems.clear();

        // First, apply category filters regardless of time filter
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

        // Now apply the time filter
        if (currentTimeFilter.equals("today")) {
            // For "Today" filter, show notifications for current day and organize by time periods
            filterTodayNotifications(categoryFiltered);
        } else if (currentTimeFilter.equals("upcoming")) {
            // For "Upcoming" filter, show notifications after current time
            filterUpcomingNotifications(categoryFiltered);
        } else if (currentTimeFilter.equals("selected_day")) {
            // For "selected_day" filter, show notifications scheduled for the selected day
            filterSelectedDayNotifications(categoryFiltered);
        } else {
            // Handle all_month filter with existing logic
            filteredItems.addAll(categoryFiltered);
        }

        notificationAdapter.notifyDataSetChanged();
    }

    /**
     * Filters notifications for the selected day in the calendar view
     */
    private void filterSelectedDayNotifications(List<NotifData.NotificationItem> categoryFiltered) {
        if (selectedDayOfWeek == -1) {
            // No day selected, don't show any notifications
            filteredItems.add(new NotifData.NotificationItem("No day selected", "", "", "#FFFFFF", "header", ""));
            return;
        }

        // Get the selected day information
        int dayOfWeek = selectedDayOfWeek;
        int dayOfMonth = selectedDayOfMonth;

        // Create lists for each time period
        List<NotifData.NotificationItem> morningItems = new ArrayList<>();
        List<NotifData.NotificationItem> afternoonItems = new ArrayList<>();
        List<NotifData.NotificationItem> nightItems = new ArrayList<>();

        for (NotifData.NotificationItem item : categoryFiltered) {
            // For weekly notifications, check if this day of week is active
            if ("Weekly".equals(item.getTypeMonthOrWeek()) &&
                    item.getActiveDaysOfWeek() != null &&
                    dayOfWeek >= 0 && dayOfWeek < 7 &&
                    item.getActiveDaysOfWeek()[dayOfWeek]) {

                // Add to the appropriate time category
                String timeCategory = getTimeCategory(item.getTime());
                if (CATEGORY_MORNING.equals(timeCategory)) {
                    morningItems.add(item);
                } else if (CATEGORY_AFTERNOON.equals(timeCategory)) {
                    afternoonItems.add(item);
                } else if (CATEGORY_NIGHT.equals(timeCategory)) {
                    nightItems.add(item);
                }
            }

            // For monthly notifications, check if this day of month is active
            else if ("Monthly".equals(item.getTypeMonthOrWeek()) &&
                    item.getActiveDaysOfMonth() != null &&
                    dayOfMonth > 0 && dayOfMonth <= 31 &&
                    item.getActiveDaysOfMonth()[dayOfMonth - 1]) { // Adjust for 0-based index

                // Add to the appropriate time category
                String timeCategory = getTimeCategory(item.getTime());
                if (CATEGORY_MORNING.equals(timeCategory)) {
                    morningItems.add(item);
                } else if (CATEGORY_AFTERNOON.equals(timeCategory)) {
                    afternoonItems.add(item);
                } else if (CATEGORY_NIGHT.equals(timeCategory)) {
                    nightItems.add(item);
                }
            }
        }

        // Sort each category by time
        Comparator<NotifData.NotificationItem> timeComparator = (item1, item2) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
                Date time1 = sdf.parse(item1.getTime());
                Date time2 = sdf.parse(item2.getTime());
                assert time1 != null;
                return time1.compareTo(time2);
            } catch (Exception e) {
                return 0;
            }
        };

        Collections.sort(morningItems, timeComparator);
        Collections.sort(afternoonItems, timeComparator);
        Collections.sort(nightItems, timeComparator);

        // Add morning section if it has items
        if (!morningItems.isEmpty()) {
            filteredItems.add(new NotifData.NotificationItem(CATEGORY_MORNING, "", null, "#FFFFFF", "header", ""));
            filteredItems.addAll(morningItems);
        }

        // Add afternoon section if it has items
        if (!afternoonItems.isEmpty()) {
            filteredItems.add(new NotifData.NotificationItem(CATEGORY_AFTERNOON, "", null, "#FFFFFF", "header", ""));
            filteredItems.addAll(afternoonItems);
        }

        // Add night section if it has items
        if (!nightItems.isEmpty()) {
            filteredItems.add(new NotifData.NotificationItem(CATEGORY_NIGHT, "", null, "#FFFFFF", "header", ""));
            filteredItems.addAll(nightItems);
        }

        // If no notifications for this day, show a message
        if (filteredItems.isEmpty()) {
            Calendar selectedDate = (Calendar) currentCalendar.clone();
            selectedDate.set(Calendar.DAY_OF_WEEK, dayOfWeek + 1); // Adjust from 0-based to Calendar's 1-based

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.US);
            String formattedDate = dateFormat.format(selectedDate.getTime());

            filteredItems.add(new NotifData.NotificationItem("No notifications for " + formattedDate,
                    "", "", "#FFFFFF", "header", ""));
        }
    }

    /**
     * Filter and show notifications that are upcoming after the current time
     */
    private void filterUpcomingNotifications(List<NotifData.NotificationItem> categoryFiltered) {
        // Get current time and round down to nearest hour
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int todayDayOfWeek = now.get(Calendar.DAY_OF_WEEK) - 1; // Convert to 0-based index
        int todayDayOfMonth = now.get(Calendar.DAY_OF_MONTH);

        // Create a calendar with the rounded hour
        Calendar roundedTime = Calendar.getInstance();
        roundedTime.set(Calendar.HOUR_OF_DAY, currentHour);
        roundedTime.set(Calendar.MINUTE, 0);
        roundedTime.set(Calendar.SECOND, 0);
        roundedTime.set(Calendar.MILLISECOND, 0);

        Date roundedDate = roundedTime.getTime();

        // Filter notifications scheduled for today and after current time
        List<NotifData.NotificationItem> upcomingItems = new ArrayList<>();

        for (NotifData.NotificationItem item : categoryFiltered) {
            boolean isScheduledForToday = false;

            // Check weekly notifications
            if ("Weekly".equals(item.getTypeMonthOrWeek()) &&
                    item.getActiveDaysOfWeek() != null &&
                    todayDayOfWeek >= 0 && todayDayOfWeek < 7 &&
                    item.getActiveDaysOfWeek()[todayDayOfWeek]) {
                isScheduledForToday = true;
            }

            // Check monthly notifications
            else if ("Monthly".equals(item.getTypeMonthOrWeek()) &&
                    item.getActiveDaysOfMonth() != null &&
                    todayDayOfMonth > 0 && todayDayOfMonth <= 31 &&
                    item.getActiveDaysOfMonth()[todayDayOfMonth - 1]) {
                isScheduledForToday = true;
            }

            if (isScheduledForToday) {
                // Parse the notification time to check if it's after current time
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
    }

    /**
     * Filter notifications for today and organize by time periods
     */
    private void filterTodayNotifications(List<NotifData.NotificationItem> categoryFiltered) {
        // Get today's day of week and day of month
        Calendar today = Calendar.getInstance();
        int todayDayOfWeek = today.get(Calendar.DAY_OF_WEEK) - 1; // Convert to 0-based index
        int todayDayOfMonth = today.get(Calendar.DAY_OF_MONTH);

        List<NotifData.NotificationItem> todayItems = new ArrayList<>();

        // Filter items that are scheduled for today
        for (NotifData.NotificationItem item : categoryFiltered) {
            boolean isScheduledForToday = false;

            // Check weekly notifications
            if ("Weekly".equals(item.getTypeMonthOrWeek()) &&
                    item.getActiveDaysOfWeek() != null &&
                    todayDayOfWeek >= 0 && todayDayOfWeek < 7 &&
                    item.getActiveDaysOfWeek()[todayDayOfWeek]) {
                isScheduledForToday = true;
            }

            // Check monthly notifications
            else if ("Monthly".equals(item.getTypeMonthOrWeek()) &&
                    item.getActiveDaysOfMonth() != null &&
                    todayDayOfMonth > 0 && todayDayOfMonth <= 31 &&
                    item.getActiveDaysOfMonth()[todayDayOfMonth - 1]) {
                isScheduledForToday = true;
            }

            if (isScheduledForToday) {
                todayItems.add(item);
            }
        }

        // Sort by time
        Collections.sort(todayItems, (item1, item2) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
                Date time1 = sdf.parse(item1.getTime());
                Date time2 = sdf.parse(item2.getTime());
                assert time1 != null;
                return time1.compareTo(time2);
            } catch (Exception e) {
                return 0;
            }
        });

        // Group by time period
        Map<String, List<NotifData.NotificationItem>> timeGroups = new HashMap<>();
        timeGroups.put(CATEGORY_MORNING, new ArrayList<>());
        timeGroups.put(CATEGORY_AFTERNOON, new ArrayList<>());
        timeGroups.put(CATEGORY_NIGHT, new ArrayList<>());

        for (NotifData.NotificationItem item : todayItems) {
            String timeCategory = getTimeCategory(item.getTime());
            timeGroups.get(timeCategory).add(item);
        }

        // Add morning section if it has items
        if (!timeGroups.get(CATEGORY_MORNING).isEmpty()) {
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

        // If no notifications for today, show a message
        if (filteredItems.isEmpty()) {
            filteredItems.add(new NotifData.NotificationItem("No notifications for today", "", "", "#FFFFFF", "header", ""));
        }
    }




    /**
     * Determines the time category (Morning, Afternoon, Night) based on the time string
     */
    private String getTimeCategory(String timeString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
            Date time = sdf.parse(timeString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);

            int hour = calendar.get(Calendar.HOUR_OF_DAY);

            if (hour < 12) {
                return CATEGORY_MORNING;
            } else if (hour < 17) {
                return CATEGORY_AFTERNOON;
            } else {
                return CATEGORY_NIGHT;
            }
        } catch (Exception e) {
            Log.e("NotificationsActivity", "Error parsing time: " + e.getMessage());
            return CATEGORY_MORNING; // Default to morning if we can't parse the time
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
            String category = data.getStringExtra("notification_category");
            if (category == null) category = "exercise"; // Default category if not provided

            // Get the type of notification (Weekly or Monthly)
            String notifTypeWeeklyOrMonth = data.getStringExtra("notification_type");
            if (notifTypeWeeklyOrMonth == null) notifTypeWeeklyOrMonth = "Weekly"; // Default to Weekly

            // Create the appropriate active days arrays based on type
            boolean[] activeDaysOfWeek = null;
            boolean[] activeDaysOfMonth = null;

            if ("Weekly".equals(notifTypeWeeklyOrMonth)) {
                activeDaysOfWeek = new boolean[7];
                // Check if specific days were selected, otherwise default to all days
                if (data.hasExtra("active_days")) {
                    boolean[] days = data.getBooleanArrayExtra("active_days");
                    if (days != null && days.length == 7) {
                        activeDaysOfWeek = days;
                    } else {
                        // Default to all days active
                        for (int i = 0; i < 7; i++) {
                            activeDaysOfWeek[i] = true;
                        }
                    }
                } else {
                    // Default to all days active
                    for (int i = 0; i < 7; i++) {
                        activeDaysOfWeek[i] = true;
                    }
                }
            } else if ("Monthly".equals(notifTypeWeeklyOrMonth)) {
                activeDaysOfMonth = new boolean[31];
                // Check if specific dates were selected, otherwise default to 1st day
                if (data.hasExtra("active_dates")) {
                    boolean[] dates = data.getBooleanArrayExtra("active_dates");
                    if (dates != null && dates.length == 31) {
                        activeDaysOfMonth = dates;
                    } else {
                        // Default to 1st day active
                        activeDaysOfMonth[0] = true;
                    }
                } else {
                    // Default to 1st day active
                    activeDaysOfMonth[0] = true;
                }
            }

            // Get duration or set default
            String duration = data.getStringExtra("notification_duration");
            if (duration == null) duration = "0/1 hr"; // Default value

            // Create and add the new notification item using the constructor with ID and active days
            NotifData.NotificationItem newItem = new NotifData.NotificationItem(
                    notificationId,
                    notificationName,
                    notificationTime,
                    duration,
                    backgroundColor,
                    category,
                    notifTypeWeeklyOrMonth,
                    activeDaysOfWeek,
                    activeDaysOfMonth
            );

            // Add the new item to the list
       //     notificationItems.add(newItem);

            notifManager.addNotification(newItem);
            notificationItems.clear();
            notificationItems.addAll(notifManager.getAllNotifications());

            // Update the recycler view
            notificationAdapter.notifyDataSetChanged();

            // Apply filters
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