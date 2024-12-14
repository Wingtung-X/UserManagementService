package com.example.cloudcomputing.controller;

import com.example.cloudcomputing.exceptoin.NotVerifiedException;
import com.example.cloudcomputing.exceptoin.UnsupportedFileTypeException;
import com.example.cloudcomputing.exceptoin.UserNotFoundException;
import com.example.cloudcomputing.model.ImageMetadata;
import com.example.cloudcomputing.model.User;
import com.example.cloudcomputing.service.ImageService;
import com.example.cloudcomputing.service.UserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping
public class ImageController {

    private final ImageService imageService;
    private final Counter uploadImageCounter;
    private final Counter deleteImageCounter;
    private final Counter getImageCounter;
    private final Timer uploadImageTimer;
    private final Timer deleteImageTimer;
    private final Timer getImageTimer;

    @Autowired
    public ImageController(ImageService imageService, MeterRegistry meterRegistry) {
        this.imageService = imageService;

        // Initialize counters
        this.uploadImageCounter = meterRegistry.counter("api.v1.user.self.pic.upload.count");
        this.deleteImageCounter = meterRegistry.counter("api.v1.user.self.pic.delete.count");
        this.getImageCounter = meterRegistry.counter("api.v1.user.self.pic.get.count");

        // Initialize timers
        this.uploadImageTimer = meterRegistry.timer("api.v1.user.self.pic.upload.time");
        this.deleteImageTimer = meterRegistry.timer("api.v1.user.self.pic.delete.time");
        this.getImageTimer = meterRegistry.timer("api.v1.user.self.pic.get.time");
    }

    @PostMapping("/v1/user/self/pic")
    public ResponseEntity<?> uploadImage(HttpServletRequest request) {
        uploadImageCounter.increment(); // Count the number of times this API is called
        long startTime = System.currentTimeMillis();

        try {
            String contentType = request.getContentType();
            if (contentType == null || !contentType.startsWith("multipart/form-data")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            MultipartFile file = ((MultipartHttpServletRequest) request).getFile("file");
            if (file == null || file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            ImageMetadata metadata = imageService.saveImageMetadata(file);

            Map<String, Object> response = new HashMap<>();
            response.put("file_name", metadata.getFileName());
            response.put("id", metadata.getId());
            response.put("url", metadata.getUrl());
            response.put("upload_date", metadata.getUploadDate());
            response.put("user_id", metadata.getUser().getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        catch (NotVerifiedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        catch (IllegalArgumentException | UserNotFoundException | UnsupportedFileTypeException | UnsupportedOperationException | MultipartException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } finally {
            uploadImageTimer.record(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS); // Record time taken
        }
    }

    @DeleteMapping("/v1/user/self/pic")
    public ResponseEntity<Void> deleteProfilePic() {
        deleteImageCounter.increment(); // Count the number of times this API is called
        long startTime = System.currentTimeMillis();

        try {
            imageService.deleteImage();
            return ResponseEntity.noContent().build(); // 返回 204 No Content
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        catch (NotVerifiedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        finally {
            deleteImageTimer.record(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS); // Record time taken
        }
    }

    @GetMapping("/v1/user/self/pic")
    public ResponseEntity<?> getProfilePic() {
        getImageCounter.increment(); // Count the number of times this API is called
        long startTime = System.currentTimeMillis();

        try {
            ImageMetadata metadata = imageService.getImage();

            Map<String, Object> response = new HashMap<>();
            response.put("file_name", metadata.getFileName());
            response.put("id", metadata.getId());
            response.put("url", metadata.getUrl());
            response.put("upload_date", metadata.getUploadDate());
            response.put("user_id", metadata.getUser().getId());

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        catch (NotVerifiedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } finally {
            getImageTimer.record(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS); // Record time taken
        }
    }
}
