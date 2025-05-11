package Exercise_Logging;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitwizard.fitwizard.R;

import java.util.Calendar;

public class ExerciseLogActivity extends AppCompatActivity {

    private EditText nameInput, setsRepsInput, durationInput;
    private RadioGroup typeGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_log);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        nameInput = findViewById(R.id.exerciseNameInput);
        setsRepsInput = findViewById(R.id.setsRepsInput);
        durationInput = findViewById(R.id.durationInput);
        typeGroup = findViewById(R.id.typeRadioGroup);
        Button saveBtn = findViewById(R.id.saveExerciseButton);
        ImageButton backBtn = findViewById(R.id.backButton);

        typeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.strengthRadio) {
                setsRepsInput.setVisibility(View.VISIBLE);
                durationInput.setVisibility(View.GONE);
            } else if (checkedId == R.id.cardioRadio) {
                setsRepsInput.setVisibility(View.GONE);
                durationInput.setVisibility(View.VISIBLE);
            }
        });

        saveBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String details = "";
            String date = DateFormat.format("MMM dd, yyyy", Calendar.getInstance()).toString();

            if (typeGroup.getCheckedRadioButtonId() == R.id.strengthRadio) {
                details = setsRepsInput.getText().toString().trim();
            } else if (typeGroup.getCheckedRadioButtonId() == R.id.cardioRadio) {
                details = durationInput.getText().toString().trim();
            }

            if (name.isEmpty() || details.isEmpty()) {
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Send back data
            Intent result = new Intent();
            result.putExtra("exercise_name", name);
            result.putExtra("exercise_details", details);
            result.putExtra("exercise_date", date);
            setResult(RESULT_OK, result);
            finish();
        });

        backBtn.setOnClickListener(v -> {
            finish(); // just go back
        });
    }
}
