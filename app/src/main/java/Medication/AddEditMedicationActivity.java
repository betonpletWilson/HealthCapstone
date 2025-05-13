package Medication;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fitwizard.fitwizard.R;
import com.fitwizard.fitwizard.network.ApiService;
import com.fitwizard.fitwizard.network.ApiService.ApiCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddEditMedicationActivity extends AppCompatActivity {

    private EditText medicationEditText;
    private EditText instructionsEditText;
    private EditText frequencyEditText;
    private Button pickTimeButton;
    private Button saveButton;
    private CheckBox dailyReminderCheckBox;
    private Calendar reminderTimeCalendar;
    private int editingPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_medication);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Bind views
        medicationEditText    = findViewById(R.id.medicationEditText);
        instructionsEditText  = findViewById(R.id.instructionsEditText);
        frequencyEditText     = findViewById(R.id.frequencyEditText);
        pickTimeButton        = findViewById(R.id.pickTimeButton);
        saveButton            = findViewById(R.id.saveMedicationButton);
        dailyReminderCheckBox = findViewById(R.id.dailyReminderCheckBox);
        ImageButton backBtn   = findViewById(R.id.backButton);

        // Back button closes
        backBtn.setOnClickListener(v -> finish());

        // Time picker
        reminderTimeCalendar = Calendar.getInstance();
        pickTimeButton.setOnClickListener(v -> showTimePicker());

        // If we're editing an existing entry, prefill the fields
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("name")) {
            medicationEditText.setText(intent.getStringExtra("name"));
            instructionsEditText.setText(intent.getStringExtra("instructions"));
            frequencyEditText.setText(intent.getStringExtra("frequency"));
            reminderTimeCalendar.setTimeInMillis(
                    intent.getLongExtra("reminderTime", 0L)
            );
            dailyReminderCheckBox.setChecked(true);
            editingPosition = intent.getIntExtra("position", -1);
        }

        // Save button just commits whatever is in the fields
        saveButton.setOnClickListener(v -> saveMedication());
    }

    private void showTimePicker() {
        new TimePickerDialog(
                this,
                (tp, hour, minute) -> {
                    reminderTimeCalendar.set(Calendar.HOUR_OF_DAY, hour);
                    reminderTimeCalendar.set(Calendar.MINUTE, minute);
                    reminderTimeCalendar.set(Calendar.SECOND, 0);
                    Toast.makeText(this,
                            String.format("Reminder set to %02d:%02d", hour, minute),
                            Toast.LENGTH_SHORT).show();
                },
                reminderTimeCalendar.get(Calendar.HOUR_OF_DAY),
                reminderTimeCalendar.get(Calendar.MINUTE),
                false
        ).show();
    }

    /**
     * Gather all field values into an Intent result and finish.
     * Name, instructions, frequency, reminderTime, and editingPosition.
     */
    private void saveMedication() {
        String name  = medicationEditText.getText().toString().trim();
        String instr = instructionsEditText.getText().toString().trim();
        String freq  = frequencyEditText.getText().toString().trim();
        long reminderMs = dailyReminderCheckBox.isChecked()
                ? reminderTimeCalendar.getTimeInMillis()
                : 0L;

        int userId =
                getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        .getInt("userId", -1);

        ApiService.addMedication(userId, name, instr, freq, reminderMs,
                new ApiService.ApiCallback<Void>() {
                    @Override public void onSuccess(Void v) {
                        runOnUiThread(() -> {
                            setResult(RESULT_OK);
                            finish();
                        });
                    }
                    @Override public void onFailure(String err) {
                        runOnUiThread(() ->
                                Toast.makeText(
                                        AddEditMedicationActivity.this,
                                        "Error: " + err,
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    }
                }
        );
    }
}
