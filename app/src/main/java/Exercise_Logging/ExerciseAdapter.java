package Exercise_Logging;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.fitwizard.fitwizard.R;

import java.util.ArrayList;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private Context context;
    private ArrayList<ExerciseEntry> exerciseLogs;

    private int[] pastelColors = {
            R.color.light_blue,
            R.color.light_green,
            R.color.light_orange,
            R.color.light_purple,
            R.color.light_yellow
    };

    public ExerciseAdapter(Context context, ArrayList<ExerciseEntry> exerciseLogs) {
        this.context = context;
        this.exerciseLogs = (exerciseLogs != null) ? exerciseLogs : new ArrayList<>();
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        ExerciseEntry log = exerciseLogs.get(position);
        holder.exerciseNameTextView.setText(log.getName());
        holder.exerciseDetailsTextView.setText(log.getDetails());
        holder.exerciseDateTextView.setText("Date: " + log.getDate());

        // ➜ new binding for calories
        holder.exerciseCaloriesTextView.setText(
                String.format("%.1f kcal", log.getCalories())
        );

        int color = pastelColors[position % pastelColors.length];
        holder.cardView.setCardBackgroundColor(
                context.getResources().getColor(color)
        );
        holder.cardView.setCardBackgroundColor(context.getResources().getColor(color));
    }

    @Override
    public int getItemCount() {
        return exerciseLogs.size();
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView exerciseNameTextView,
                exerciseDetailsTextView,
            exerciseCaloriesTextView,   // new view
        exerciseDateTextView;
        CardView cardView;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView                 = itemView.findViewById(R.id.cardView);
            exerciseNameTextView     = itemView.findViewById(R.id.exerciseNameTextView);
            exerciseDetailsTextView  = itemView.findViewById(R.id.exerciseDetailsTextView);
            exerciseCaloriesTextView = itemView.findViewById(R.id.exerciseCaloriesTextView);
            exerciseDateTextView     = itemView.findViewById(R.id.exerciseDateTextView);
        }
    }
}
