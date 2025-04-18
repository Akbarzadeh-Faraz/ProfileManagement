package ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.profilemanagement.R;
import com.squareup.picasso.Picasso;
import api.ApiService;
import api.RetrofitClient;
import de.hdodenhof.circleimageview.CircleImageView;
import model.DeleteProfileRequest;
import model.UpdateProfileResponse;
import model.User;
import util.SharedPreferencesHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EditProfileActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText, fullNameEditText, dobEditText, addressEditText, phoneEditText;
    private CircleImageView profilePicturePreview;
    private Button saveButton, deleteButton, selectPictureButton;
    private SharedPreferencesHelper prefs;
    private ApiService apiService;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        prefs = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getClient().create(ApiService.class);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        fullNameEditText = findViewById(R.id.full_name);
        dobEditText = findViewById(R.id.date_of_birth);
        addressEditText = findViewById(R.id.address);
        phoneEditText = findViewById(R.id.phone_number);
        profilePicturePreview = findViewById(R.id.profile_picture_preview);
        selectPictureButton = findViewById(R.id.select_picture_button);
        saveButton = findViewById(R.id.save_button);
        deleteButton = findViewById(R.id.delete_button);

        loadProfile();

        selectPictureButton.setOnClickListener(v -> selectImage());
        saveButton.setOnClickListener(v -> saveProfile());
        deleteButton.setOnClickListener(v -> confirmDelete());
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

    private void loadProfile() {
        String token = prefs.getToken();
        Call<User> call = apiService.getProfile(token);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user.profile_picture != null && !user.profile_picture.isEmpty()) {
                        Picasso.get().load(RetrofitClient.getClient().baseUrl() + user.profile_picture)
                                .into(profilePicturePreview);
                    } else {
                        profilePicturePreview.setImageResource(R.drawable.default_avatar);
                    }
                    usernameEditText.setText(user.username);
                    fullNameEditText.setText(user.full_name);
                    dobEditText.setText(user.date_of_birth);
                    addressEditText.setText(user.address);
                    phoneEditText.setText(user.phone_number);
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String fullName = fullNameEditText.getText().toString().trim();
        String dob = dobEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (username.isEmpty() || fullName.isEmpty() || dob.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody usernameBody = RequestBody.create(MediaType.parse("text/plain"), username);
        RequestBody passwordBody = password.isEmpty() ? null : RequestBody.create(MediaType.parse("text/plain"), password);
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

        String token = prefs.getToken();
        Call<UpdateProfileResponse> call = apiService.updateProfile(token, usernameBody, passwordBody, fullNameBody, dobBody, addressBody, phoneBody, profilePicturePart);
        call.enqueue(new Callback<UpdateProfileResponse>() {
            @Override
            public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {
                if (response.isSuccessful()) {
                    String message = response.body().message;
                    Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                    if (message.contains("credentials changed")) {
                        prefs.clearCredentials();
                        Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        finish();
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmPassword() {
        EditText passwordInput = new EditText(this);
        passwordInput.setHint("Password");
        new AlertDialog.Builder(this)
            .setTitle("Delete Profile")
            .setMessage("Enter your password to confirm deletion:")
            .setView(passwordInput)
            .setPositiveButton("Delete", (dialog, which) -> {
                String password = passwordInput.getText().toString().trim();
                if (password.isEmpty()) {
                    Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }
                deleteProfile(password);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Profile")
            .setMessage("Are you sure you want to delete your profile?")
            .setPositiveButton("Delete", (dialog, which) -> {
                confirmPassword();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteProfile(String password) {
        String token = prefs.getToken();
        Call<Void> call = apiService.deleteProfile(token, new DeleteProfileRequest(password));
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Profile deleted", Toast.LENGTH_SHORT).show();
                    prefs.clearCredentials();
                    Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Deletion failed: Incorrect password?", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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