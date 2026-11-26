
package com.mercedes.telemetry.service;

import com.mercedes.telemetry.dto.ProfilePhotoFile;
import com.mercedes.telemetry.dto.ProfilePhotoResponse;
import com.mercedes.telemetry.entity.User;
import com.mercedes.telemetry.entity.UserProfilePhoto;
import com.mercedes.telemetry.repository.UserProfilePhotoRepository;
import com.mercedes.telemetry.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

@Service
public class ProfilePhotoService {

    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;

    private static final int MAX_IMAGE_DIMENSION = 4096;

    private static final long MAX_IMAGE_PIXELS = 16_000_000L;

    private final UserRepository userRepository;

    private final UserProfilePhotoRepository photoRepository;

    public ProfilePhotoService(
            UserRepository userRepository,
            UserProfilePhotoRepository photoRepository) {

        this.userRepository = userRepository;
        this.photoRepository = photoRepository;
    }

    // =====================================================
    // UPLOAD OR REPLACE PROFILE PHOTO
    // =====================================================

    @Transactional
    public ProfilePhotoResponse uploadPhoto(
            String authenticatedEmail,
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select a profile photo."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "Profile photo cannot exceed 5 MB."
            );
        }

        final byte[] imageBytes;

        try {
            imageBytes = file.getBytes();
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to read the uploaded image.",
                    exception
            );
        }

        if (imageBytes.length == 0 || imageBytes.length > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid profile photo size."
            );
        }

        // Validate the actual image bytes before touching the
        // existing database record.
        String detectedContentType = detectAndValidateImage(imageBytes);

        String fileName = sanitizeFileName(file.getOriginalFilename());

        String extension = getExtension(fileName);

        validateExtension(extension, detectedContentType);

        validateProvidedContentType(
                file.getContentType(),
                detectedContentType
        );

        // Find the authenticated user.
        User user = userRepository
                .findByEmailIgnoreCase(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User account not found."
                ));

        // Find the existing photo or create a new record.
        // Existing entity is updated instead of inserting another row.
        UserProfilePhoto photo = photoRepository
                .findByUser_Id(user.getId())
                .orElseGet(UserProfilePhoto::new);

        // Assign all new values only after validation succeeds.
        photo.setUser(user);
        photo.setFileName(fileName);
        photo.setContentType(detectedContentType);
        photo.setFileExtension(extension);
        photo.setFileSize((long) imageBytes.length);
        photo.setPhotoData(imageBytes);

        // Flush ensures database constraints are checked
        // while this transactional operation is executing.
        UserProfilePhoto savedPhoto = photoRepository.saveAndFlush(photo);

        return toResponse(savedPhoto);
    }

    // =====================================================
    // GET PROFILE PHOTO
    // =====================================================

    @Transactional(readOnly = true)
    public ProfilePhotoFile getPhoto(String authenticatedEmail) {

        User user = findUser(authenticatedEmail);

        UserProfilePhoto photo = photoRepository
                .findByUser_Id(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profile photo not found."
                ));

        return new ProfilePhotoFile(
                photo.getPhotoData(),
                photo.getFileName(),
                photo.getContentType()
        );
    }

    // =====================================================
    // GET PROFILE PHOTO METADATA
    // =====================================================

    @Transactional(readOnly = true)
    public ProfilePhotoResponse getPhotoMetadata(
            String authenticatedEmail) {

        User user = findUser(authenticatedEmail);

        UserProfilePhoto photo = photoRepository
                .findByUser_Id(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profile photo not found."
                ));

        return toResponse(photo);
    }

    // =====================================================
    // DELETE PROFILE PHOTO
    // =====================================================

    @Transactional
    public void deletePhoto(String authenticatedEmail) {

        User user = findUser(authenticatedEmail);

        UserProfilePhoto photo = photoRepository
                .findByUser_Id(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profile photo not found."
                ));

        photoRepository.delete(photo);
        photoRepository.flush();
    }

    // =====================================================
    // FIND AUTHENTICATED USER
    // =====================================================

    private User findUser(String email) {

        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User account not found."
                ));
    }

    // =====================================================
    // IMAGE CONTENT VALIDATION
    // =====================================================

    private String detectAndValidateImage(byte[] imageBytes) {

        String detectedType;

        // Check the actual file signature.
        if (isJpeg(imageBytes)) {
            detectedType = "image/jpeg";
        } else if (isPng(imageBytes)) {
            detectedType = "image/png";
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only valid JPG, JPEG and PNG images are allowed."
            );
        }

        // Decode the image and verify its dimensions.
        try (ByteArrayInputStream input =
                     new ByteArrayInputStream(imageBytes);
             ImageInputStream imageInput =
                     ImageIO.createImageInputStream(input)) {

            if (imageInput == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid image file."
                );
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(imageInput);

            if (!readers.hasNext()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Unsupported or corrupted image."
                );
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(imageInput, true, true);

                String format = reader
                        .getFormatName()
                        .toUpperCase(Locale.ROOT);

                if (detectedType.equals("image/jpeg")
                        && !format.equals("JPEG")) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Image content does not match its file signature."
                    );
                }

                if (detectedType.equals("image/png")
                        && !format.equals("PNG")) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Image content does not match its file signature."
                    );
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width <= 0 || height <= 0
                        || width > MAX_IMAGE_DIMENSION
                        || height > MAX_IMAGE_DIMENSION
                        || (long) width * height > MAX_IMAGE_PIXELS) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Image dimensions are too large. Maximum allowed dimensions are 4096 x 4096."
                    );
                }

                // Decode to verify that the image is actually readable.
                if (reader.read(0) == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Unable to decode the uploaded image."
                    );
                }

            } finally {
                reader.dispose();
            }

        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The uploaded image is corrupted or invalid.",
                    exception
            );
        }

        return detectedType;
    }

    // =====================================================
    // FILE SIGNATURE CHECKS
    // =====================================================

    private boolean isJpeg(byte[] bytes) {

        return bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] bytes) {

        byte[] signature = {
                (byte) 0x89,
                0x50,
                0x4E,
                0x47,
                0x0D,
                0x0A,
                0x1A,
                0x0A
        };

        if (bytes.length < signature.length) {
            return false;
        }

        for (int i = 0; i < signature.length; i++) {
            if (bytes[i] != signature[i]) {
                return false;
            }
        }

        return true;
    }

    // =====================================================
    // FILE NAME VALIDATION
    // =====================================================

    private String sanitizeFileName(String originalFileName) {

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Image filename is missing."
            );
        }

        String normalized = originalFileName
                .replace('\\', '/');

        String fileName = normalized.substring(
                normalized.lastIndexOf('/') + 1
        );

        fileName = fileName.trim();

        if (fileName.isBlank()
                || fileName.equals(".")
                || fileName.equals("..")
                || fileName.length() > 255
                || fileName.indexOf('\0') >= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid image filename."
            );
        }

        return fileName;
    }

    private String getExtension(String fileName) {

        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex <= 0 || dotIndex == fileName.length() - 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Image file must have a JPG, JPEG or PNG extension."
            );
        }

        return fileName.substring(dotIndex + 1)
                .toLowerCase(Locale.ROOT);
    }

    private void validateExtension(
            String extension,
            String contentType) {

        boolean valid = switch (extension) {
            case "jpg", "jpeg" ->
                    contentType.equals("image/jpeg");

            case "png" ->
                    contentType.equals("image/png");

            default -> false;
        };

        if (!valid) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File extension does not match the actual image format."
            );
        }
    }

    private void validateProvidedContentType(
            String providedContentType,
            String detectedContentType) {

        if (providedContentType == null
                || providedContentType.isBlank()
                || providedContentType.equalsIgnoreCase(
                "application/octet-stream")) {
            return;
        }

        if (!providedContentType.equalsIgnoreCase(detectedContentType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provided content type does not match the image."
            );
        }
    }

    // =====================================================
    // DTO MAPPING
    // =====================================================

    private ProfilePhotoResponse toResponse(UserProfilePhoto photo) {

        return new ProfilePhotoResponse(
                photo.getFileName(),
                photo.getContentType(),
                photo.getFileExtension(),
                photo.getFileSize(),
                photo.getUploadedAt()
        );
    }
}
