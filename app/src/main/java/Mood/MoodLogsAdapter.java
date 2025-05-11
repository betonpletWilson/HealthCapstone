package Mood;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fitwizard.fitwizard.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

// Adapter for mood logs
class MoodLogsAdapter extends RecyclerView.Adapter<MoodLogsAdapter.MoodLogViewHolder> {

    private Context context;
    private List<MoodData> entries;
    private SimpleDateFormat dateFormat;

    public MoodLogsAdapter(Context context, List<MoodData> entries) {
        this.context = context;
        this.entries = entries;
        this.dateFormat = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public MoodLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mood_log, parent, false);
        return new MoodLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodLogViewHolder holder, int position) {
        MoodData entry = entries.get(position);

        // Set mood emoji
        if (entry.getMoodResourceId() != -1) {
            holder.moodImageView.setImageResource(entry.getMoodResourceId());
        }

        // Set date
        if (entry.getDate() != null) {
            holder.dateTextView.setText(dateFormat.format(entry.getDate()));
        }

        // Set content preview (first 50 chars)
        String content = entry.getContent();
        if (content != null && !content.isEmpty()) {
            if (content.length() > 50) {
                content = content.substring(0, 47) + "...";
            }
            holder.contentPreviewTextView.setText(content);
        }

        // Set tags
        StringBuilder tagText = new StringBuilder();
        List<MoodData> tags = entry.getSelectedTags();
        if (tags != null && !tags.isEmpty()) {
            for (int i = 0; i < tags.size(); i++) {
                tagText.append(tags.get(i).getTagName());
                if (i < tags.size() - 1) {
                    tagText.append(", ");
                }
            }
        }
        holder.tagsTextView.setText(tagText.toString());

        // Set click listener to view full entry
        //todo: show the full entry on click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MoodEntryDetailActivity.class);
            intent.putExtra("ENTRY_DATE", dateFormat.format(entry.getDate()));
          //  startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    class MoodLogViewHolder extends RecyclerView.ViewHolder {
        ImageView moodImageView;
        TextView dateTextView;
        TextView contentPreviewTextView;
        TextView tagsTextView;

        public MoodLogViewHolder(@NonNull View itemView) {
            super(itemView);
            moodImageView = itemView.findViewById(R.id.logMoodImageView);
            dateTextView = itemView.findViewById(R.id.logDateTextView);
            contentPreviewTextView = itemView.findViewById(R.id.logContentPreviewTextView);
            tagsTextView = itemView.findViewById(R.id.logTagsTextView);
        }
    }
}