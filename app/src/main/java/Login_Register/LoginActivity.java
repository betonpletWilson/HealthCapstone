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

public class LoginActivity extends AppCompatActivity {

    private EditText idInput, passwordInput;   // idInput = username OR email
    private Button   loginBtn, registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        idInput       = findViewById(R.id.editTextIdentifier);
        passwordInput = findViewById(R.id.editTextPassword);
        loginBtn      = findViewById(R.id.buttonLogin);
        registerBtn   = findViewById(R.id.buttonRegister);

        loginBtn.setOnClickListener(v -> {
            String id   = idInput.getText().toString().trim();
            String pass = passwordInput.getText().toString().trim();

            if (id.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this,
                        "Enter username/email and password",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService.login(id, pass, new ApiService.AuthCallback() {
                @Override public void onSuccess(String jwt) {
                    getSharedPreferences("UserPrefs", MODE_PRIVATE)
                            .edit().putString("jwt", jwt).apply();
                    runOnUiThread(() -> {
                        startActivity(new Intent(
                                LoginActivity.this, HomeActivity.class));
                        finish();
                    });
                }
                @Override public void onFailure(String err) {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this,
                                    "Login failed: " + err,
                                    Toast.LENGTH_SHORT).show()
                    );
                }
            });
        });

        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
}
