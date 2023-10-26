package src.service.Video;

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
import src.model.Video;
import src.repository.VideoRepository;
import src.service.Video.Dto.VideoCreateDto;
import src.service.Video.Dto.VideoDto;
import src.service.Video.Dto.VideoUpdateDto;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class VideoService {
    private static final String APPLICATION_NAME = "Your Application Name";
    private static final String SERVICE_ACCOUNT_EMAIL = "your-service-account-email@your-project.iam.gserviceaccount.com";
    private static final String P12_FILE_PATH = "path/to/your/p12/file.p12";
    private static final String ROOT_FOLDER_ID = "root";
    @Autowired
    private VideoRepository videoRepository;
    @Autowired
    private ModelMapper toDto;

    @PersistenceContext
    EntityManager em;

    @Async
    public CompletableFuture<List<VideoDto>> getAll() {
        return CompletableFuture.completedFuture(
                videoRepository.findAll().stream().map(
                        x -> toDto.map(x, VideoDto.class)
                ).collect(Collectors.toList()));
    }

    @Async
    public CompletableFuture<VideoDto> getOne(int id) {
        Video video = videoRepository.findById(id).orElse(null);
        if (video == null) {
            throw new NotFoundException("Không tìm thấy quyền với ID " + id);
        }
        return CompletableFuture.completedFuture(toDto.map(video, VideoDto.class));
    }

    public CompletableFuture<Video> createVideo(VideoCreateDto videoCreateDto) {
        CompletableFuture<Video> future = new CompletableFuture<>();

        Video newVideo = new Video();
        newVideo.setVideo_filepath(videoCreateDto.getVideo_filepath());
        newVideo.setDescription(videoCreateDto.getDescription());
        newVideo.setTitle(videoCreateDto.getTitle());
        newVideo.setImage(videoCreateDto.getImage());
        newVideo.setCourseId(videoCreateDto.getCourseId());

        Video savedVideo = videoRepository.save(newVideo);
        future.complete(savedVideo);

        return future;
    }

    @Async
    public CompletableFuture<VideoDto> update(int id, VideoUpdateDto videos) {
        Video existingVideo = videoRepository.findById(id).orElse(null);
        if (existingVideo == null)
            throw new NotFoundException("Unable to find video!");
        BeanUtils.copyProperties(videos, existingVideo);
        existingVideo.setUpdateAt(new Date(new java.util.Date().getTime()));
        return CompletableFuture.completedFuture(toDto.map(videoRepository.save(existingVideo), VideoDto.class));
    }

    @Async
    public CompletableFuture<PagedResultDto<VideoDto>> findAllPagination(HttpServletRequest request, Integer limit, Integer skip) {
        long total = videoRepository.count();
        Pagination pagination = Pagination.create(total, skip, limit);

        ApiQuery<Video> features = new ApiQuery<>(request, em, Video.class, pagination);
        return CompletableFuture.completedFuture(PagedResultDto.create(pagination,
                features.filter().orderBy().paginate().exec().stream().map(x -> toDto.map(x, VideoDto.class)).toList()));
    }

    @Async
    public CompletableFuture<String> deleteById(int id) {
        Optional<Video> videoOptional = videoRepository.findById(id);
        if (!videoOptional.isPresent()) {
            return CompletableFuture.completedFuture("Không có ID này");
        }
        try {
            Video video = videoOptional.get();
            video.setIsDeleted(true);
            video.setUpdateAt(new Date(new java.util.Date().getTime()));
            videoRepository.save(video);
            return CompletableFuture.completedFuture("Đánh dấu xóa thành công");
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Xóa không được");
        }
    }

}

