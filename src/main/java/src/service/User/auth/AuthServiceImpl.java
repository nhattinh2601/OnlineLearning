package src.service.User.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import src.config.auth.JwtTokenUtil;
import src.model.User;
import src.repository.UserRepository;
import src.service.User.Dto.UserCreateDto;
import src.service.User.Dto.UserDto;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDto createUser(UserCreateDto userCreateDTO) {
        User user = new User();
        user.setFullname(userCreateDTO.getFullname());
        user.setEmail(userCreateDTO.getEmail());
        user.setPhone(userCreateDTO.getPhone());
        user.setAvatar(userCreateDTO.getAvatar());
        user.setDescription(userCreateDTO.getDescription());
        user.setBank_name(userCreateDTO.getBank_name());
        user.setAccount_number(userCreateDTO.getAccount_number());
        user.setAccount_name(userCreateDTO.getAccount_name());
        user.setRoleId(userCreateDTO.getRoleId());
        user.setPassword(JwtTokenUtil.hashPassword(userCreateDTO.getPassword()));
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
        userDTO.setPassword(JwtTokenUtil.hashPassword(createdUser.getPassword()));
        return userDTO;
    }
}
