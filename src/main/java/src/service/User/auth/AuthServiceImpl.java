package src.service.User.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import src.config.auth.JwtUtil;
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
        user.setPassword(JwtUtil.hashPassword(signupDto.getPassword()));
        User createdUser = userRepository.save(user);
        UserDto userDTO = new UserDto();
        userDTO.setId(createdUser.getId());
        userDTO.setFullname(createdUser.getFullname());
        userDTO.setEmail(createdUser.getEmail());
        userDTO.setPhone(createdUser.getPhone());
        userDTO.setAvatar(createdUser.getAvatar());
        userDTO.setDescription(createdUser.getDescription());
        userDTO.setBank_name(createdUser.getBank_name());
        userDTO.setAccount_number(createdUser.getAccount_number());
        userDTO.setAccount_name(createdUser.getAccount_name());
        userDTO.setRoleId(createdUser.getRoleId());
        userDTO.setPassword(JwtUtil.hashPassword(createdUser.getPassword()));
        return userDTO;
    }
}
