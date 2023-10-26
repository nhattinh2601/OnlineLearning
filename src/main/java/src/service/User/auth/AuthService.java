package src.service.User.auth;

import src.service.User.Dto.SignupDto;
import src.service.User.Dto.UserDto;

public interface AuthService {
    UserDto createUser(SignupDto signupDTO);
}