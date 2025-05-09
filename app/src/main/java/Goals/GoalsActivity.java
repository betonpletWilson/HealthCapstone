package Goals;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitwizard.fitwizard.HomeActivity;
import com.fitwizard.fitwizard.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GoalsActivity extends AppCompatActivity implements GoalAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private GoalAdapter goalAdapter;
    private ArrayList<Goal> goalList;
    private SharedPreferences prefs;

    private static final int ADD_GOAL_REQUEST = 1;
    private static final int EDIT_GOAL_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Back Button setup
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(GoalsActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        recyclerView = findViewById(R.id.goalsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        prefs = getSharedPreferences("GoalPrefs", MODE_PRIVATE);

        goalList = loadGoals();
        goalAdapter = new GoalAdapter(this, goalList);
        recyclerView.setAdapter(goalAdapter);

        findViewById(R.id.addGoalButton).setOnClickListener(v -> {
            Intent intent = new Intent(GoalsActivity.this, AddEditGoalActivity.class);
            startActivityForResult(intent, ADD_GOAL_REQUEST);
        });

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }

    private ArrayList<Goal> loadGoals() {
        ArrayList<Goal> list = new ArrayList<>();
        Set<String> set = prefs.getStringSet("goals_list", new HashSet<>());
        for (String item : set) {
            list.add(Goal.fromString(item));
        }
        return list;
    }

    private void saveGoals() {
        Set<String> set = new HashSet<>();
        for (Goal goal : goalList) {
            set.add(goal.toString());
        }
        prefs.edit().putStringSet("goals_list", set).apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            String goalName = data.getStringExtra("goal");
            long deadline = data.getLongExtra("deadline", 0);
            boolean reminder = data.getBooleanExtra("reminder", false);
            long reminderTime = data.getLongExtra("reminderTime", 0);

            if (requestCode == ADD_GOAL_REQUEST) {
                goalList.add(new Goal(goalName, deadline, reminder, reminderTime));
                goalAdapter.notifyItemInserted(goalList.size() - 1);
            } else if (requestCode == EDIT_GOAL_REQUEST) {
                int position = data.getIntExtra("position", -1);
                if (position != -1) {
                    goalList.set(position, new Goal(goalName, deadline, reminder, reminderTime));
                    goalAdapter.notifyItemChanged(position);
                }
            }

            saveGoals();
        }
    }

    private final ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            showDeleteConfirmationDialog(position);
        }
    };

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete this goal?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    goalList.remove(position);
                    goalAdapter.notifyItemRemoved(position);
                    saveGoals();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    goalAdapter.notifyItemChanged(position); // reset swipe if canceled
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onEditClick(int position) {
        Intent intent = new Intent(this, AddEditGoalActivity.class);
        Goal goal = goalList.get(position);
        intent.putExtra("goal", goal.getName());
        intent.putExtra("deadline", goal.getDeadlineMillis());
        intent.putExtra("reminder", goal.isDailyReminder());
        intent.putExtra("reminderTime", goal.getReminderTimeMillis());
        intent.putExtra("position", position);
        startActivityForResult(intent, EDIT_GOAL_REQUEST);
    }
}
