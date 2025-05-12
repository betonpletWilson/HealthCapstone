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
import java.util.List;

public class MedicationAdapter
        extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {

    public interface OnItemClickListener { void onEditClick(int pos); }

    private final List<Medication>      medicationList;
    private final OnItemClickListener   listener;
    private final Context               context;
    private final int[]                 colors = {
            R.color.light_blue,
            R.color.light_green,
            R.color.light_yellow,
            R.color.light_purple,
            R.color.light_orange
    };

    public MedicationAdapter(
            Context ctx,
            List<Medication> meds,
            OnItemClickListener l
    ) {
        this.context       = ctx;
        this.medicationList= meds;
        this.listener      = l;
    }

    @Override
    public MedicationViewHolder onCreateViewHolder(
            ViewGroup parent, int viewType
    ) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MedicationViewHolder holder, int pos) {
        Medication med = medicationList.get(pos);
        holder.medicationTextView.setText(med.getName());
        holder.instructionsTextView.setText(med.getInstructions());
        holder.frequencyTextView.setText(med.getFrequency());
        holder.reminderTextView.setText(
                "Reminder: " +
                        DateFormat.format("hh:mm a", med.getReminderTimeMillis())
        );

        int color = colors[pos % colors.length];
        holder.cardView.setCardBackgroundColor(
                context.getResources().getColor(color)
        );

        holder.editButton.setOnClickListener(_v ->
                listener.onEditClick(holder.getAdapterPosition())
        );
    }

    @Override public int getItemCount() { return medicationList.size(); }

    static class MedicationViewHolder extends RecyclerView.ViewHolder {
        CardView    cardView;
        TextView    medicationTextView,
                instructionsTextView,
                frequencyTextView,
                reminderTextView;
        ImageButton editButton;

        MedicationViewHolder(View itemView) {
            super(itemView);
            cardView               = itemView.findViewById(R.id.cardView);
            medicationTextView     = itemView.findViewById(R.id.medicationTextView);
            instructionsTextView   = itemView.findViewById(R.id.instructionsTextView);
            frequencyTextView      = itemView.findViewById(R.id.frequencyTextView);
            reminderTextView       = itemView.findViewById(R.id.reminderTimeTextView);
            editButton             = itemView.findViewById(R.id.editMedicationButton);
        }
    }
}
