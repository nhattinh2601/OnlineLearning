package src.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import src.config.annotation.ApiPrefixController;
import src.config.auth.JwtUtil;
import src.config.jwt.UserDetailsServiceImpl;
import src.model.User;
import src.repository.UserRepository;
import src.service.User.Dto.AuthenticationDto;
import src.service.User.Dto.AuthenticationResponse;
import src.service.User.Dto.SignupDto;
import src.service.User.Dto.UserDto;
import src.service.User.auth.AuthService;

import java.io.IOException;

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


    @PostMapping("/sign-in")
    public AuthenticationResponse createAuthenticationToken(AuthenticationDto authenticationDto, HttpServletResponse response) throws IOException {
        // Lấy thông tin người dùng từ UserDetailsService
        UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationDto.getEmail());

        // Thực hiện xác thực mật khẩu
        if (isPasswordValid(authenticationDto.getPassword(), userDetails.getPassword())) {
            // Mật khẩu đúng, tạo token JWT và trả về
            String jwt = jwtUtil.generateToken(userDetails.getUsername());
            return new AuthenticationResponse(jwt);
        } else {
            // Mật khẩu không đúng, xử lý lỗi tại đây
            throw new BadCredentialsException("Incorrect username or password!");
        }
    }

    private boolean isPasswordValid(String inputPassword, String storedPassword) {
        // Thực hiện so sánh mật khẩu nhập vào với mật khẩu đã lưu
        // Bạn có thể sử dụng các phương thức băm và so sánh để kiểm tra tính đúng đắn của mật khẩu
        // Ở đây, ta sẽ giả sử mật khẩu được lưu trữ ở dạng plaintext để minh họa
        return inputPassword.equals(storedPassword);
    }
    @PostMapping("/sign-up")
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
        // Kiểm tra xem email đã tồn tại trong cơ sở dữ liệu hay chưa
        User existingUser = userRepository.findByEmail(email);
        return existingUser != null;
    }

}
