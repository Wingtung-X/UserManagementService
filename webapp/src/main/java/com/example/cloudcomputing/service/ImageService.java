package com.example.cloudcomputing.service;

import com.example.cloudcomputing.exceptoin.NotVerifiedException;
import com.example.cloudcomputing.exceptoin.UnsupportedFileTypeException;
import com.example.cloudcomputing.exceptoin.UserNotFoundException;
import com.example.cloudcomputing.model.ImageMetadata;
import com.example.cloudcomputing.model.User;
import com.example.cloudcomputing.model.UserToken;
import com.example.cloudcomputing.repository.ImageMetadataRepository;
import com.example.cloudcomputing.repository.UserRepository;
import com.example.cloudcomputing.repository.UserTokenRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final ImageMetadataRepository imageMetadataRepository;
    private final UserRepository userRepository;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    // Timers for database operations and S3 operations
    private final Timer findUserByEmailTimer;
    private final Timer saveImageMetadataTimer;
    private final Timer findImageByUserIdTimer;
    private final Timer findImagesByFileNameTimer;
    private final Timer deleteImageMetadataTimer;
    private final Timer uploadToS3Timer;
    private final Timer deleteFromS3Timer;

    private final UserTokenRepository userTokenRepository;



//    @Autowired
//    public ImageService(ImageMetadataRepository imageMetadataRepository, UserRepository userRepository, S3Client s3Client, MeterRegistry meterRegistry) {
//        this.imageMetadataRepository = imageMetadataRepository;
//        this.userRepository = userRepository;
//        this.s3Client = s3Client;
//
//        // Initialize timers for database operations
//        this.findUserByEmailTimer = meterRegistry.timer("db.query.find_user_by_email.time");
//        this.saveImageMetadataTimer = meterRegistry.timer("db.query.save_image_metadata.time");
//        this.findImageByUserIdTimer = meterRegistry.timer("db.query.find_image_by_user_id.time");
//        this.findImagesByFileNameTimer = meterRegistry.timer("db.query.find_images_by_file_name.time");
//        this.deleteImageMetadataTimer = meterRegistry.timer("db.query.delete_image_metadata.time");
//
//        // Initialize timers for S3 operations
//        this.uploadToS3Timer = meterRegistry.timer("s3.operation.upload_image.time");
//        this.deleteFromS3Timer = meterRegistry.timer("s3.operation.delete_image.time");
//    }

    @Autowired
    public ImageService(ImageMetadataRepository imageMetadataRepository, UserRepository userRepository, S3Client s3Client, MeterRegistry meterRegistry, UserTokenRepository userTokenRepository) {
        this.imageMetadataRepository = imageMetadataRepository;
        this.userRepository = userRepository;
        this.userTokenRepository = userTokenRepository;
        this.s3Client = s3Client;

        // Initialize timers for database operations
        this.findUserByEmailTimer = meterRegistry.timer("db.query.find_user_by_email.time");
        this.saveImageMetadataTimer = meterRegistry.timer("db.query.save_image_metadata.time");
        this.findImageByUserIdTimer = meterRegistry.timer("db.query.find_image_by_user_id.time");
        this.findImagesByFileNameTimer = meterRegistry.timer("db.query.find_images_by_file_name.time");
        this.deleteImageMetadataTimer = meterRegistry.timer("db.query.delete_image_metadata.time");

        // Initialize timers for S3 operations
        this.uploadToS3Timer = meterRegistry.timer("s3.operation.upload_image.time");
        this.deleteFromS3Timer = meterRegistry.timer("s3.operation.delete_image.time");
    }

    @Transactional
    public ImageMetadata saveImageMetadata(MultipartFile file) {
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        logger.info("Searching user information for email: {}", email);

        // Record time for finding user by email
        Optional<User> mapUser = findUserByEmailTimer.record(() -> userRepository.findByEmail(email));

        if (!userTokenRepository.findByUserId(mapUser.get().getId()).get().getIsVerified()) {
            logger.error("User with email {} not verify", email);
            throw new NotVerifiedException("User with email {} not verify");
        }

        if (mapUser.isEmpty()) {
            logger.error("User with email {} not found", email);
            throw new UserNotFoundException(email);
        }
        User user = mapUser.get();

        // 检查是否已经有图片
        Optional<ImageMetadata> existingImage = findImageByUserIdTimer.record(() -> imageMetadataRepository.findByUserId(user.getId()));
        if (existingImage.isPresent()) {
            logger.error("User with email {} already has an image uploaded", email);
            throw new UnsupportedOperationException("User already has an image uploaded. Only one image per user is allowed.");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/jpg"))) {
            logger.error("User with email {} uploading invalid file type ", email);
            throw new UnsupportedFileTypeException("Only JPEG, PNG, and JPG files are allowed.");
        }

        // 上传图片到 S3
        String fileName = file.getOriginalFilename();
        String path = user.getId() + "/" + fileName;
        String s3Url = uploadToS3Timer.record(() -> uploadToS3(file, path)); // Record S3 upload time

        // 更新用户信息
        user.setAccount_updated(LocalDateTime.now());

        // 将图片元数据保存到数据库
        ImageMetadata metadata = new ImageMetadata(path, s3Url, user);
        saveImageMetadataTimer.record(() -> imageMetadataRepository.save(metadata)); // Record time for saving image metadata
        logger.info("Successfully uploaded image for email: {}", email);
        return metadata;
    }

    @Transactional
    public void deleteImage() {
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        logger.info("Searching user information for email: {}", email);

        // Record time for finding user by email
        Optional<User> mapUser = findUserByEmailTimer.record(() -> userRepository.findByEmail(email));

        if (mapUser.isEmpty()) {
            logger.error("User with email {} not found", email);
            throw new UserNotFoundException(email);
        }

        if (!userTokenRepository.findByUserId(mapUser.get().getId()).get().getIsVerified()) {
            logger.error("User with email {} not verify", email);
            throw new NotVerifiedException("User with email {} not verify");
        }

        User user = mapUser.get();

        // 检查用户是否有图片记录
        Optional<ImageMetadata> imageMetadataOpt = findImageByUserIdTimer.record(() -> imageMetadataRepository.findByUserId(user.getId()));
        if (imageMetadataOpt.isEmpty()) {
            logger.error("User with email {} does not have image", email);
            throw new UnsupportedOperationException("No image found for the user.");
        }
        ImageMetadata imageMetadata = imageMetadataOpt.get();

        deleteFromS3Timer.record(() -> deleteFromS3(imageMetadata.getFileName()));

        user.setImageMetadata(null);
        userRepository.save(user);

        deleteImageMetadataTimer.record(() -> imageMetadataRepository.delete(imageMetadata));
        logger.info("Successfully deleted image metadata and S3 file for user: {}", user.getEmail());

//        // 删除 S3 上的文件
//        deleteFromS3Timer.record(() -> deleteFromS3(imageMetadata.getFileName())); // Record S3 delete time
//        logger.info("Successfully deleted image for email: {}", email);
    }

    @Transactional
    public ImageMetadata getImage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        logger.info("Searching user information for email: {}", email);

        // Record time for finding user by email
        Optional<User> mapUser = findUserByEmailTimer.record(() -> userRepository.findByEmail(email));

        if (mapUser.isEmpty()) {
            logger.error("User with email {} not found", email);
            throw new UserNotFoundException(email);
        }

        if (!userTokenRepository.findByUserId(mapUser.get().getId()).get().getIsVerified()) {
            logger.error("User with email {} not verify", email);
            throw new NotVerifiedException("not verify");
        }
        User user = mapUser.get();

        // 检查用户是否有图片记录
        Optional<ImageMetadata> imageMetadataOpt = findImageByUserIdTimer.record(() -> imageMetadataRepository.findByUserId(user.getId()));
        if (imageMetadataOpt.isEmpty()) {
            logger.error("User with email {} does not have image", email);
            throw new UnsupportedOperationException("No image found for the user.");
        }
        logger.info("Get image information for user with email: {}", email);
        return imageMetadataOpt.get();
    }

    private String uploadToS3(MultipartFile file, String filepath) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filepath)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            logger.info("Uploading file to bucket: {}", bucketName);

            return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(filepath)).toExternalForm();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    private void deleteFromS3(String fileName) {
        try {
            s3Client.deleteObject(builder -> builder.bucket(bucketName).key(fileName).build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }
}
