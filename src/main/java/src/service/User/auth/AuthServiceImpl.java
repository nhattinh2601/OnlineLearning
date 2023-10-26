package src.service.User.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import src.model.User;
import src.repository.UserRepository;
import src.service.User.Dto.SignupDto;
import src.service.User.Dto.UserDto;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDto createUser(SignupDto signupDto) {
        User user = new User();
        user.setFullname(signupDto.getFullname());
        user.setEmail(signupDto.getEmail());
        user.setPhone(signupDto.getPhone());
        user.setAvatar(signupDto.getAvatar());
        user.setDescription(signupDto.getDescription());
        user.setBank_name(signupDto.getBank_name());
        user.setAccount_number(signupDto.getAccount_number());
        user.setAccount_name(signupDto.getAccount_name());
        user.setRoleId(signupDto.getRoleId());
        user.setPassword(signupDto.getPassword());
        User createdUser = userRepository.save(user);
        UserDto userDTO = new UserDto();
        userDTO.setId(createdUser.getId());
        userDTO.setEmail(createdUser.getEmail());
        userDTO.setFullname(createdUser.getFullname());
        return userDTO;
    }
}
