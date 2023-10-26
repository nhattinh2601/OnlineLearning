package src.controller.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import src.config.annotation.ApiPrefixController;
import src.config.auth.JwtTokenUtil;
import src.config.exception.BadRequestException;
import src.model.User;
import src.repository.UserRepository;
import src.service.User.Dto.UserProfileDto;

@RestController
@ApiPrefixController("/auth")
@Tag(name = "User authentication")
public class AuthController {

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

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

}