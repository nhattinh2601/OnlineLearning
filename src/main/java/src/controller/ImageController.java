package src.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import src.config.annotation.ApiPrefixController;
import src.config.dto.SuccessResponseDto;
import src.config.exception.NotFoundException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@ApiPrefixController("cloud")
public class ImageController {
    @Autowired
    private Cloudinary cloudinary;

    @Async
    @PostMapping(value = "/images/upload", consumes = "multipart/form-data")
    public CompletableFuture<SuccessResponseDto<String>> uploadFileCloud(@RequestPart("file") MultipartFile file) throws IOException {
        // Kiểm tra nếu file là ảnh
        boolean isImage = file.getContentType().startsWith("image/");
        if (isImage) {
            // Đọc dữ liệu của file vào một mảng byte
            byte[] fileData = file.getBytes();
            // Tạo một đối tượng BufferedImage từ dữ liệu của file
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(fileData));
            // Thiết lập kích thước mới cho ảnh (full HD)
            int newWidth = 1920;
            int newHeight = 1080;
            // Kiểm tra kích thước của ảnh
            boolean isLargeImage = originalImage != null && originalImage.getWidth() > newWidth && originalImage.getHeight() > newHeight;
            // Nếu ảnh lớn hơn kích thước cho phép, resize ảnh
            if (isLargeImage) {
                // Tạo một đối tượng BufferedImage mới với kích thước mới và vẽ ảnh gốc lên đó
                BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
                Graphics2D g2d = resizedImage.createGraphics();
                g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
                g2d.dispose();

                // Chuyển đổi ảnh mới thành mảng byte
                ByteArrayOutputStream newImageBytes = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, "jpg", newImageBytes);
                fileData = newImageBytes.toByteArray();
            }
            // Upload file lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(fileData, ObjectUtils.emptyMap());
            // Trả về URL của file đã upload
            String fileUrl = (String) uploadResult.get("url");
            return CompletableFuture.completedFuture(new SuccessResponseDto<String>(fileUrl));
        } else {
            // Trường hợp file không phải là ảnh, upload file thẳng lên Cloudinary
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String fileUrl = ((String) uploadResult.get("url")).replace("http", "https");
            return CompletableFuture.completedFuture(new SuccessResponseDto<String>(fileUrl));
        }
    }

    @Async
    @PostMapping(value = "/videos/upload", consumes = "multipart/form-data")
    public CompletableFuture<SuccessResponseDto<String>> uploadVideoCloud(@RequestPart("file") MultipartFile file) throws IOException {
        // Đọc dữ liệu của file vào một mảng byte
        byte[] fileData = file.getBytes();

        // Tạo đối tượng Cloudinary
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dqptxftlv",
                "api_key", "268148952558612",
                "api_secret", "8gzmcO9n4yChRpHfXAr-8-T6ZXQ"
        ));

        // Upload video lên Cloudinary
        Map<String, Object> uploadResult = cloudinary.uploader().upload(fileData, ObjectUtils.asMap(
                "resource_type", "video"
        ));

        // Trả về URL của video đã upload
        String videoUrl = (String) uploadResult.get("url");
        return CompletableFuture.completedFuture(new SuccessResponseDto<String>(videoUrl));
    }

    @Async
    @DeleteMapping("/images/{publicId}")
    public CompletableFuture<SuccessResponseDto<String>> deleteFileCloud(@PathVariable String publicId) {
        try {
            if (publicId == null || publicId.trim() == "")
                throw new NotFoundException("Not found publicId");
            cloudinary.api().deleteResources(List.of(publicId), ObjectUtils.emptyMap());
            return CompletableFuture.completedFuture(new SuccessResponseDto<String>("success"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

   /* @Async
    @PostMapping(value = "/local/upload", consumes = "multipart/form-data")
    public CompletableFuture<SuccessResponseDto<String>> uploadFileLocal(HttpServletRequest request, @RequestPart("file") MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");
        String fileName = System.currentTimeMillis() + "-" + UUID.randomUUID() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
        // Kiểm tra nếu file là ảnh
        boolean isImage = file.getContentType().startsWith("image/");
        if (isImage) {
            // Đọc dữ liệu của file vào một mảng byte
            byte[] fileData = file.getBytes();
            // Tạo một đối tượng BufferedImage từ dữ liệu của file
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(fileData));
            // Thiết lập kích thước mới cho ảnh (full HD)
            int newWidth = 1920;
            int newHeight = 1080;
            // Kiểm tra kích thước của ảnh
            boolean isLargeImage = originalImage != null && originalImage.getWidth() > newWidth && originalImage.getHeight() > newHeight;
            // Nếu ảnh lớn hơn kích thước cho phép, resize ảnh
            if (isLargeImage) {
                // Tạo một đối tượng BufferedImage mới với kích thước mới và vẽ ảnh gốc lên đó
                BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
                Graphics2D g2d = resizedImage.createGraphics();
                g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
                g2d.dispose();

                // Chuyển đổi ảnh mới thành mảng byte
                ByteArrayOutputStream newImageBytes = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, "jpg", newImageBytes);
                fileData = newImageBytes.toByteArray();
            }
            // Upload file lên Cloudinary
            // Trả về URL của file đã upload

            Path filePath = Paths.get(uploadPath.toString(), fileName);
            Files.write(filePath, fileData);
            return CompletableFuture.completedFuture(new SuccessResponseDto<String>((request.getRemoteAddr().equalsIgnoreCase("0:0:0:0:0:0:0:1") ? "localhost" : request.getRemoteAddr()) + ":" + request.getLocalPort() + "/uploads/" + fileName));
        } else {
            // Trường hợp file không phải là ảnh, upload file thẳng lên Cloudinary
            Path filePath = Paths.get(uploadPath.toString(), fileName);
            Files.write(filePath, file.getBytes());
            return CompletableFuture.completedFuture(new SuccessResponseDto<String>((request.getRemoteAddr().equalsIgnoreCase("0:0:0:0:0:0:0:1") ? "localhost" : request.getRemoteAddr()) + ":" + request.getLocalPort() + "/uploads/" + fileName));
        }
    }*/

    /*@Async
    @DeleteMapping("/local/{publicId}")
    public CompletableFuture<SuccessResponseDto<String>> deleteFileLocal(@PathVariable String publicId) {
        try {
            if (publicId == null || publicId.trim().equals(""))
                throw new NotFoundException("Not found publicId");
            File file = new File(System.getProperty("user.dir"), "uploads/" + publicId);
            if (file.delete()) {
                return CompletableFuture.completedFuture(new SuccessResponseDto<String>("success"));
            } else {
                throw new RuntimeException("Some thing went wrong!");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/


}

