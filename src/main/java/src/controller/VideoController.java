package src.controller;

import com.cloudinary.Cloudinary;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import src.config.annotation.ApiPrefixController;
import src.config.dto.PagedResultDto;
import src.model.Video;
import src.model.Video;
import src.service.Video.VideoService;
import src.service.Video.Dto.VideoCreateDto;
import src.service.Video.Dto.VideoDto;
import src.service.Video.Dto.VideoUpdateDto;

import java.util.List;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@ApiPrefixController(value = "/videos")
public class VideoController {
    @Autowired
    private VideoService videoService;
    @Autowired
    private Cloudinary cloudinary;

    @GetMapping()
    public CompletableFuture<List<VideoDto>> findAll() {
        return videoService.getAll();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<VideoDto> getOne(@PathVariable int id) {
        return videoService.getOne(id);
    }

    @GetMapping("/pagination")
    public CompletableFuture<PagedResultDto<VideoDto>> findAllPagination(HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") Integer page ,
                                                                            @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                            @RequestParam(required = false, defaultValue = "createAt") String orderBy) {
        return videoService.findAllPagination(request, size, page * size);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompletableFuture<Video>> createVideo(@RequestBody VideoCreateDto videoCreateDto) {
        CompletableFuture<Video> future = videoService.createVideo(videoCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(future);
    }

    /*@PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<VideoDto> update(@PathVariable int id, VideoUpdateDto video) {
        return videoService.update(id, video);
    }*/

    @PatchMapping("/{videoId}")
    public ResponseEntity<Video> updateVideoField(
            @PathVariable int videoId,
            @RequestBody Map<String, Object> fieldsToUpdate) {

        Video updatedVideo = videoService.updateVideo(videoId, fieldsToUpdate);

        if (updatedVideo != null) {
            return ResponseEntity.ok(updatedVideo);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<String> deleteById(@PathVariable int id) {
        return videoService.deleteById(id);
    }

    @GetMapping("/course={courseId}")
    public CompletableFuture<List<VideoDto>> findByCourseId(@PathVariable int courseId) {
        return videoService.findByCourseId(courseId);
    }


    @GetMapping("/countByCourse/{courseId}")
    public int countVideosByCourseId(@PathVariable int courseId) {
        return videoService.countVideosByCourseId(courseId);
    }




}


