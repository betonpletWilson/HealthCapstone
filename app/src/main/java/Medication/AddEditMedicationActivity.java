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

        // When medication name loses focus, fetch suggestions
        medicationEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                String rawName = medicationEditText.getText().toString().trim();
                if (!rawName.isEmpty()) {
                    ApiService.DBSearch(
                            "medication",
                            "name",
                            rawName,
                            Medication[].class,
                            new ApiCallback<List<Medication>>() {
                                @Override
                                public void onSuccess(List<Medication> meds) {
                                    runOnUiThread(() -> {
                                        if (!meds.isEmpty()) {
                                            showMedicationDialog(meds);
                                        }
                                    });
                                }
                                @Override
                                public void onFailure(String err) {
                                    // ignore failures silently
                                }
                            }
                    );
                }
            }
        });

        // Save button just commits whatever is in the fields
        saveButton.setOnClickListener(v -> saveMedication());
    }

    /**
     * Show a simple ListView dialog allowing the user to pick
     * one of the Medication suggestions.  On pick, we fill the
     * name field and leave the rest as the user entered.
     */
    private void showMedicationDialog(List<Medication> meds) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_medication_search, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        ListView lv = dialogView.findViewById(R.id.medSearchListView);
        List<String> names = new ArrayList<>();
        for (Medication m : meds) {
            names.add(m.getName());
        }
        ArrayAdapter<String> aa = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, names
        );
        lv.setAdapter(aa);

        lv.setOnItemClickListener((parent, view, pos, id) -> {
            dialog.dismiss();
            Medication picked = meds.get(pos);
            medicationEditText.setText(picked.getName());
            // instructions and frequency remain as the user entered
        });

        dialog.show();
    }

    /** Show Android’s time picker dialog to set reminderTimeCalendar */
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
        String name        = medicationEditText.getText().toString().trim();
        String instr       = instructionsEditText.getText().toString().trim();
        String freq        = frequencyEditText.getText().toString().trim();
        long   reminderMs  = dailyReminderCheckBox.isChecked()
                ? reminderTimeCalendar.getTimeInMillis()
                : 0L;

        Intent data = new Intent();
        data.putExtra("name",         name);
        data.putExtra("instructions", instr);
        data.putExtra("frequency",    freq);
        data.putExtra("reminderTime", reminderMs);
        data.putExtra("position",     editingPosition);
        setResult(RESULT_OK, data);
        finish();
    }
}
