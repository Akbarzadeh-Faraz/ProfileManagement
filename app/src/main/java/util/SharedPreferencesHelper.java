package util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SharedPreferencesHelper {
    private static final String PREF_NAME = "prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_TOKEN = "token";
    private final SharedPreferences sharedPreferences;

    public SharedPreferencesHelper(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize EncryptedSharedPreferences", e);
        }
    }

    public void saveCredentials(String username, String token) {
        sharedPreferences.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_TOKEN, token)
                .apply();
    }

    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public void clearCredentials() {
        sharedPreferences.edit()
                .remove(KEY_USERNAME)
                .remove(KEY_TOKEN)
                .apply();
    }
}