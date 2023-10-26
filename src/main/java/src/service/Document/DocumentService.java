package src.service.Document;


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
import src.model.Course;
import src.model.Document;
import src.model.Video;
import src.repository.DocumentRepository;
import src.service.Course.Dto.CourseDto;
import src.service.Document.Dto.DocumentCreateDto;
import src.service.Document.Dto.DocumentDto;
import src.service.Document.Dto.DocumentUpdateDto;
import src.service.Video.Dto.VideoDto;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class DocumentService {
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private ModelMapper toDto;
    @PersistenceContext
    EntityManager em;
    @Async
    public CompletableFuture<List<DocumentDto>> getAll() {
        return CompletableFuture.completedFuture(
                documentRepository.findAll().stream().map(
                        x -> toDto.map(x, DocumentDto.class)
                ).collect(Collectors.toList()));
    }
    @Async
    public CompletableFuture<DocumentDto> getOne(int id) {
        Document document = documentRepository.findById(id).orElse(null);
        if (document == null) {
            throw new NotFoundException("Không tìm thấy quyền với ID " + id);
        }
        return CompletableFuture.completedFuture(toDto.map(document, DocumentDto.class));
    }


    @Async
    public CompletableFuture<DocumentDto> create(DocumentCreateDto input) {
        Document document = new Document();
        document.setFile_path(input.getFile_path());
        document.setTitle(input.getTitle());
        document.setCourseId(input.getCourseId());

        Document savedDocument = documentRepository.save(document);
        return CompletableFuture.completedFuture(toDto.map(savedDocument, DocumentDto.class));
    }

    @Async
    public CompletableFuture<DocumentDto> update(int id, DocumentUpdateDto documents) {
        Document existingDocument = documentRepository.findById(id).orElse(null);
        if (existingDocument == null)
            throw new NotFoundException("Unable to find document!");
        BeanUtils.copyProperties(documents, existingDocument);
        return CompletableFuture.completedFuture(toDto.map(documentRepository.save(existingDocument), DocumentDto.class));
    }

    @Async
    public CompletableFuture<String> deleteById(int id) {
        Optional<Document> documentOptional = documentRepository.findById(id);
        if (!documentOptional.isPresent()) {
            return CompletableFuture.completedFuture("Không có ID này");
        }
        try {
            Document document = documentOptional.get();
            document.setIsDeleted(true);
            document.setUpdateAt(new Date(new java.util.Date().getTime()));
            documentRepository.save(document);
            return CompletableFuture.completedFuture("Đánh dấu xóa thành công");
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Xóa không được");
        }
    }
    @Async
    public CompletableFuture<PagedResultDto<DocumentDto>> findAllPagination(HttpServletRequest request, Integer limit, Integer skip) {
        long total = documentRepository.count();
        Pagination pagination = Pagination.create(total, skip, limit);

        ApiQuery<Video> features = new ApiQuery<>(request, em, Video.class, pagination);
        return CompletableFuture.completedFuture(PagedResultDto.create(pagination,
                features.filter().orderBy().paginate().exec().stream().map(x -> toDto.map(x, DocumentDto.class)).toList()));
    }

}
