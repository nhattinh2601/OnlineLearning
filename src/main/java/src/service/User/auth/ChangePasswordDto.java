package src.service.User.auth;

import lombok.Data;

@Data
public class ChangePasswordDto {
    private int userId;
    private String email;
    private String oldPassword;
    private String newPassword;
    private String rePassword;


}
