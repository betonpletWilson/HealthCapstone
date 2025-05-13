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

import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fitwizard.fitwizard.R;
import com.fitwizard.fitwizard.network.ApiService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExerciseLogActivity extends AppCompatActivity {

    private EditText nameInput, durationInput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_log);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        nameInput     = findViewById(R.id.exerciseNameInput);
        durationInput = findViewById(R.id.durationInput);

        Button saveBtn     = findViewById(R.id.saveExerciseButton);
        ImageButton backBtn = findViewById(R.id.backButton);

        saveBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String durS = durationInput.getText().toString().trim();
            String date = DateFormat.format(
                    "MMM dd, yyyy",
                    Calendar.getInstance()).toString();

            if (name.isEmpty() || durS.isEmpty()) {
                Toast.makeText(this,
                        "Please enter name and duration",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            float durationMin;
            try {
                durationMin = Float.parseFloat(durS);
            } catch (NumberFormatException e) {
                Toast.makeText(this,
                        "Enter a valid number for duration",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService.DBSearch(
                    "workout",
                    "name",
                    name,
                    ExerciseEntry[].class,
                    new ApiService.ApiCallback<List<ExerciseEntry>>() {
                        @Override public void onSuccess(List<ExerciseEntry> list) {
                            runOnUiThread(() -> {
                                if (list.isEmpty()) {
                                    // no matches → zero calories
                                    postEntry(name, durationMin, date, 0f);
                                } else {
                                    // show dialog of choices
                                    showWorkoutDialog(name, durationMin, date, list);
                                }
                            });
                        }
                        @Override public void onFailure(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(
                                        ExerciseLogActivity.this,
                                        "No workout found: " + error,
                                        Toast.LENGTH_SHORT).show();
                                postEntry(name, durationMin, date, 0f);
                            });
                        }
                    }
            );
        });

        backBtn.setOnClickListener(v -> finish());
    }

    private void showWorkoutDialog(
            String name,
            float durationMin,
            String date,
            List<ExerciseEntry> workouts
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_exercise_search, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        ListView listV = dialogView.findViewById(R.id.workoutListView);
        List<String> names = new ArrayList<>();
        for (ExerciseEntry e : workouts) {
            names.add(e.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, names);
        listV.setAdapter(adapter);

        listV.setOnItemClickListener((parent, view, pos, id) -> {
            dialog.dismiss();
            ExerciseEntry picked = workouts.get(pos);

            // avg calories/hour is stored in picked.getCalories()
            float burned = picked.getCalories() * (durationMin / 60f);
            postEntry(name, durationMin, date, burned);
        });

        dialog.show();
    }

    private void postEntry(
            String name,
            float durationMin,
            String date,
            float caloriesBurned
    ) {
        ExerciseEntry entry = new ExerciseEntry(
                name,
                durationMin + " min",
                date,
                caloriesBurned
        );
        Intent result = new Intent();
        result.putExtra("exercise_pref", entry.toPrefString());
        setResult(RESULT_OK, result);
        finish();
    }
}
