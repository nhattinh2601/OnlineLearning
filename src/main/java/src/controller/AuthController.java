package src.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import src.config.annotation.ApiPrefixController;
import src.config.auth.JwtTokenUtil;
import src.config.exception.BadRequestException;
import src.model.User;
import src.repository.UserRepository;
import src.service.User.Dto.UserCreateDto;
import src.service.User.Dto.UserDto;
import src.service.User.Dto.UserProfileDto;

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
        final User user = userRepository.findByEmail(loginRequest.getEmail());
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

    @PostMapping("/register")
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

    @PostMapping("/forget-password")
    public ResponseEntity<?> forgetPassword(@RequestBody @Valid ForgetPasswordDto forgetPasswordDto) {

        User user = userRepository.findByEmail(forgetPasswordDto.getEmail());
        if (user == null) {
            throw new BadRequestException("Email not found");
        }

        if (!forgetPasswordDto.getNewPassword().equals(forgetPasswordDto.getRePassword())) {
            throw new BadRequestException("Password and rePassword do not match");
        }

        user.setPassword(JwtTokenUtil.hashPassword(forgetPasswordDto.getNewPassword())); // Thực hiện mã hóa mật khẩu

        userRepository.save(user);

        return ResponseEntity.ok("Password reset successfully");
    }

    @PostMapping("/send/message/all-users")
    public ResponseEntity<String> sendEmailToAllUsers(@RequestBody MessageDto messageDto) {
        List<User> users = userRepository.findAll(); // Lấy danh sách tất cả người dùng

        for (User user : users) {
            String email = user.getEmail();

            try {

                mailService.sendMessageMail(email, messageDto);
            } catch (Exception e) {

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


        mailService.sendMessageMail(mail, messageDto);
        return new ResponseEntity<>("Successfully sent the email!", HttpStatus.OK);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordDto changePasswordDto) {

        User user = userRepository.findByEmail(changePasswordDto.getEmail());
        if (user == null) {
            throw new BadRequestException("Email not found");
        }

        // Kiểm tra xem userId từ DTO có khớp với userId của user không
        if (changePasswordDto.getUserId() != user.getId()) {
            throw new BadRequestException("Invalid user ID");
        }

        // Kiểm tra xem mật khẩu cũ có đúng không
        if (!JwtTokenUtil.comparePassword(changePasswordDto.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Incorrect old password");
        }

        // Kiểm tra xem mật khẩu mới có giống mật khẩu cũ không
        if (changePasswordDto.getNewPassword().equals(changePasswordDto.getOldPassword())) {
            throw new BadRequestException("New password must be different from the old password");
        }

        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getRePassword())) {
            throw new BadRequestException("New password and re-entered password must match");
        }

        // Cập nhật mật khẩu mới cho user
        user.setPassword(JwtTokenUtil.hashPassword(changePasswordDto.getNewPassword()));

        userRepository.save(user);

        return ResponseEntity.ok("Password changed successfully");
    }




}