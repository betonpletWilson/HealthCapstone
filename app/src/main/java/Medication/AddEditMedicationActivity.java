package Medication;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.fitwizard.fitwizard.R;

import java.util.Calendar;

public class AddEditMedicationActivity extends AppCompatActivity {

    private EditText medicationEditText;
    private Button pickTimeButton, saveButton;
    private CheckBox dailyReminderCheckBox;
    private Calendar reminderTimeCalendar;
    private int editingPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_medication);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        medicationEditText = findViewById(R.id.medicationEditText);
        pickTimeButton = findViewById(R.id.pickTimeButton);
        saveButton = findViewById(R.id.saveMedicationButton);
        dailyReminderCheckBox = findViewById(R.id.dailyReminderCheckBox);
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
        });


        reminderTimeCalendar = Calendar.getInstance();

        pickTimeButton.setOnClickListener(v -> showTimePicker());

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("name")) {
            medicationEditText.setText(intent.getStringExtra("name"));
            reminderTimeCalendar.setTimeInMillis(intent.getLongExtra("reminderTime", 0));
            editingPosition = intent.getIntExtra("position", -1);
            dailyReminderCheckBox.setChecked(true); // Assume if editing, reminder is set
        }

        saveButton.setOnClickListener(v -> saveMedication());
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (timePicker, hourOfDay, minute) -> {
            reminderTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            reminderTimeCalendar.set(Calendar.MINUTE, minute);
            reminderTimeCalendar.set(Calendar.SECOND, 0);
            Toast.makeText(this, "Reminder set to: " +
                    String.format("%02d:%02d", hourOfDay, minute), Toast.LENGTH_SHORT).show();
        }, reminderTimeCalendar.get(Calendar.HOUR_OF_DAY), reminderTimeCalendar.get(Calendar.MINUTE), false).show();
    }

    private void saveMedication() {
        String name = medicationEditText.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter medication name", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent data = new Intent();
        data.putExtra("name", name);
        data.putExtra("reminderTime", dailyReminderCheckBox.isChecked() ? reminderTimeCalendar.getTimeInMillis() : 0);
        data.putExtra("position", editingPosition);
        setResult(RESULT_OK, data);
        finish();
    }
}
