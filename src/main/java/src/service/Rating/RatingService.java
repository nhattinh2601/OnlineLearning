package src.service.Rating;

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
import src.model.Rating;
import src.repository.RatingRepository;
import src.service.Rating.Dto.RatingCreateDto;
import src.service.Rating.Dto.RatingDto;
import src.service.Rating.Dto.RatingUpdateDto;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class RatingService {
    @Autowired
    private RatingRepository ratingRepository;
    @Autowired
    private ModelMapper toDto;

    @PersistenceContext
    EntityManager em;

    @Async
    public CompletableFuture<List<RatingDto>> getAll() {
        return CompletableFuture.completedFuture(
                ratingRepository.findAll().stream().map(
                        x -> toDto.map(x, RatingDto.class)
                ).collect(Collectors.toList()));
    }

    @Async
    public CompletableFuture<RatingDto> getOne(int id) {
        Rating rating = ratingRepository.findById(id).orElse(null);
        if (rating == null) {
            throw new NotFoundException("Không tìm thấy quyền với ID " + id);
        }
        return CompletableFuture.completedFuture(toDto.map(rating, RatingDto.class));
    }

    @Async
    public CompletableFuture<RatingDto> create(RatingCreateDto input) {
        Rating rating = new Rating();
        rating.setRating(input.getRating());
        rating.setCourseId(input.getCourseId());
        rating.setUserId(input.getUserId());

        Rating savedRating = ratingRepository.save(rating);
        return CompletableFuture.completedFuture(toDto.map(savedRating, RatingDto.class));
    }

    @Async
    public CompletableFuture<RatingDto> update(int id, RatingUpdateDto ratings) {
        Rating existingRating = ratingRepository.findById(id).orElse(null);
        if (existingRating == null)
            throw new NotFoundException("Unable to find rating!");
        BeanUtils.copyProperties(ratings, existingRating);
        existingRating.setUpdateAt(new Date(new java.util.Date().getTime()));
        return CompletableFuture.completedFuture(toDto.map(ratingRepository.save(existingRating), RatingDto.class));
    }

    @Async
    public CompletableFuture<PagedResultDto<RatingDto>> findAllPagination(HttpServletRequest request, Integer limit, Integer skip) {
        long total = ratingRepository.count();
        Pagination pagination = Pagination.create(total, skip, limit);

        ApiQuery<Rating> features = new ApiQuery<>(request, em, Rating.class, pagination);
        return CompletableFuture.completedFuture(PagedResultDto.create(pagination,
                features.filter().orderBy().paginate().exec().stream().map(x -> toDto.map(x, RatingDto.class)).toList()));
    }

    @Async
    public CompletableFuture<String> deleteById(int id) {
        Optional<Rating> ratingOptional = ratingRepository.findById(id);
        if (!ratingOptional.isPresent()) {
            return CompletableFuture.completedFuture("Không có ID này");
        }
        try {
            Rating rating = ratingOptional.get();
            rating.setIsDeleted(true);
            rating.setUpdateAt(new Date(new java.util.Date().getTime()));
            ratingRepository.save(rating);
            return CompletableFuture.completedFuture("Đánh dấu xóa thành công");
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Xóa không được");
        }
    }
}
