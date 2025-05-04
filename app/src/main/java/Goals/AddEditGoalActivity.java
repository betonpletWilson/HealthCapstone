package Goals;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.fitwizard.fitwizard.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditGoalActivity extends AppCompatActivity {

    private EditText editTextGoalName;
    private TextView goalScreenTitle;
    private Button buttonPickDeadline, buttonPickReminderTime, buttonSaveGoal;
    private CheckBox checkBoxDailyReminder;
    private ImageButton backButton;

    private Calendar selectedDeadline;
    private Calendar selectedReminderTime;

    private boolean isEditing = false;
    private int editingPosition = -1; // position of goal being edited, if any

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_goal);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        editTextGoalName = findViewById(R.id.editTextGoalName);
        buttonPickDeadline = findViewById(R.id.buttonPickDeadline);
        buttonPickReminderTime = findViewById(R.id.buttonPickReminderTime);
        buttonSaveGoal = findViewById(R.id.buttonSaveGoal);
        checkBoxDailyReminder = findViewById(R.id.checkBoxDailyReminder);
        goalScreenTitle = findViewById(R.id.goalScreenTitle);
        backButton = findViewById(R.id.backButton);

        selectedDeadline = Calendar.getInstance();
        selectedReminderTime = Calendar.getInstance();

        // Check if editing existing goal
        Intent intent = getIntent();
        if (intent.hasExtra("goal")) {
            isEditing = true;
            String goalName = intent.getStringExtra("goal");
            long deadlineMillis = intent.getLongExtra("deadline", 0);
            boolean reminder = intent.getBooleanExtra("reminder", false);
            long reminderMillis = intent.getLongExtra("reminderTime", 0);
            editingPosition = intent.getIntExtra("position", -1);

            editTextGoalName.setText(goalName);
            if (deadlineMillis > 0) {
                selectedDeadline.setTimeInMillis(deadlineMillis);
                buttonPickDeadline.setText("Deadline: " + formatDate(selectedDeadline));
            }
            checkBoxDailyReminder.setChecked(reminder);
            if (reminderMillis > 0) {
                selectedReminderTime.setTimeInMillis(reminderMillis);
                buttonPickReminderTime.setText("Reminder: " + formatTime(selectedReminderTime));
            }

            goalScreenTitle.setText("Edit Goal");
            buttonSaveGoal.setText("Update Goal");
        }

        buttonPickDeadline.setOnClickListener(v -> showDatePickerDialog());
        buttonPickReminderTime.setOnClickListener(v -> showTimePickerDialog());

        buttonSaveGoal.setOnClickListener(v -> saveGoal());

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, GoalsActivity.class));
            finish();
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDeadline.set(Calendar.YEAR, year);
                    selectedDeadline.set(Calendar.MONTH, month);
                    selectedDeadline.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    buttonPickDeadline.setText("Deadline: " + formatDate(selectedDeadline));
                },
                selectedDeadline.get(Calendar.YEAR),
                selectedDeadline.get(Calendar.MONTH),
                selectedDeadline.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedReminderTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedReminderTime.set(Calendar.MINUTE, minute);
                    selectedReminderTime.set(Calendar.SECOND, 0);
                    buttonPickReminderTime.setText("Reminder: " + formatTime(selectedReminderTime));
                },
                selectedReminderTime.get(Calendar.HOUR_OF_DAY),
                selectedReminderTime.get(Calendar.MINUTE),
                false);
        timePickerDialog.show();
    }

    private void saveGoal() {
        String goalName = editTextGoalName.getText().toString().trim();
        if (goalName.isEmpty()) {
            Toast.makeText(this, "Please enter a goal name.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent data = new Intent();
        data.putExtra("goal", goalName);
        data.putExtra("deadline", selectedDeadline.getTimeInMillis());
        data.putExtra("reminder", checkBoxDailyReminder.isChecked());
        data.putExtra("reminderTime", selectedReminderTime.getTimeInMillis());
        if (isEditing) {
            data.putExtra("position", editingPosition);
        }

        setResult(RESULT_OK, data);
        finish();
    }

    private String formatDate(Calendar calendar) {
        return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(calendar.getTime());
    }

    private String formatTime(Calendar calendar) {
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(calendar.getTime());
    }
}
