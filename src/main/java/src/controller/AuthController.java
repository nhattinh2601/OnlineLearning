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

import java.util.*;

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

    private final Map<String, String> confirmationCodes = new HashMap<>();
    private final Map<String, Timer> confirmationTimers = new HashMap<>();

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

        if (changePasswordDto.getUserId() != user.getId()) {
            throw new BadRequestException("Invalid user ID");
        }

        if (!JwtTokenUtil.comparePassword(changePasswordDto.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Incorrect old password");
        }

        if (changePasswordDto.getNewPassword().equals(changePasswordDto.getOldPassword())) {
            throw new BadRequestException("New password must be different from the old password");
        }

        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getRePassword())) {
            throw new BadRequestException("New password and re-entered password must match");
        }

        user.setPassword(JwtTokenUtil.hashPassword(changePasswordDto.getNewPassword()));

        userRepository.save(user);

        return ResponseEntity.ok("Password changed successfully");
    }


    @PostMapping("/forgetpassword/sendotp/{mail}")
    public ResponseEntity<String> sendMessageMail(@PathVariable String mail) {
        User user = userRepository.findByEmail(mail);

        if (user == null) {
            return new ResponseEntity<>("Email not found in the database", HttpStatus.BAD_REQUEST);
        }

        // Tạo mã xác nhận (ví dụ: một chuỗi ngẫu nhiên)
        String confirmationCode = generateConfirmationCode();

        // Lưu mã xác nhận với email tương ứng
        confirmationCodes.put(mail, confirmationCode);

        // Gửi email với mã xác nhận
        mailService.sendOtpEmailForPassword(mail, confirmationCode);

        // Đặt hẹn giờ để xóa mã xác nhận sau 5 phút
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                confirmationCodes.remove(mail);
            }
        }, 5 * 60 * 1000);  // 5 phút

        // Lưu Timer để có thể hủy bỏ hẹn giờ nếu cần
        confirmationTimers.put(mail, timer);

        return new ResponseEntity<>("Successfully sent the confirmation email!", HttpStatus.OK);
    }

    private String generateConfirmationCode() {
        // Tạo mã xác nhận ngẫu nhiên, ví dụ, sử dụng một chuỗi ngẫu nhiên
        // (Trong thực tế, bạn có thể sử dụng cách bảo mật mạnh mẽ hơn)
        return String.valueOf(new Random().nextInt(899999) + 100000);
    }

    @PostMapping("/confirm-reset-password/{mail}")
    public ResponseEntity<String> confirmResetPassword(@PathVariable String mail, @RequestBody ConfirmResetDto confirmResetDto) {
        // Kiểm tra xem mã xác nhận có đúng không
        String storedCode = confirmationCodes.get(mail);
        if (storedCode == null || !storedCode.equals(confirmResetDto.getConfirmationCode())) {
            return new ResponseEntity<>("Invalid confirmation code", HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra thêm điều kiện khác nếu cần thiết (ví dụ: thời gian hết hạn)

        // Đặt lại mật khẩu
        User user = userRepository.findByEmail(mail);
        user.setPassword(JwtTokenUtil.hashPassword(confirmResetDto.getNewPassword()));
        userRepository.save(user);

        // Xóa mã xác nhận đã sử dụng và hủy bỏ hẹn giờ
        confirmationCodes.remove(mail);
        Timer timer = confirmationTimers.get(mail);
        if (timer != null) {
            timer.cancel();
        }

        return new ResponseEntity<>("Password reset successfully!", HttpStatus.OK);
    }





}