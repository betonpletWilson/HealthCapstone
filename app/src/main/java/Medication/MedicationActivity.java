package Medication;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitwizard.fitwizard.R;
import com.fitwizard.fitwizard.network.ApiService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MedicationActivity extends AppCompatActivity implements MedicationAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private MedicationAdapter adapter;
    private ArrayList<Medication> medicationList;
    private SharedPreferences prefs;

    private static final int ADD_MEDICATION_REQUEST = 100;
    private static final int EDIT_MEDICATION_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication); // singular name!

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        recyclerView = findViewById(R.id.medicationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        prefs = getSharedPreferences("MedicationPrefs", MODE_PRIVATE);
        medicationList = new ArrayList<>();
        adapter = new MedicationAdapter(this, medicationList, this);
        recyclerView.setAdapter(adapter);

        loadMedications();

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // Cleaner back behavior

        findViewById(R.id.addMedicationButton).setOnClickListener(v -> {
            Intent intent = new Intent(MedicationActivity.this, AddEditMedicationActivity.class);
            startActivityForResult(intent, ADD_MEDICATION_REQUEST);
        });

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }


    private void loadMedications() {
        int userId = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getInt("userId", -1);
        ApiService.getMedications(userId, new ApiService.ApiCallback<List<Medication>>() {
            @Override public void onSuccess(List<Medication> meds) {
                runOnUiThread(() -> {
                    medicationList.clear();
                    medicationList.addAll(meds);
                    adapter.notifyDataSetChanged();    // tell the RecyclerView there’s new data
                });
            }
            @Override public void onFailure(String err) {
                runOnUiThread(() ->
                        Toast.makeText(MedicationActivity.this,
                                "Error loading meds: " + err,
                                Toast.LENGTH_SHORT).show());
            }
        });
    }


    private void saveMedications() {
        Set<String> set = new HashSet<>();
        for (Medication medication : medicationList) {
            set.add(medication.toString());
        }
        prefs.edit().putStringSet("medications_list", set).apply();
    }

    @Override
    public void onEditClick(int position) {
        Intent intent = new Intent(this, AddEditMedicationActivity.class);
        Medication medication = medicationList.get(position);
        intent.putExtra("name", medication.getName());
        intent.putExtra("instructions", medication.getInstructions());
        intent.putExtra("frequency",    medication.getFrequency());
        intent.putExtra("reminderTime", medication.getReminderTimeMillis());
        intent.putExtra("position", position);
        startActivityForResult(intent, EDIT_MEDICATION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("name");
            String instr        = data.getStringExtra("instructions");
            String freq         = data.getStringExtra("frequency");
            long reminderTime = data.getLongExtra("reminderTime", 0);
            int position = data.getIntExtra("position", -1);

            if (requestCode == ADD_MEDICATION_REQUEST) {
                medicationList.add(new Medication(-1, name, instr, freq, reminderTime));
                adapter.notifyItemInserted(medicationList.size() - 1);
            } else if (requestCode == EDIT_MEDICATION_REQUEST && position != -1) {
                int existingId = medicationList.get(position).getId();
                medicationList.set(position, new Medication(existingId, name, instr, freq, reminderTime));
                adapter.notifyItemChanged(position);
            }
            saveMedications();
        }
    }

    private final ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            Medication toRemove = medicationList.get(position);
            int medId = toRemove.getId();
            int userId = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    .getInt("userId", -1);

            new AlertDialog.Builder(MedicationActivity.this)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this medication?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // 1) call the API to delete the link in usermedications
                        ApiService.deleteMedication(userId, medId, new ApiService.ApiCallback<Void>() {
                            @Override public void onSuccess(Void unused) {
                                // 2) only remove locally on server success
                                runOnUiThread(() -> {
                                    medicationList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                });
                            }
                            @Override public void onFailure(String err) {
                                // 3) on error, reset the swipe and show a message
                                runOnUiThread(() -> {
                                    adapter.notifyItemChanged(position);
                                    Toast.makeText(MedicationActivity.this,
                                            "Could not delete: " + err,
                                            Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        adapter.notifyItemChanged(position);
                    })
                    .setCancelable(false)
                    .show();
        }

    };
}
