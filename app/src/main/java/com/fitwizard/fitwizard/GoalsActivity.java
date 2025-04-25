package com.fitwizard.fitwizard;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Set;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

public class GoalsActivity extends AppCompatActivity {

    private EditText goalInput;
    private TextView deadlineText, goalSummaryText;
    private CheckBox dailyReminderCheckbox;
    private Button pickTimeBtn;
    private Calendar selectedDeadline;
    private Calendar selectedReminderTime;
    private SharedPreferences prefs;

    private ListView goalsListView;
    private ArrayAdapter<String> goalsAdapter;
    private ArrayList<String> savedGoals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        prefs = getSharedPreferences("GoalPrefs", MODE_PRIVATE);

        goalInput = findViewById(R.id.editTextGoal);
        deadlineText = findViewById(R.id.textGoalDeadline);
        pickTimeBtn = findViewById(R.id.buttonPickTime);
        dailyReminderCheckbox = findViewById(R.id.checkBoxDailyReminder);
        Button pickDateBtn = findViewById(R.id.buttonPickDate);
        Button saveBtn = findViewById(R.id.buttonSaveGoal);
        goalSummaryText = findViewById(R.id.goalSummaryText);

        selectedDeadline = Calendar.getInstance();
        selectedReminderTime = Calendar.getInstance();

        savedGoals = new ArrayList<>();
        goalsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, savedGoals);
        goalsListView = findViewById(R.id.goalsListView);
        goalsListView.setAdapter(goalsAdapter);

        pickDateBtn.setOnClickListener(v -> showDatePicker());
        pickTimeBtn.setOnClickListener(v -> showTimePicker());

        saveBtn.setOnClickListener(v -> saveGoal());

        if (getIntent().getBooleanExtra("reset_goal", false)) {
            goalInput.setText("Set your goal here...");
            deadlineText.setText("Pick a deadline");
            dailyReminderCheckbox.setChecked(false);
        }

        loadSavedGoal();
        loadSavedGoals();
    }

    private void saveGoal() {
        String goal = goalInput.getText().toString().trim();
        if (goal.isEmpty()) {
            Toast.makeText(this, "Please enter a goal", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add goal to saved list
        savedGoals.add(goal);
        saveGoalsToPrefs();
        goalsAdapter.notifyDataSetChanged();

        // Save to SharedPreferences
        prefs.edit()
                .putString("goal_text", goal)
                .putLong("goal_deadline", selectedDeadline.getTimeInMillis())
                .putBoolean("daily_reminder", dailyReminderCheckbox.isChecked())
                .putLong("reminder_time", selectedReminderTime.getTimeInMillis())
                .apply();

        if (dailyReminderCheckbox.isChecked()) {
            setDailyReminder();
        }

        Toast.makeText(this, "Goal saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void loadSavedGoal() {
        String savedGoal = prefs.getString("goal_text", "");
        long deadline = prefs.getLong("goal_deadline", 0);
        boolean reminder = prefs.getBoolean("daily_reminder", false);

        if (!savedGoal.isEmpty()) {
            goalInput.setText(savedGoal);
            deadlineText.setText("Deadline: " + android.text.format.DateFormat.format("MMM dd, yyyy", deadline));
            dailyReminderCheckbox.setChecked(reminder);

            String summary = "Current Goal: " + savedGoal + "\nDeadline: " +
                    android.text.format.DateFormat.format("MMM dd, yyyy", deadline) +
                    (reminder ? "\nReminder set." : "\nNo Reminder");

            goalSummaryText.setVisibility(View.VISIBLE);
            goalSummaryText.setText(summary);
        }
    }

    private void loadSavedGoals() {
        Set<String> goalsSet = prefs.getStringSet("goals_list", new HashSet<>());
        savedGoals.clear();
        savedGoals.addAll(goalsSet);
        goalsAdapter.notifyDataSetChanged();

        goalsListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedGoal = savedGoals.get(position);
            showEditDialog(selectedGoal, position);
        });
    }

    private void saveGoalsToPrefs() {
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> updatedSet = new HashSet<>(savedGoals);
        editor.putStringSet("goals_list", updatedSet);
        editor.apply();
    }

    private void showEditDialog(String goal, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit or Delete Goal");

        final EditText input = new EditText(this);
        input.setText(goal);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newGoal = input.getText().toString().trim();
            if (!newGoal.isEmpty()) {
                savedGoals.set(position, newGoal);
                saveGoalsToPrefs();
                goalsAdapter.notifyDataSetChanged();
            }
        });

        builder.setNeutralButton("Delete", (dialog, which) -> {
            savedGoals.remove(position);
            saveGoalsToPrefs();
            goalsAdapter.notifyDataSetChanged();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDatePicker() {
        final Calendar now = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDeadline.set(year, month, dayOfMonth);
                    deadlineText.setText("Deadline: " + android.text.format.DateFormat.format("MMM dd, yyyy", selectedDeadline));
                },
                now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void showTimePicker() {
        int hour = selectedReminderTime.get(Calendar.HOUR_OF_DAY);
        int minute = selectedReminderTime.get(Calendar.MINUTE);

        new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            selectedReminderTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedReminderTime.set(Calendar.MINUTE, minute1);
            Toast.makeText(this, "Reminder time set to: " + hourOfDay + ":" + String.format("%02d", minute1), Toast.LENGTH_SHORT).show();
        }, hour, minute, false).show();
    }

    private void setDailyReminder() {
        Intent intent = new Intent(this, GoalReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        selectedReminderTime.set(Calendar.SECOND, 0);

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                selectedReminderTime.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }
}
