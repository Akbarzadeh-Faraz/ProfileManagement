package ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.profilemanagement.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import api.ApiService;
import api.RetrofitClient;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText, fullNameEditText, dobEditText, addressEditText, phoneEditText;
    private CircleImageView profilePicturePreview;
    private Button registerButton, selectPictureButton;
    private ApiService apiService;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        fullNameEditText = findViewById(R.id.full_name);
        dobEditText = findViewById(R.id.date_of_birth);
        addressEditText = findViewById(R.id.address);
        phoneEditText = findViewById(R.id.phone_number);
        profilePicturePreview = findViewById(R.id.profile_picture_preview);
        selectPictureButton = findViewById(R.id.select_picture_button);
        registerButton = findViewById(R.id.register_button);

        selectPictureButton.setOnClickListener(v -> selectImage());
        registerButton.setOnClickListener(v -> register());
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) ->
                dobEditText.setText(String.format("%04d-%02d-%02d", year, month + 1, day)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            profilePicturePreview.setImageURI(selectedImageUri);
        }
    }

    private void register() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String fullName = fullNameEditText.getText().toString().trim();
        String dob = dobEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || dob.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody usernameBody = RequestBody.create(MediaType.parse("text/plain"), username);
        RequestBody passwordBody = RequestBody.create(MediaType.parse("text/plain"), password);
        RequestBody fullNameBody = RequestBody.create(MediaType.parse("text/plain"), fullName);
        RequestBody dobBody = RequestBody.create(MediaType.parse("text/plain"), dob);
        RequestBody addressBody = RequestBody.create(MediaType.parse("text/plain"), address);
        RequestBody phoneBody = RequestBody.create(MediaType.parse("text/plain"), phone);

        MultipartBody.Part profilePicturePart = null;
        if (selectedImageUri != null) {
            try {
                File file = createTempImageFile();
                copyUriToFile(selectedImageUri, file);
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                profilePicturePart = MultipartBody.Part.createFormData("profile_picture", file.getName(), requestFile);
            } catch (IOException e) {
                Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Call<Void> call = apiService.register(usernameBody, passwordBody, fullNameBody, dobBody, addressBody, phoneBody, profilePicturePart);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Registered successfully. Please login.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else if (response.code() == 500) {
                    Toast.makeText(RegisterActivity.this, "Server error during registration", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 409) {
                    Toast.makeText(RegisterActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401) {
                    Toast.makeText(RegisterActivity.this,
                            "Password must be 8+ characters with letters, numbers, one capital, and one special character",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Registration failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File createTempImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalCacheDir();
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void copyUriToFile(Uri uri, File file) throws IOException {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }
}