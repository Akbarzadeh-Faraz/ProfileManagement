package ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.profilemanagement.R;
import api.ApiService;
import api.RetrofitClient;
import model.LoginRequest;
import model.LoginResponse;
import util.SharedPreferencesHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;
    private TextView errorText;
    private Button loginButton, registerButton;
    private SharedPreferencesHelper prefs;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getClient().create(ApiService.class);

        if (prefs.getToken() != null) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
            return;
        }

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        errorText = findViewById(R.id.error_text);

        loginButton.setOnClickListener(v -> login());
        registerButton.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void login() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            errorText.setText("Please enter username and password");
            errorText.setVisibility(View.VISIBLE);
            return;
        }

        Call<LoginResponse> call = apiService.login(new LoginRequest(username, password));
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    String token = response.body().token;
                    prefs.saveCredentials(username, token);
                    startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                    finish();
                } else if (response.code() == 500) {
                    Toast.makeText(LoginActivity.this, "Server error during login", Toast.LENGTH_SHORT).show();
                    errorText.setText("Server error during login");
                    errorText.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    errorText.setText("Invalid credentials");
                    errorText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Login failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                errorText.setText("Login failed: " + t.getMessage());
                errorText.setVisibility(View.VISIBLE);
            }
        });
    }
}