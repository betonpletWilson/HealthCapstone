package Goals;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.fitwizard.fitwizard.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    private Context context;
    private ArrayList<Goal> goalList;
    private SharedPreferences prefs;

    public GoalAdapter(Context context, ArrayList<Goal> goalList) {
        this.context = context;
        this.goalList = goalList;
        prefs = context.getSharedPreferences("GoalPrefs", Context.MODE_PRIVATE);
    }

    @Override
    public GoalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_goal, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GoalViewHolder holder, int position) {
        Goal goal = goalList.get(position);

        holder.goalTextView.setText(goal.getName()); // fix here
        holder.deadlineTextView.setText("Deadline: " + DateFormat.format("MMM dd, yyyy", goal.getDeadlineMillis())); // fix here
        holder.reminderTextView.setText(goal.isDailyReminder() ? "Reminder set: " + DateFormat.format("hh:mm a", goal.getReminderTimeMillis()) : "No reminder"); // fix here

        holder.editButton.setOnClickListener(v -> showEditGoalDialog(goal, position));
    }



    @Override
    public int getItemCount() {
        return goalList.size();
    }

    public void deleteGoal(int position) {
        goalList.remove(position);
        notifyItemRemoved(position);
        saveGoalsToPrefs();
    }

    private void showEditGoalDialog(Goal goal, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_goal, null);
        EditText goalEditText = view.findViewById(R.id.dialogGoalEditText);
        Button pickReminderTimeButton = view.findViewById(R.id.dialogPickTimeButton);
        TextView reminderTimeText = view.findViewById(R.id.dialogReminderTimeText);

        goalEditText.setText(goal.getName());
        Calendar reminderTime = Calendar.getInstance();
        reminderTime.setTimeInMillis(goal.getReminderTimeMillis());

        reminderTimeText.setText(DateFormat.format("hh:mm a", reminderTime));

        pickReminderTimeButton.setOnClickListener(v -> {
            new TimePickerDialog(context, (timePicker, hour, minute) -> {
                reminderTime.set(Calendar.HOUR_OF_DAY, hour);
                reminderTime.set(Calendar.MINUTE, minute);
                goal.setReminderTimeMillis(reminderTime.getTimeInMillis());
                reminderTimeText.setText(DateFormat.format("hh:mm a", reminderTime));
            }, reminderTime.get(Calendar.HOUR_OF_DAY), reminderTime.get(Calendar.MINUTE), false).show();
        });

        new AlertDialog.Builder(context)
                .setTitle("Edit Goal")
                .setView(view)
                .setPositiveButton("Save", (dialogInterface, i) -> {
                    goal.setName(goalEditText.getText().toString());
                    saveGoalsToPrefs();
                    notifyItemChanged(position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveGoalsToPrefs() {
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> goalSet = new HashSet<>();
        for (Goal g : goalList) {
            goalSet.add(g.toString());
        }
        editor.putStringSet("goals_list", goalSet);
        editor.apply();
    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView goalTextView, deadlineTextView, reminderTextView;
        ImageButton editButton;

        public GoalViewHolder(View itemView) {
            super(itemView);
            goalTextView = itemView.findViewById(R.id.goalTextView);
            deadlineTextView = itemView.findViewById(R.id.deadlineTextView);
            reminderTextView = itemView.findViewById(R.id.reminderTextView);
            editButton = itemView.findViewById(R.id.editGoalButton);
        }
    }


    public interface OnItemClickListener {
        void onEditClick(int position);
    }
    private OnItemClickListener listener;

    public GoalAdapter(ArrayList<Goal> goalList, OnItemClickListener listener) {
        this.goalList = goalList;
        this.listener = listener;
    }


}
