package ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.profilemanagement.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;
import api.ApiService;
import api.RetrofitClient;
import de.hdodenhof.circleimageview.CircleImageView;
import model.DiaryEntry;
import model.User;
import util.SharedPreferencesHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private CircleImageView profilePictureImageView;
    private TextView fullNameText, dobText, addressText, phoneText;
    private RecyclerView diaryRecyclerView;
    private DiaryAdapter diaryAdapter;
    private Button addEntryButton;
    private FloatingActionButton fabAddEntry;
    private SharedPreferencesHelper prefs;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefs = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getClient().create(ApiService.class);

        profilePictureImageView = findViewById(R.id.profile_picture);
        fullNameText = findViewById(R.id.full_name);
        dobText = findViewById(R.id.date_of_birth);
        addressText = findViewById(R.id.address);
        phoneText = findViewById(R.id.phone_number);
        diaryRecyclerView = findViewById(R.id.diary_list);
        addEntryButton = findViewById(R.id.add_entry_button);
        fabAddEntry = findViewById(R.id.fab_add_entry);

        diaryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        diaryAdapter = new DiaryAdapter(this::viewDiaryEntry);
        diaryRecyclerView.setAdapter(diaryAdapter);

        addEntryButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DiaryEntryActivity.class);
            startActivity(intent);
        });
        fabAddEntry.setOnClickListener(v -> {
            Intent intent = new Intent(this, DiaryEntryActivity.class);
            startActivity(intent);
        });

        loadProfile();
        loadDiaryEntries();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit_profile) {
            startActivity(new Intent(this, EditProfileActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            prefs.clearCredentials();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                                .into(profilePictureImageView);
                    } else {
                        profilePictureImageView.setImageResource(R.drawable.default_avatar);
                    }
                    fullNameText.setText(user.full_name);
                    dobText.setText(user.date_of_birth);
                    addressText.setText(user.address);
                    phoneText.setText(user.phone_number);
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDiaryEntries() {
        String token = prefs.getToken();
        Call<List<DiaryEntry>> call = apiService.getDiaryEntries(token);
        call.enqueue(new Callback<List<DiaryEntry>>() {
            @Override
            public void onResponse(Call<List<DiaryEntry>> call, Response<List<DiaryEntry>> response) {
                if (response.isSuccessful()) {
                    diaryAdapter.setDiaryEntries(response.body());
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load diary", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<DiaryEntry>> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void viewDiaryEntry(DiaryEntry entry) {
        Intent intent = new Intent(this, DiaryEntryActivity.class);
        intent.putExtra("entry_id", entry.id);
        intent.putExtra("title", entry.title);
        intent.putExtra("content", entry.content);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
        loadDiaryEntries();
    }
}