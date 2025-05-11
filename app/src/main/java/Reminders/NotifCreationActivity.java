package Reminders;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.GridLayout;
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


    private NotifManager notificationManager;

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


    // Color options with hex values (without # prefix)
    private final String[] colorOptions = {"8FE5E5", "CCF3C1", "F7F9BF", "FCE3B8", "F6D6D6", "E3D3F4"};
    private String selectedColor = "#8FE5E5"; // Default color (changed from #FFF2D9)

    private String selectedCategory = "General"; // Default category


    // To store selected days for monthly reminders
    private Set<Integer> selectedDaysOfMonth = new HashSet<>();
    private Calendar currentCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif_creation);

        notificationManager = new NotifManager(this);

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


        goalSelectionView = findViewById(R.id.layout_goal_selection);
        medicationSelectionView = findViewById(R.id.layout_medication_selection);

        // Get references to both the row layout and the circle indicator
        colorSelectionView = findViewById(R.id.layout_color_selection);
        colorSelectionView = findViewById(R.id.view_selected_color);


        // Set initial color indicator
        updateColorIndicator(selectedColor);

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


    private void showColorSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Color");

        // Create layout for color options
        LinearLayout colorLayout = new LinearLayout(this);
        colorLayout.setOrientation(LinearLayout.VERTICAL);
        colorLayout.setPadding(40, 40, 40, 40);

        // Create a grid layout for the colors
        GridLayout gridLayout = new GridLayout(this);
        gridLayout.setColumnCount(3); // 3 columns
        gridLayout.setUseDefaultMargins(true);

        final View[] colorViews = new View[colorOptions.length];

        for (int i = 0; i < colorOptions.length; i++) {
            final String colorHex = "#" + colorOptions[i];

            // Create color circle view
            View colorView = new View(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 120; // px
            params.height = 120; // px
            params.setMargins(20, 20, 20, 20);
            colorView.setLayoutParams(params);

            // Set circle background
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(Color.parseColor(colorHex));
            colorView.setBackground(shape);

            // Add border to currently selected color
            if (colorHex.equalsIgnoreCase(selectedColor)) {
                GradientDrawable selectedShape = new GradientDrawable();
                selectedShape.setShape(GradientDrawable.OVAL);
                selectedShape.setColor(Color.parseColor(colorHex));
                selectedShape.setStroke(10, Color.BLACK);
                colorView.setBackground(selectedShape);
            }

            colorViews[i] = colorView;
            gridLayout.addView(colorView);
        }

        colorLayout.addView(gridLayout);
        builder.setView(colorLayout);

        // Create the dialog AFTER setting the view on the builder
        final AlertDialog dialog = builder.create();

        // Now update the OnClickListener for each color view to use the dialog reference
        for (int i = 0; i < colorOptions.length; i++) {
            final String colorHex = "#" + colorOptions[i];
            colorViews[i].setOnClickListener(v -> {
                selectedColor = colorHex;
                updateColorIndicator(selectedColor); // This will update both views
                dialog.dismiss();
            });
        }

        // Show the dialog
        dialog.show();
    }

    private void updateColorIndicator(String colorHex) {
        if (colorSelectionView != null) {
            try {
                // Apply the color to just the circle view
                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.OVAL);
                shape.setColor(Color.parseColor(colorHex));
                colorSelectionView.setBackground(shape);
            } catch (Exception e) {
                Log.e("NotifCreation", "Error updating color: " + e.getMessage());
            }
        }
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

        // Save button with save to Shared Pref and save with proper formatting
        saveButton.setOnClickListener(v -> {
            // Create notification from UI fields
            NotifData.NotificationItem notification = createNotificationFromInput();

            if (notification == null) {
                Toast.makeText(this, "Notification creation failed. Missing required fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save it
            boolean success = notificationManager.addNotification(notification);

            if (success) {
                // Show confirmation
                Toast.makeText(this, "Notification saved", Toast.LENGTH_SHORT).show();

                // Cleanup clear fields
                clearForm();

                // Go back to notifications list
                Intent intent = new Intent(NotifCreationActivity.this, NotificationsActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to save notification. Please check your inputs.", Toast.LENGTH_SHORT).show();
            }
        });
    //    saveButton.setOnClickListener(v -> saveNotification());

        // Cancel button
        cancelButton.setOnClickListener(v -> finish());
    }

    private void clearForm() {
        nameEditText.setText("");
        messageEditText.setText("");
        selectedDaysOfMonth.clear();
        updateSelectedDaysText();
        selectAllDays(false);
        recurrenceTypeRadioGroup.clearCheck();
        weekdaysContainer.setVisibility(View.VISIBLE);
        monthlyContainer.setVisibility(View.GONE);
        updateTimeDisplay();
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

    //TODO: update the duration section here
    private NotifData.NotificationItem createNotificationFromInput() {
        String title = nameEditText.getText().toString().trim();
        String message = messageEditText.getText().toString().trim();
        String time = timeTextView.getText().toString();
        String duration = "na";
        String backgroundColor = getSelectedBackgroundColor(); // Method to get selected color
        String category = getSelectedCategory();

        if (title.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please fill in all required sections", Toast.LENGTH_SHORT).show();

        }

        // Determine notification type
        boolean isWeekly = recurrenceTypeRadioGroup.getCheckedRadioButtonId() == R.id.rb_weekday;
        String typeMonthOrWeek = isWeekly ? "Weekly" : "Monthly";

        // Get active days
        boolean[] activeDaysOfWeek = null;
        boolean[] activeDaysOfMonth = null;

        if (isWeekly) {
            activeDaysOfWeek = new boolean[7];
            for (int i = 0; i < 7; i++) {
                activeDaysOfWeek[i] = weekdayToggleButtons[i].isChecked();
            }
        } else {
            activeDaysOfMonth = new boolean[31];
            for (int day : selectedDaysOfMonth) {
                // Convert from 1-31 to 0-30 index
                if (day >= 1 && day <= 31) {
                    activeDaysOfMonth[day - 1] = true;
                }
            }
        }

        NotifData.NotificationItem item = createNotificationItem(title, message);


        // Pass data back to NotificationsActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("notification_id", item.getNotifID());
        resultIntent.putExtra("notification_name", title);
        resultIntent.putExtra("notification_time", time);
        resultIntent.putExtra("notification_background", selectedColor);
        resultIntent.putExtra("notification_type", item.getTypeMonthOrWeek()); // Add the type to the intent

        // You might want to serialize the entire item or pass each attribute separately
        setResult(RESULT_OK, resultIntent);

        return new NotifData.NotificationItem(
                title, time, duration, backgroundColor, category,
                typeMonthOrWeek, activeDaysOfWeek, activeDaysOfMonth
        );
    }

    private String getSelectedCategory() {
        // Get selected category based on your UI implementation
      //  if (goalSelectionView.isSelected()) return "Goal";
    //    if (medicationSelectionView.isSelected()) return "Medication";
        return "General"; // Default category
    }

    //TODO: setup background color selection

    // Helper method to get selected background color
    private String getSelectedBackgroundColor() {
        return selectedColor;
    }


    private NotifData.NotificationItem createNotificationItem(String name, String message) {
        // Format time
        String timeString = timeTextView.getText().toString();

        // Determine recurrence pattern
        String durationText = "dur";
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


        String category = "Activity";

        // Include the selected background color in the NotificationItem constructor
        return new NotifData.NotificationItem(name, timeString, durationText,
                                                selectedColor, category, notifTypeWeekOrMonth);

    }
}