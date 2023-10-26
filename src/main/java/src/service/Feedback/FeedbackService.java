package src.service.Feedback;

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
import src.model.Feedback;
import src.repository.FeedbackRepository;
import src.service.Feedback.Dto.FeedbackCreateDto;
import src.service.Feedback.Dto.FeedbackDto;
import src.service.Feedback.Dto.FeedbackUpdateDto;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class FeedbackService {
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private ModelMapper toDto;

    @PersistenceContext
    EntityManager em;

    @Async
    public CompletableFuture<List<FeedbackDto>> getAll() {
        return CompletableFuture.completedFuture(
                feedbackRepository.findAll().stream().map(
                        x -> toDto.map(x, FeedbackDto.class)
                ).collect(Collectors.toList()));
    }

    @Async
    public CompletableFuture<FeedbackDto> getOne(int id) {
        Feedback feedback = feedbackRepository.findById(id).orElse(null);
        if (feedback == null) {
            throw new NotFoundException("Không tìm thấy quyền với ID " + id);
        }
        return CompletableFuture.completedFuture(toDto.map(feedback, FeedbackDto.class));
    }

    @Async
    public CompletableFuture<FeedbackDto> create(FeedbackCreateDto input) {
        Feedback feedback = new Feedback();
        feedback.setTitle(input.getTitle());
        feedback.setContent(input.getContent());
        feedback.setImage(input.getImage());
        feedback.setUserId(input.getUserId());

        Feedback savedFeedback = feedbackRepository.save(feedback);
        return CompletableFuture.completedFuture(toDto.map(savedFeedback, FeedbackDto.class));
    }

    @Async
    public CompletableFuture<FeedbackDto> update(int id, FeedbackUpdateDto feedbacks) {
        Feedback existingFeedback = feedbackRepository.findById(id).orElse(null);
        if (existingFeedback == null)
            throw new NotFoundException("Unable to find feedback!");
        BeanUtils.copyProperties(feedbacks, existingFeedback);
        existingFeedback.setUpdateAt(new Date(new java.util.Date().getTime()));
        return CompletableFuture.completedFuture(toDto.map(feedbackRepository.save(existingFeedback), FeedbackDto.class));
    }

    @Async
    public CompletableFuture<PagedResultDto<FeedbackDto>> findAllPagination(HttpServletRequest request, Integer limit, Integer skip) {
        long total = feedbackRepository.count();
        Pagination pagination = Pagination.create(total, skip, limit);

        ApiQuery<Feedback> features = new ApiQuery<>(request, em, Feedback.class, pagination);
        return CompletableFuture.completedFuture(PagedResultDto.create(pagination,
                features.filter().orderBy().paginate().exec().stream().map(x -> toDto.map(x, FeedbackDto.class)).toList()));
    }

    @Async
    public CompletableFuture<String> deleteById(int id) {
        Optional<Feedback> feedbackOptional = feedbackRepository.findById(id);
        if (!feedbackOptional.isPresent()) {
            return CompletableFuture.completedFuture("Không có ID này");
        }
        try {
            Feedback feedback = feedbackOptional.get();
            feedback.setIsDeleted(true);
            feedback.setUpdateAt(new Date(new java.util.Date().getTime()));
            feedbackRepository.save(feedback);
            return CompletableFuture.completedFuture("Đánh dấu xóa thành công");
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Xóa không được");
        }
    }
    
}
