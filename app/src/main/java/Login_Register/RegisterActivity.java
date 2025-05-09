package Login_Register;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitwizard.fitwizard.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        nameInput = findViewById(R.id.editTextName);
        emailInput = findViewById(R.id.editTextEmail);
        passwordInput = findViewById(R.id.editTextPassword);
        Button registerBtn = findViewById(R.id.buttonRegister);

        registerBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            // Save user-specific credentials using unique keys
            editor.putString("user_" + email + "_name", name);
            editor.putString("user_" + email + "_password", password);

            // Set current user session
            editor.putString("current_user", email);
            editor.putBoolean("is_logged_in", true);
            editor.apply();

            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();

            // Go directly to HomeActivity after registration
            startActivity(new Intent(this, com.fitwizard.fitwizard.HomeActivity.class));
            finish();
        });

    }
}
