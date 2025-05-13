package Login_Register;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitwizard.fitwizard.HomeActivity;
import com.fitwizard.fitwizard.R;
import com.fitwizard.fitwizard.network.ApiService;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, passwordInput;
    private Button   registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        nameInput     = findViewById(R.id.editTextName);
        emailInput    = findViewById(R.id.editTextEmail);
        passwordInput = findViewById(R.id.editTextPassword);
        registerBtn   = findViewById(R.id.buttonRegister);

        registerBtn.setOnClickListener(v -> {
            String name  = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String pass  = passwordInput.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this,
                        "Please fill in all fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Call remote register
            ApiService.register(name, email, pass, new ApiService.AuthCallback() {
                public void onSuccess(String jwt, int userId) {
                    // save JWT and userId
                    SharedPreferences prefs = getSharedPreferences(
                            "UserPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("jwt", jwt)
                            .putInt("userId", userId)
                            .apply();

                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this,
                                "Account created!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(
                                RegisterActivity.this, HomeActivity.class));
                        finish();
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() ->
                            Toast.makeText(RegisterActivity.this,
                                    "Register failed: " + error,
                                    Toast.LENGTH_SHORT).show()
                    );
                }
            });
        });
    }
}
