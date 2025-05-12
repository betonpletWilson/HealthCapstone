package Exercise_Logging;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitwizard.fitwizard.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ExerciseHistoryActivity extends AppCompatActivity {

    private static final int REQUEST_LOG_EXERCISE = 101;
    private static final String PREFS_NAME      = "ExercisePrefs";
    private static final String EXERCISE_KEY    = "exercise_logs";

    private ArrayList<ExerciseEntry> exerciseList;
    private ExerciseAdapter adapter;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_history);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        exerciseList = loadExerciseLogs();

        RecyclerView recyclerView = findViewById(R.id.exerciseRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ExerciseAdapter(this, exerciseList);
        recyclerView.setAdapter(adapter);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        FloatingActionButton addExerciseButton = findViewById(R.id.addExerciseButton);
        addExerciseButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExerciseLogActivity.class);
            startActivityForResult(intent, REQUEST_LOG_EXERCISE);
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ExerciseEntry entry = exerciseList.get(position);

                new AlertDialog.Builder(ExerciseHistoryActivity.this)
                        .setTitle("Delete Exercise")
                        .setMessage("Delete this entry?\n\n" + entry.getName())
                        .setPositiveButton("Delete", (dialog, which) -> {
                            exerciseList.remove(position);
                            adapter.notifyItemRemoved(position);
                            saveExerciseLogs();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> adapter.notifyItemChanged(position))
                        .setCancelable(false)
                        .show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    private ArrayList<ExerciseEntry> loadExerciseLogs() {
        Set<String> savedSet = prefs.getStringSet(EXERCISE_KEY, new HashSet<>());
        ArrayList<ExerciseEntry> list = new ArrayList<>();
        for (String entry : savedSet) {
            list.add(ExerciseEntry.fromPrefString(entry));
        }
        return list;
    }

    private void saveExerciseLogs() {
        Set<String> set = new HashSet<>();
        for (ExerciseEntry entry : exerciseList) {
            set.add(entry.toPrefString());
        }
        prefs.edit().putStringSet(EXERCISE_KEY, set).apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOG_EXERCISE
                && resultCode == RESULT_OK
                && data != null) {

            // ◀◀◀ REPLACED the old name/details/date logic with the single pref-string
            String pref = data.getStringExtra("exercise_pref");
            if (pref != null) {
                ExerciseEntry newEntry = ExerciseEntry.fromPrefString(pref);
                exerciseList.add(0, newEntry);
                adapter.notifyItemInserted(0);
                saveExerciseLogs();
            }
        }
    }
}
