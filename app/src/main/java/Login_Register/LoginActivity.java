package Login_Register;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitwizard.fitwizard.HomeActivity;
import com.fitwizard.fitwizard.R;
import com.fitwizard.fitwizard.network.ApiService;

public class LoginActivity extends AppCompatActivity {

    private EditText  idInput;
    private EditText  passwordInput;
    private CheckBox  rememberCB;
    private Button    loginBtn;
    private Button    registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedJwt = prefs.getString("jwt", null);

        if (savedJwt != null) {
            Log.d("JWT-LOAD", savedJwt);
            ApiService.validate(savedJwt, new ApiService.AuthCallback() {
                @Override public void onSuccess(String jwt, int userId, String username) {
                    launchHome();                          // already uses runOnUiThread
                }
                @Override public void onFailure(String err) {
                    prefs.edit().remove("jwt").apply();
                    runOnUiThread(() -> buildUi());        // <-- wrap it here
                }
            });
        } else {
            buildUi();                                     // this is already on UI thread
        }
    }

    /** inflate layout and wire up buttons exactly once */
    private void buildUi() {
        setContentView(R.layout.activity_login);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        idInput       = findViewById(R.id.editTextIdentifier);
        passwordInput = findViewById(R.id.editTextPassword);
        rememberCB    = findViewById(R.id.checkRemember);
        loginBtn      = findViewById(R.id.buttonLogin);
        registerBtn   = findViewById(R.id.buttonRegister);

        loginBtn.setOnClickListener(v -> attemptLogin());
        registerBtn.setOnClickListener(
                v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    /** POST /login then persist token if “Remember me” is ticked */
    private void attemptLogin() {
        String id   = idInput.getText().toString().trim();
        String pass = passwordInput.getText().toString().trim();

        if (id.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this,
                    "Enter username / email and password",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService.login(id, pass, new ApiService.AuthCallback() {
            @Override public void onSuccess(String jwt, int userId, String username) {
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                prefs.edit()
                        .putString("jwt", jwt)
                        .putInt("userId", userId)
                        .putString("user_name", username)
                        .apply();

                launchHome();
                launchHome();
            }
            @Override public void onFailure(String err) {
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this,
                                "Login failed: " + err,
                                Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void launchHome() {
        runOnUiThread(() -> {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        });
    }
}
