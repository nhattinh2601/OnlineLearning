package src.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import src.config.annotation.ApiPrefixController;
import src.config.auth.JwtUtil;
import src.config.jwt.UserDetailsServiceImpl;
import src.model.User;
import src.repository.UserRepository;
import src.service.User.Dto.*;
import src.service.User.auth.AuthService;

import java.util.ArrayList;
import java.util.List;


@RestController
@ApiPrefixController(value = "/auth")
public class AuthenticationController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthService authService;

    // Danh sách token đã hủy
    private List<String> revokedTokens = new ArrayList<>();


    @PostMapping("/login")
    public AuthenticationResponse createAuthenticationToken(AuthenticationDto authenticationDto, HttpServletResponse response) throws Exception {
        // Lấy thông tin người dùng từ UserDetailsService
        UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationDto.getEmail());
        if (userDetails == null){
            throw new Exception("Cannot find user with email");
        }
        // Xử lý mật khẩu đã băm với mật khẩu nhập
        if (!JwtUtil.comparePassword(authenticationDto.getPassword(), userDetails.getPassword())){
            throw new Exception("Password not correct");
        }
        else {
            // Mật khẩu đúng
            String jwt = jwtUtil.generateToken(userDetails.getUsername());
            response.addHeader("Authorization", "Bearer " + jwt);
            return new AuthenticationResponse(jwt);
        }
    }


    @PostMapping("/signup")
    public ResponseEntity<?> signupUser(@RequestBody SignupDto signupDto) {
        if (isEmailAlreadyTaken(signupDto.getEmail())) {
            return new ResponseEntity<>("Email already exists", HttpStatus.BAD_REQUEST);
        }
        UserDto createdUser = authService.createUser(signupDto);
        if (createdUser == null){
            return new ResponseEntity<>("User not created, come again later!", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    private boolean isEmailAlreadyTaken(String email) {
        User existingUser = userRepository.findByEmail(email);
        return existingUser != null;
    }





}