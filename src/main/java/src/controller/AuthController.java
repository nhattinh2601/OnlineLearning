package src.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import src.config.annotation.ApiPrefixController;
import src.config.auth.JwtTokenUtil;
import src.config.exception.BadRequestException;
import src.model.User;
import src.repository.UserRepository;
import src.service.User.Dto.UserCreateDto;
import src.service.User.Dto.UserDto;
import src.service.User.Dto.UserProfileDto;
import src.service.User.IUserService;
import src.service.User.auth.*;

import java.util.List;

@RestController
@ApiPrefixController("/auth")
@Tag(name = "User authentication")
public class AuthController {

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthService authService;
    @Autowired
    private MailService mailService;
    @Autowired
    private ModelMapper toDto;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody @Valid LoginInputDto loginRequest) throws Exception {
        final User user = userRepository.findByEmail(loginRequest.getUsername());
        if (user == null){
            throw new Exception("Cannot find user with email");
        }
        if (!JwtTokenUtil.comparePassword(loginRequest.getPassword(), user.getPassword())){
            throw new Exception("Password not correct");
        }
        final String accessToken = jwtUtil.generateAccessToken(user);
        final String refreshToken = jwtUtil.generateRefreshToken(user);
        toDto.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return ResponseEntity.ok(new LoginDto(accessToken, refreshToken,toDto.map(user, UserProfileDto.class) ));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAuthenticationToken(@RequestBody @Valid RefreshTokenInput refreshTokenRequest) throws Exception {
        final String refreshToken = refreshTokenRequest.getRefreshToken();
        // Check if the refresh token is valid and not expired
        String username = jwtUtil.checkRefreshToken(refreshToken);
        if (username == null)
            throw new BadRequestException("Not Type Refresh Token");
        final User userDetails = userRepository.findByEmail(jwtUtil.getUsernameFromToken(refreshToken));
        if (jwtUtil.validateToken(refreshToken, userDetails)) {
            final String accessToken = jwtUtil.generateAccessToken(userDetails);
            return ResponseEntity.ok(new RefreshTokenDto(accessToken, refreshToken));
        }
        throw new Exception("Invalid refresh token");
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signupUser(@RequestBody UserCreateDto userCreateDto) {
        if (isEmailAlreadyTaken(userCreateDto.getEmail())) {
            return new ResponseEntity<>("Email already exists", HttpStatus.BAD_REQUEST);
        }
        UserDto createdUser = authService.createUser(userCreateDto);
        if (createdUser == null){
            return new ResponseEntity<>("User not created, come again later!", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    private boolean isEmailAlreadyTaken(String email) {
        User existingUser = userRepository.findByEmail(email);
        return existingUser != null;
    }

    @PostMapping("/send/message/all-users")
    public ResponseEntity<String> sendEmailToAllUsers(@RequestBody MessageDto messageDto) {
        List<User> users = userRepository.findAll(); // Lấy danh sách tất cả người dùng

        for (User user : users) {
            String email = user.getEmail();

            try {
                // Gửi email đến địa chỉ email của người dùng
                mailService.sendMessageMail(email, messageDto);
            } catch (Exception e) {
                // Xử lý lỗi khi gửi email
                // Bạn có thể ghi log lỗi hoặc thực hiện các xử lý cụ thể khác
            }
        }

        return new ResponseEntity<>("Emails sent to all users!", HttpStatus.OK);
    }



    @PostMapping("/send/message/{mail}")
    public ResponseEntity<String> sendMessageMail(@PathVariable String mail, @RequestBody MessageDto messageDto) {
        User user = userRepository.findByEmail(mail);

        if (user == null) {
            return new ResponseEntity<>("Email not found in the database", HttpStatus.BAD_REQUEST);
        }

        // Gửi email vì email và số điện thoại đều khớp
        mailService.sendMessageMail(mail, messageDto);
        return new ResponseEntity<>("Successfully sent the email!", HttpStatus.OK);
    }







}