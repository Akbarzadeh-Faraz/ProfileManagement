package api;

import model.*;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

public interface ApiService {
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @Multipart
    @POST("register")
    Call<Void> register(
            @Part("username") RequestBody username,
            @Part("password") RequestBody password,
            @Part("full_name") RequestBody full_name,
            @Part("date_of_birth") RequestBody date_of_birth,
            @Part("address") RequestBody address,
            @Part("phone_number") RequestBody phone_number,
            @Part MultipartBody.Part profile_picture
    );

    @GET("profile")
    Call<User> getProfile(@Query("token") String token);

    @Multipart
    @PUT("profile")
    Call<UpdateProfileResponse> updateProfile(
            @Query("token") String token,
            @Part("username") RequestBody username,
            @Part("password") RequestBody password,
            @Part("full_name") RequestBody full_name,
            @Part("date_of_birth") RequestBody date_of_birth,
            @Part("address") RequestBody address,
            @Part("phone_number") RequestBody phone_number,
            @Part MultipartBody.Part profile_picture
    );

    @POST("profile")
    Call<Void> deleteProfile(@Query("token") String token, @Body DeleteProfileRequest request);

    @POST("diary")
    Call<Void> addDiaryEntry(@Query("token") String token, @Body DiaryEntryRequest request);

    @PUT("diary/{entry_id}")
    Call<Void> updateDiaryEntry(@Path("entry_id") int entryId, @Query("token") String token, @Body DiaryEntryRequest request);

    @DELETE("diary/{entry_id}")
    Call<Void> deleteDiaryEntry(@Path("entry_id") int entryId, @Query("token") String token);

    @GET("diary")
    Call<List<DiaryEntry>> getDiaryEntries(@Query("token") String token);
}