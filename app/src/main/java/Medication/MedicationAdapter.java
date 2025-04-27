package Medication;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.fitwizard.fitwizard.R;

import java.util.ArrayList;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {

    private ArrayList<Medication> medicationList;
    private OnItemClickListener listener;
    private final Context context;

    private int[] colors = {
            R.color.light_blue,
            R.color.light_green,
            R.color.light_yellow,
            R.color.light_purple,
            R.color.light_orange
    };

    public interface OnItemClickListener {
        void onEditClick(int position);
    }

    public MedicationAdapter(Context context, ArrayList<Medication> medicationList, OnItemClickListener listener) {
        this.context = context;
        this.medicationList = medicationList;
        this.listener = listener;
    }

    @Override
    public MedicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MedicationViewHolder holder, int position) {
        Medication medication = medicationList.get(position);
        holder.medicationTextView.setText(medication.getName());
        holder.reminderTextView.setText(
                "Reminder: " + DateFormat.format("hh:mm a", medication.getReminderTimeMillis())
        );

        int randomColor = colors[position % colors.length]; // cycle instead of random so it doesn't change after scrolling
        holder.cardView.setCardBackgroundColor(context.getResources().getColor(randomColor));

        holder.editButton.setOnClickListener(v -> listener.onEditClick(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    public static class MedicationViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView medicationTextView, reminderTextView;
        ImageButton editButton;

        public MedicationViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            medicationTextView = itemView.findViewById(R.id.medicationTextView);
            reminderTextView = itemView.findViewById(R.id.reminderTimeTextView);
            editButton = itemView.findViewById(R.id.editMedicationButton);
        }
    }
}
