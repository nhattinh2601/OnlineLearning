package src.service.User.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class UserProfileDto {

    @JsonProperty(value = "fullname", required = true)
    private String fullname;
    @JsonProperty(value = "email", required = true)
    private String email;
    @JsonProperty(value = "phone", required = true, defaultValue = "0")
    private String phone;
    @JsonProperty(value = "avatar", required = true, defaultValue = "0")
    private String avatar;
    @JsonProperty(value = "description", required = true)
    private String description;
    @JsonProperty(value = "password", required = true, defaultValue = "0")
    private String password;
}
