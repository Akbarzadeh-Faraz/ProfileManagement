package ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.profilemanagement.R;
import api.ApiService;
import api.RetrofitClient;
import model.DiaryEntry;
import model.DiaryEntryRequest;
import util.SharedPreferencesHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiaryEntryActivity extends AppCompatActivity {
    private EditText titleEditText, contentEditText;
    private Button saveButton, editButton, deleteButton;
    private SharedPreferencesHelper prefs;
    private ApiService apiService;
    private int entryId = -1;
    private String originalTitle = "";
    private String originalContent = "";
    private boolean isViewMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_entry);

        prefs = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Initialize UI elements
        titleEditText = findViewById(R.id.title);
        contentEditText = findViewById(R.id.content);
        saveButton = findViewById(R.id.save_button);
        editButton = findViewById(R.id.edit_button);
        deleteButton = findViewById(R.id.delete_button);

        // Check if we're editing an existing entry
        if (getIntent().hasExtra("entry_id")) {
            // View mode - existing entry
            isViewMode = true;
            entryId = getIntent().getIntExtra("entry_id", -1);
            originalTitle = getIntent().getStringExtra("title");
            originalContent = getIntent().getStringExtra("content");

            titleEditText.setText(originalTitle);
            contentEditText.setText(originalContent);

            // Set initial view mode UI state
            setViewMode();
        } else {
            // Create mode - new entry
            setCreateMode();
        }

        // Setup click listeners
        saveButton.setOnClickListener(v -> saveEntry());

        editButton.setOnClickListener(v -> {
            setEditMode();
        });

        deleteButton.setOnClickListener(v -> {
            DiaryEntry entry = new DiaryEntry();
            entry.id = entryId;
            deleteDiaryEntry(entry);
        });
    }

    private void setViewMode() {
        // View-only mode
        titleEditText.setEnabled(false);
        contentEditText.setEnabled(false);

        // Show only Edit button
        editButton.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.GONE);
        deleteButton.setVisibility(View.GONE);
    }

    private void setEditMode() {
        // Edit mode
        titleEditText.setEnabled(true);
        contentEditText.setEnabled(true);

        titleEditText.requestFocus();

        // Show Save and Delete buttons
        editButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.VISIBLE);
        deleteButton.setVisibility(View.VISIBLE);
    }

    private void setCreateMode() {
        // Create mode
        titleEditText.setEnabled(true);
        contentEditText.setEnabled(true);

        // Show only Save button
        saveButton.setVisibility(View.VISIBLE);
        editButton.setVisibility(View.GONE);
        deleteButton.setVisibility(View.GONE);
    }

    private void saveEntry() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (content.isEmpty() && title.isEmpty()) {
            Toast.makeText(this, "Please enter title or content", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = prefs.getToken();
        DiaryEntryRequest request = new DiaryEntryRequest(title, content);
        Call<Void> call;

        if (entryId == -1) {
            // Create new entry
            call = apiService.addDiaryEntry(token, request);
        } else {
            // Update existing entry
            call = apiService.updateDiaryEntry(entryId, token, request);
        }

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DiaryEntryActivity.this, "Entry saved", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(DiaryEntryActivity.this, "Failed to save entry", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DiaryEntryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteDiaryEntry(DiaryEntry entry) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this entry?")
            .setPositiveButton("Yes", (dialog, which) -> {
                String token = prefs.getToken();
                Call<Void> call = apiService.deleteDiaryEntry(entry.id, token);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(DiaryEntryActivity.this, "Entry deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(DiaryEntryActivity.this, "Failed to delete entry", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(DiaryEntryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("No", null)
            .show();
    }
}