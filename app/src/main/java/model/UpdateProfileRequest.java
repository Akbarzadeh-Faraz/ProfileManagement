package model;

public class UpdateProfileRequest {
    public String username;
    public String password;
    public String full_name;
    public String date_of_birth;
    public String address;
    public String phone_number;
    public String profile_picture;

    public UpdateProfileRequest(String username, String password, String full_name, String date_of_birth,
                                String address, String phone_number, String profile_picture) {
        this.username = username;
        this.password = password;
        this.full_name = full_name;
        this.date_of_birth = date_of_birth;
        this.address = address;
        this.phone_number = phone_number;
        this.profile_picture = profile_picture;
    }
}