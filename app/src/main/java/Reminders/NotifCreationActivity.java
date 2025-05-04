package Reminders;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.fitwizard.fitwizard.R;

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
    private EditText messageEditText;
    private TextView timeTextView;
    private ToggleButton[] weekdayToggleButtons;
    private RadioGroup recurrenceTypeRadioGroup;
    private LinearLayout weekdaysContainer;
    private LinearLayout monthlyContainer;
    private CalendarView calendarView;
    private TextView selectedDaysTextView;
    private Button saveButton;
    private Button cancelButton;
    private Button btnEveryday;
    private Button btnWeekdays;
    private Button btnWeekends;

    private int selectedHour = 8;
    private int selectedMinute = 0;

    View colorSelectionView;
    View goalSelectionView;
    View medicationSelectionView;


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

        // Back button navigation
        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            // upper left button < , return to home page
            Intent intent = new Intent(NotifCreationActivity.this, NotificationsActivity.class);
            startActivity(intent);
            finish(); // closes the current activity
        });

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        nameEditText = findViewById(R.id.et_notification_name);
        timeTextView = findViewById(R.id.tv_selected_time);
        messageEditText = findViewById(R.id.et_notification_message);
        recurrenceTypeRadioGroup = findViewById(R.id.rg_recurrence_type);
        weekdaysContainer = findViewById(R.id.layout_weekdays);
        monthlyContainer = findViewById(R.id.layout_monthly);
        calendarView = findViewById(R.id.calendar_monthly);
        selectedDaysTextView = findViewById(R.id.tv_selected_days);
        saveButton = findViewById(R.id.btn_save);
        cancelButton = findViewById(R.id.btn_cancel);

        // Initialize new quick option buttons
        btnEveryday = findViewById(R.id.btn_everyday);
        btnWeekdays = findViewById(R.id.btn_weekdays);
        btnWeekends = findViewById(R.id.btn_weekends);

        colorSelectionView = findViewById(R.id.layout_color_selection);
        goalSelectionView = findViewById(R.id.layout_goal_selection);
        medicationSelectionView = findViewById(R.id.layout_medication_selection);


        // Initialize weekday toggle buttons
        String[] weekdays = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        weekdayToggleButtons = new ToggleButton[7];
        for (int i = 0; i < 7; i++) {
            weekdayToggleButtons[i] = findViewById(getResources().getIdentifier(
                    "toggle_" + weekdays[i].toLowerCase(), "id", getPackageName()));
        }

        // Set default time display
        updateTimeDisplay();

        // Default to weekday view
        weekdaysContainer.setVisibility(View.VISIBLE);
        monthlyContainer.setVisibility(View.GONE);





    }

    private void showColorSelectionDialog(){

    }

    private void showGoalSelectionDialog(){

    }

    private void showMedicationSelectionDialog(){

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

        // Quick option buttons
        btnEveryday.setOnClickListener(v -> selectAllDays(true));

        btnWeekdays.setOnClickListener(v -> {
            // First clear all selections
            selectAllDays(false);

            // Then select Monday to Friday (indices 0-4)
            for (int i = 0; i < 5; i++) {
                weekdayToggleButtons[i].setChecked(true);
            }
        });

        btnWeekends.setOnClickListener(v -> {
            // First clear all selections
            selectAllDays(false);

            // Then select Saturday and Sunday (indices 5-6)
            weekdayToggleButtons[5].setChecked(true);
            weekdayToggleButtons[6].setChecked(true);
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


        });


        colorSelectionView.setOnClickListener(v -> {
            // Show color selection dialog
            showColorSelectionDialog();
        });

        goalSelectionView.setOnClickListener(v -> {
            // Show goal selection dialog
            showGoalSelectionDialog();
        });

        medicationSelectionView.setOnClickListener(v -> {
            // Show medication selection dialog
            showMedicationSelectionDialog();
        });

        // Save button
        saveButton.setOnClickListener(v -> saveNotification());

        // Cancel button
        cancelButton.setOnClickListener(v -> finish());
    }

    // Helper method to select or deselect all days
    private void selectAllDays(boolean select) {
        for (ToggleButton button : weekdayToggleButtons) {
            button.setChecked(select);
        }
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
        String message = messageEditText.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a name for the reminder", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the selected color from the colorSelection view
        String backgroundColor = getSelectedBackgroundColor();

        // Get the formatted time from the TextView
        String timeString = timeTextView.getText().toString();

        NotifData.NotificationItem item = createNotificationItem(name, message);

        // Add to database and handle scheduling logic here
        // ...

        // Pass data back to NotificationsActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("notification_id", item.getNotifID());
        resultIntent.putExtra("notification_name", name);
        resultIntent.putExtra("notification_time", timeString);
        resultIntent.putExtra("notification_background", backgroundColor);
        resultIntent.putExtra("notification_type", item.getTypeMonthOrWeek()); // Add the type to the intent

        // You might want to serialize the entire item or pass each attribute separately
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    //TODO: setup background color selection

    // Helper method to get selected background color
    private String getSelectedBackgroundColor() {
        // Implement logic to get the selected color from color selection view
        // For example:
        if (colorSelectionView != null) {
            // Get the selected color tag or attribute
            // This is a placeholder - implement based on color selection UI
            Object selectedColor = colorSelectionView.getTag();
            if (selectedColor != null && selectedColor instanceof String) {
                return (String) selectedColor;
            }
        }
        return "#FFF2D9"; // Default color
    }


    private NotifData.NotificationItem createNotificationItem(String name, String message) {
        // Format time
        String timeString = timeTextView.getText().toString();

        // Determine recurrence pattern
        String durationText;
        // Determine notification type (weekly or monthly)
        String notifTypeWeekOrMonth;

        if (recurrenceTypeRadioGroup.getCheckedRadioButtonId() == R.id.rb_weekday) {
            notifTypeWeekOrMonth = "weekly"; // Set type as weekly

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
            notifTypeWeekOrMonth = "monthly"; // Set type as monthly

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

        // Include the type in the NotificationItem constructor
        return new NotifData.NotificationItem(name, timeString, durationText, backgroundColor, category, notifTypeWeekOrMonth);
    }
}