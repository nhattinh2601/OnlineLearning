package src.service.Review;

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
import src.config.exception.NotFoundException;
import src.config.utils.ApiQuery;
import src.model.Review;
import src.repository.ReviewRepository;
import src.service.Review.Dto.ReviewCreateDto;
import src.service.Review.Dto.ReviewDto;
import src.service.Review.Dto.ReviewUpdateDto;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ModelMapper toDto;

    @PersistenceContext
    EntityManager em;

    @Async
    public CompletableFuture<List<ReviewDto>> getAll() {
        return CompletableFuture.completedFuture(
                reviewRepository.findAll().stream().map(
                        x -> toDto.map(x, ReviewDto.class)
                ).collect(Collectors.toList()));
    }

    @Async
    public CompletableFuture<ReviewDto> getOne(int id) {
        Review review = reviewRepository.findById(id).orElse(null);
        if (review == null) {
            throw new NotFoundException("Không tìm thấy quyền với ID " + id);
        }
        return CompletableFuture.completedFuture(toDto.map(review, ReviewDto.class));
    }

    @Async
    public CompletableFuture<ReviewDto> create(ReviewCreateDto input) {
        Review review = new Review();
        review.setCourseId(input.getCourseId());
        review.setUserId(input.getUserId());

        Review savedReview = reviewRepository.save(review);
        return CompletableFuture.completedFuture(toDto.map(savedReview, ReviewDto.class));
    }

    @Async
    public CompletableFuture<ReviewDto> update(int id, ReviewUpdateDto reviews) {
        Review existingReview = reviewRepository.findById(id).orElse(null);
        if (existingReview == null)
            throw new NotFoundException("Unable to find review!");
        BeanUtils.copyProperties(reviews, existingReview);
        existingReview.setUpdateAt(new Date(new java.util.Date().getTime()));
        return CompletableFuture.completedFuture(toDto.map(reviewRepository.save(existingReview), ReviewDto.class));
    }

    @Async
    public CompletableFuture<PagedResultDto<ReviewDto>> findAllPagination(HttpServletRequest request, Integer limit, Integer skip) {
        long total = reviewRepository.count();
        Pagination pagination = Pagination.create(total, skip, limit);

        ApiQuery<Review> features = new ApiQuery<>(request, em, Review.class, pagination);
        return CompletableFuture.completedFuture(PagedResultDto.create(pagination,
                features.filter().orderBy().paginate().exec().stream().map(x -> toDto.map(x, ReviewDto.class)).toList()));
    }

    @Async
    public CompletableFuture<String> deleteById(int id) {
        Optional<Review> reviewOptional = reviewRepository.findById(id);
        if (!reviewOptional.isPresent()) {
            return CompletableFuture.completedFuture("Không có ID này");
        }
        try {
            Review review = reviewOptional.get();
            review.setIsDeleted(true);
            review.setUpdateAt(new Date(new java.util.Date().getTime()));
            reviewRepository.save(review);
            return CompletableFuture.completedFuture("Đánh dấu xóa thành công");
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Xóa không được");
        }
    }
}