package src.service.User.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuthenticationDto {
    @JsonProperty(value = "email", required = true)
    private String email;
    @JsonProperty(value = "password", required = true)
    private String password;

}