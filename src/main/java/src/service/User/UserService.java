

package src.service.User;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import src.config.auth.JwtTokenUtil;
import src.config.dto.PagedResultDto;

import src.config.dto.Pagination;
import src.config.exception.NotFoundException;
import src.config.utils.ApiQuery;
import src.model.Cart;
import src.model.Rating;
import src.model.Role;
import src.model.User;
import src.repository.CartRepository;
import src.repository.RoleRepository;
import src.repository.UserRepository;
import src.service.Rating.Dto.RatingDto;
import src.service.Role.Dto.RoleCreateDto;
import src.service.Role.Dto.RoleDto;
import src.service.User.Dto.UserCreateDto;
import src.service.User.Dto.UserDto;
import src.service.User.Dto.UserUpdateDto;
import src.service.User.IUserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
    EntityManager em;
    private UserRepository userRepository;
    private CartRepository cartRepository;
    private ModelMapper toDto;
    private RoleRepository roleRepository;
    int roleId;

    @Autowired
    public UserService(UserRepository userRepository, ModelMapper toDto, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.toDto = toDto;
        this.roleRepository = roleRepository;
    }

    @Async
    public CompletableFuture<List<UserDto>> getAll() {
        return CompletableFuture.completedFuture(
                userRepository.findAll().stream().map(
                        x -> toDto.map(x, UserDto.class)
                ).collect(Collectors.toList()));
    }

    @Async
    public CompletableFuture<UserDto> getOne(int id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new NotFoundException("Không tìm thấy người dùng với ID " + id);
        }
        return CompletableFuture.completedFuture(toDto.map(user, UserDto.class));
    }

   @Async
    public CompletableFuture<UserDto> create(UserCreateDto input) {
        roleId = roleRepository.findByName("user").orElse(null).getId();
        input.setPassword(JwtTokenUtil.hashPassword(input.getPassword()));
        input.setRoleId(roleId);
        User user = userRepository.save(toDto.map(input, User.class));
        // tao cart
        /*cartRepository.save(new Cart(user.getId()));*/
        toDto.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return CompletableFuture.completedFuture(toDto.map(user, UserDto.class));
    }

   /* @Async
    public CompletableFuture<UserDto> create(UserCreateDto input) {
        User user = new User();

        user.setFullname(input.getFullname());
        user.setEmail(input.getEmail());
        user.setPhone(input.getPhone());
        user.setAvatar(input.getAvatar());
        user.setDescription(input.getDescription());
        user.setBank_name(input.getBank_name());
        user.setAccount_number(input.getAccount_number());
        user.setAccount_name(input.getAccount_name());
        user.setPassword(input.getPassword());
        user.setRoleId(input.getRoleId());

        User savedUser = userRepository.save(user);
        return CompletableFuture.completedFuture(toDto.map(savedUser, UserDto.class));
    }*/


    @Async
    public CompletableFuture<UserDto> update(int id, UserUpdateDto user) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null)
            throw new ResponseStatusException(NOT_FOUND, "Không tìm thấy người dùng này");
        BeanUtils.copyProperties(user, existingUser);
        return CompletableFuture.completedFuture(toDto.map(userRepository.save(existingUser), UserDto.class));
    }
    
    @Async
    public CompletableFuture<PagedResultDto<UserDto>> findAllPagination(HttpServletRequest request, Integer limit, Integer skip) {
        long total = userRepository.count();
        Pagination pagination = Pagination.create(total, skip, limit);
        ApiQuery<User> features = new ApiQuery<>(request, em, User.class, pagination);
        return CompletableFuture.completedFuture(PagedResultDto.create(pagination,
                features.filter().orderBy().paginate().exec().stream().map(x -> toDto.map(x, UserDto.class)).toList()));
    }

    @Async
    public CompletableFuture<String> deleteById(int id) {
        Optional<User> userOptional = userRepository.findById(id);

        if (!userOptional.isPresent()) {
            return CompletableFuture.completedFuture("Không có ID này");
        }

        try {
            User user = userOptional.get();
            user.setIsDeleted(true);
            userRepository.save(user);
            return CompletableFuture.completedFuture("Đánh dấu xóa thành công");
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Xóa không được");
        }
    }

    @Async
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        request.setAttribute("id", user.getId());
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        } else {
            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(user.getRoleByRoleId().getName()));
            return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
        }
    }



}

