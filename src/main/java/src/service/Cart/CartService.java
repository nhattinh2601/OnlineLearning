package src.service.Cart;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import src.config.dto.PagedResultDto;
import src.config.dto.Pagination;
import src.config.exception.BadRequestException;
import src.config.exception.NotFoundException;
import src.config.utils.ApiQuery;
import src.model.Cart;
import src.model.Cart;
import src.model.OrderItem;
import src.model.Orders;
import src.repository.CartRepository;
import src.repository.OrderItemRepository;
import src.repository.OrdersRepository;
import src.service.Cart.Dto.CartDto;
import src.service.Cart.Dto.CartOrderDTO;
import src.service.Cart.Dto.CartUpdateDto;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrdersRepository ordersRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ModelMapper toDto;

    @PersistenceContext
    EntityManager em;

    @Async
    public CompletableFuture<List<CartDto>> getAll() {
        return CompletableFuture.completedFuture(
                cartRepository.findAll().stream().map(
                        x -> toDto.map(x, CartDto.class)
                ).collect(Collectors.toList()));
    }

    @Async
    public CompletableFuture<CartDto> getOne(int id) {
        Cart cart = cartRepository.findById(id).orElse(null);
        if (cart == null) {
            throw new NotFoundException("Không tìm thấy quyền với ID " + id);
        }
        return CompletableFuture.completedFuture(toDto.map(cart, CartDto.class));
    }

    @Async
    public CompletableFuture<CartDto> create(CartOrderDTO cartOrderDTO) {
        try {
            List<Cart> existCart = cartRepository.findCart(
                    cartOrderDTO.getUserId(), cartOrderDTO.getCourseId());
            List<Orders> existUser = ordersRepository.findUser(
                    cartOrderDTO.getUserId());
            List<OrderItem> existCourse = orderItemRepository.findCourse(
                    cartOrderDTO.getCourseId());


            if (!existCart.isEmpty()) {
                // Handle case where cart already exists
                throw new BadRequestException("Giỏ hàng chỉ tồn tại 1 khóa học");
            }
            if (!existUser.isEmpty() && !existCourse.isEmpty()) {
                // Both user and course exist, indicating the course has already been paid
                throw new BadRequestException("Khóa học đã được thanh toán");
            }

            Cart cart = new Cart();
            cart.setCourseId(cartOrderDTO.getCourseId());
            cart.setUserId(cartOrderDTO.getUserId());
            cartRepository.save(cart);

            // Assuming toDto is some kind of mapper, map the Cart to CourseDto
            CartDto cartDto = toDto.map(cart, CartDto.class);

            return CompletableFuture.completedFuture(cartDto);
        } catch (Exception e) {
            e.printStackTrace();

            return CompletableFuture.failedFuture(new RuntimeException("Thêm giỏ hàng thất bại"));
        }
    }

    /*@Async
    public CompletableFuture<CartDto> update(int id, CartUpdateDto carts) {
        Cart existingCart = cartRepository.findById(id).orElse(null);
        if (existingCart == null)
            throw new NotFoundException("Unable to find cart!");
        BeanUtils.copyProperties(carts, existingCart);
        existingCart.setUpdateAt(new Date(new java.util.Date().getTime()));
        return CompletableFuture.completedFuture(toDto.map(cartRepository.save(existingCart), CartDto.class));
    }*/
    public Cart updateCart(int cartId, Map<String, Object> fieldsToUpdate) {
        Optional<Cart> optionalCart = cartRepository.findById(cartId);

        if (optionalCart.isPresent()) {
            Cart cart = optionalCart.get();
            updateCartFields(cart, fieldsToUpdate);
            cart.setUpdateAt(new Date());
            cartRepository.save(cart);
            return cart;
        }

        return null;
    }

    private void updateCartFields(Cart cart, Map<String, Object> fieldsToUpdate) {
        for (Map.Entry<String, Object> entry : fieldsToUpdate.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            updateCartField(cart, fieldName, value);
        }
    }

    private void updateCartField(Cart cart, String fieldName, Object value) {
        switch (fieldName) {
            case "courseId":
                cart.setCourseId((int) value);
                break;
            case "userId":
                cart.setUserId((int) value);
                break;

            default:
                break;
        }
    }

    @Async
    public CompletableFuture<PagedResultDto<CartDto>> findAllPagination(HttpServletRequest request, Integer limit, Integer skip) {
        long total = cartRepository.count();
        Pagination pagination = Pagination.create(total, skip, limit);

        ApiQuery<Cart> features = new ApiQuery<>(request, em, Cart.class, pagination);
        return CompletableFuture.completedFuture(PagedResultDto.create(pagination,
                features.filter().orderBy().paginate().exec().stream().map(x -> toDto.map(x, CartDto.class)).toList()));
    }

    @Async
    public CompletableFuture<String> deleteById(int id) {
        Optional<Cart> cartOptional = cartRepository.findById(id);
        if (!cartOptional.isPresent()) {
            return CompletableFuture.completedFuture("Không có ID này");
        }
        try {
            Cart cart = cartOptional.get();
            cart.setIsDeleted(true);
            cart.setUpdateAt(new Date(new java.util.Date().getTime()));
            cartRepository.save(cart);
            return CompletableFuture.completedFuture("Đánh dấu xóa thành công");
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Xóa không được");
        }
    }

    public List<CartOrderDTO> getCartByUserId(int userId) {
        List<CartOrderDTO> result = new ArrayList<>();

        List<Cart> carts = cartRepository.findByUserId(userId);

        for (Cart cart : carts) {
            CartOrderDTO dto = new CartOrderDTO();
            dto.setId(cart.getId());
            dto.setCourseId(cart.getCourseByCourseId().getId());
            dto.setImage(cart.getCourseByCourseId().getImage());
            dto.setTeacher(cart.getUserByUserId().getFullname());
            dto.setPrice(cart.getCourseByCourseId().getPrice());
            dto.setPromotional_price(cart.getCourseByCourseId().getPromotional_price());
            dto.setIsDeleted(cart.getIsDeleted());
            dto.setTitle(cart.getCourseByCourseId().getTitle());

            result.add(dto);
        }

        return result;
    }
}
