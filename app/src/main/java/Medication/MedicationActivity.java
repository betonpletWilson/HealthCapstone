package Medication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitwizard.fitwizard.R;
import Login_Register.UserSession;

import java.util.ArrayList;
import java.util.HashSet;
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
        setContentView(R.layout.activity_medication);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        recyclerView = findViewById(R.id.medicationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        prefs = getSharedPreferences("MedicationPrefs", MODE_PRIVATE);
        medicationList = loadMedications();
        adapter = new MedicationAdapter(this, medicationList, this);
        recyclerView.setAdapter(adapter);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        findViewById(R.id.addMedicationButton).setOnClickListener(v -> {
            Intent intent = new Intent(MedicationActivity.this, AddEditMedicationActivity.class);
            startActivityForResult(intent, ADD_MEDICATION_REQUEST);
        });

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }

    private ArrayList<Medication> loadMedications() {
        Set<String> set = prefs.getStringSet(UserSession.getUserKey(this, "medications_list"), new HashSet<>());
        ArrayList<Medication> list = new ArrayList<>();
        for (String item : set) {
            list.add(Medication.fromString(item));
        }
        return list;
    }

    private void saveMedications() {
        Set<String> set = new HashSet<>();
        for (Medication medication : medicationList) {
            set.add(medication.toString());
        }
        prefs.edit().putStringSet(UserSession.getUserKey(this, "medications_list"), set).apply();
    }

    @Override
    public void onEditClick(int position) {
        Intent intent = new Intent(this, AddEditMedicationActivity.class);
        Medication medication = medicationList.get(position);
        intent.putExtra("name", medication.getName());
        intent.putExtra("reminderTime", medication.getReminderTimeMillis());
        intent.putExtra("position", position);
        startActivityForResult(intent, EDIT_MEDICATION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("name");
            long reminderTime = data.getLongExtra("reminderTime", 0);
            int position = data.getIntExtra("position", -1);

            if (requestCode == ADD_MEDICATION_REQUEST) {
                medicationList.add(new Medication(name, reminderTime));
                adapter.notifyItemInserted(medicationList.size() - 1);
            } else if (requestCode == EDIT_MEDICATION_REQUEST && position != -1) {
                medicationList.set(position, new Medication(name, reminderTime));
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

            new androidx.appcompat.app.AlertDialog.Builder(MedicationActivity.this)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this medication?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        medicationList.remove(position);
                        adapter.notifyItemRemoved(position);
                        saveMedications();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        adapter.notifyItemChanged(position);
                    })
                    .setCancelable(false)
                    .show();
        }
    };
}