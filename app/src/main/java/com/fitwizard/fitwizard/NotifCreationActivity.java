package com.fitwizard.fitwizard;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class NotifCreationActivity extends AppCompatActivity {

    private EditText nameEditText;
    private TextView timeTextView;
    private ToggleButton[] weekdayToggleButtons;
    private RadioGroup recurrenceTypeRadioGroup;
    private LinearLayout weekdaysContainer;
    private LinearLayout monthlyContainer;
    private CalendarView calendarView;
    private TextView selectedDaysTextView;
    private Button saveButton;
    private Button cancelButton;

    private int selectedHour = 8;
    private int selectedMinute = 0;

    // To store selected days for monthly reminders
    private Set<Integer> selectedDaysOfMonth = new HashSet<>();
    private Calendar currentCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif_creation);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        nameEditText = findViewById(R.id.et_notification_name);
        timeTextView = findViewById(R.id.tv_selected_time);
        recurrenceTypeRadioGroup = findViewById(R.id.rg_recurrence_type);
        weekdaysContainer = findViewById(R.id.layout_weekdays);
        monthlyContainer = findViewById(R.id.layout_monthly);
        calendarView = findViewById(R.id.calendar_monthly);
        selectedDaysTextView = findViewById(R.id.tv_selected_days);
        saveButton = findViewById(R.id.btn_save);
        cancelButton = findViewById(R.id.btn_cancel);

        // Initialize weekday toggle buttons
        String[] weekdays = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        weekdayToggleButtons = new ToggleButton[7];
        for (int i = 0; i < 7; i++) {
            weekdayToggleButtons[i] = findViewById(getResources().getIdentifier(
                    "toggle" + weekdays[i].toLowerCase(), "id", getPackageName()));
        }

        // Set default time display
        updateTimeDisplay();

        // Default to weekday view
        weekdaysContainer.setVisibility(View.VISIBLE);
        monthlyContainer.setVisibility(View.GONE);
    }

    private void setupListeners() {
        // Time selection
        timeTextView.setOnClickListener(v -> showTimePickerDialog());

        // Recurrence type selection
        recurrenceTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_weekday) {
                weekdaysContainer.setVisibility(View.VISIBLE);
                monthlyContainer.setVisibility(View.GONE);
            } else if (checkedId == R.id.rb_monthly) {
                weekdaysContainer.setVisibility(View.GONE);
                monthlyContainer.setVisibility(View.VISIBLE);
            }
        });

        // Calendar date selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Update the selected day of month
            if (selectedDaysOfMonth.contains(dayOfMonth)) {
                selectedDaysOfMonth.remove(dayOfMonth);
            } else {
                selectedDaysOfMonth.add(dayOfMonth);
            }

            // Update the month being viewed
            currentCalendar.set(Calendar.YEAR, year);
            currentCalendar.set(Calendar.MONTH, month);

            // Update selected days display
            updateSelectedDaysText();

            // Optional: Highlight selected dates on calendar
            // This would require a custom calendar implementation
        });

        // Save button
        saveButton.setOnClickListener(v -> saveNotification());

        // Cancel button
        cancelButton.setOnClickListener(v -> finish());
    }

    private void updateSelectedDaysText() {
        if (selectedDaysOfMonth.isEmpty()) {
            selectedDaysTextView.setText("No days selected");
        } else {
            List<Integer> sortedDays = new ArrayList<>(selectedDaysOfMonth);
            Collections.sort(sortedDays);

            StringBuilder builder = new StringBuilder("Selected days: ");
            for (int i = 0; i < sortedDays.size(); i++) {
                builder.append(sortedDays.get(i));
                if (i < sortedDays.size() - 1) {
                    builder.append(", ");
                }
            }
            selectedDaysTextView.setText(builder.toString());
        }
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    updateTimeDisplay();
                },
                selectedHour,
                selectedMinute,
                false
        );
        timePickerDialog.show();
    }

    private void updateTimeDisplay() {
        // Format time for display
        String format = "hh:mm a";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, selectedHour);
        cal.set(Calendar.MINUTE, selectedMinute);
        timeTextView.setText(sdf.format(cal.getTime()));
    }

    private void saveNotification() {
        String name = nameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a name for the reminder", Toast.LENGTH_SHORT).show();
            return;
        }

        NotifData.NotificationItem item = createNotificationItem(name);

        // Add to database and handle scheduling logic here
        // ...

        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private NotifData.NotificationItem createNotificationItem(String name) {
        // Format time
        String timeString = timeTextView.getText().toString();

        // Determine recurrence pattern
        String durationText;

        if (recurrenceTypeRadioGroup.getCheckedRadioButtonId() == R.id.rb_weekday) {
            // Collect selected weekdays
            List<String> selectedDays = new ArrayList<>();
            String[] weekdays = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

            for (int i = 0; i < weekdayToggleButtons.length; i++) {
                if (weekdayToggleButtons[i].isChecked()) {
                    selectedDays.add(weekdays[i]);
                }
            }

            if (selectedDays.isEmpty()) {
                durationText = "One-time";
            } else if (selectedDays.size() == 7) {
                durationText = "Every day";
            } else if (selectedDays.size() == 5 &&
                    selectedDays.contains("Mon") &&
                    selectedDays.contains("Tue") &&
                    selectedDays.contains("Wed") &&
                    selectedDays.contains("Thu") &&
                    selectedDays.contains("Fri")) {
                durationText = "Weekdays";
            } else if (selectedDays.size() == 2 &&
                    selectedDays.contains("Sat") &&
                    selectedDays.contains("Sun")) {
                durationText = "Weekends";
            } else {
                durationText = TextUtils.join(", ", selectedDays);
            }
        } else {
            // Monthly recurrence - multiple days
            if (selectedDaysOfMonth.isEmpty()) {
                durationText = "Monthly (no days selected)";
            } else {
                List<Integer> sortedDays = new ArrayList<>(selectedDaysOfMonth);
                Collections.sort(sortedDays);

                if (sortedDays.size() == 1) {
                    durationText = "Monthly on day " + sortedDays.get(0);
                } else {
                    durationText = "Monthly on days " + TextUtils.join(", ", sortedDays);
                }
            }
        }

        // For this example, we'll use a default color
        String backgroundColor = "#FFF2D9";
        String category = "Activity";

        return new NotifData.NotificationItem(name, timeString, durationText, backgroundColor, category);
    }
}