
package com.mercedes.telemetry.service;

import com.mercedes.telemetry.dto.*;
import com.mercedes.telemetry.entity.*;
import com.mercedes.telemetry.event.*;
import com.mercedes.telemetry.exception.ApiException;
import com.mercedes.telemetry.repository.*;
import com.mercedes.telemetry.security.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final long MAX_PHOTO_SIZE =
            5L * 1024L * 1024L;

    private static final int MAX_IMAGE_DIMENSION = 4096;

    private static final long MAX_IMAGE_PIXELS = 16_000_000L;

    private final UserRepository userRepository;

    private final RevokedTokenRepository revokedTokenRepository;

    private final UserProfilePhotoRepository userProfilePhotoRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final KafkaEventPublisher eventPublisher;

    private final PasswordChangeOtpService passwordChangeOtpService;

    private final PasswordEmailEventPublisher passwordEmailEventPublisher;

    public AuthService(
            UserRepository userRepository,
            RevokedTokenRepository revokedTokenRepository,
            UserProfilePhotoRepository userProfilePhotoRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            KafkaEventPublisher eventPublisher,
            PasswordChangeOtpService passwordChangeOtpService,
            PasswordEmailEventPublisher passwordEmailEventPublisher) {

        this.userRepository = userRepository;
        this.revokedTokenRepository = revokedTokenRepository;
        this.userProfilePhotoRepository = userProfilePhotoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.eventPublisher = eventPublisher;
        this.passwordChangeOtpService = passwordChangeOtpService;
        this.passwordEmailEventPublisher = passwordEmailEventPublisher;
    }

    // =========================================
    // CUSTOMER REGISTRATION
    // =========================================

    @Transactional
    public AuthResponse signup(SignupRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        String mobile = request.getMobileNumber();

        if (mobile != null) {
            mobile = mobile.trim();

            if (mobile.isEmpty()) {
                mobile = null;
            }
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(
                    "Email already registered",
                    HttpStatus.CONFLICT
            );
        }

        if (mobile != null
                && userRepository.existsByMobileNumber(mobile)) {

            throw new ApiException(
                    "Mobile number already registered",
                    HttpStatus.CONFLICT
            );
        }

        User user = new User();

        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setMobileNumber(mobile);

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);

        user = userRepository.save(user);

        UserRegisteredEvent event = new UserRegisteredEvent(
                UUID.randomUUID().toString(),
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );

        eventPublisher.publishUserRegistered(event);

        String token = jwtService.generateToken(user);

        boolean hasProfilePhoto =
                userProfilePhotoRepository.existsByUser_Id(
                        user.getId()
                );

        return new AuthResponse(
                "Registration successful",
                token,
                jwtService.getExpirationMs(),
                UserResponse.from(user, hasProfilePhoto)
        );
    }

    // =========================================
    // CUSTOMER LOGIN
    // =========================================

    @Transactional
    public AuthResponse login(LoginRequest request) {

        String identifier = request.getIdentifier().trim();

        Optional<User> optionalUser;

        if (identifier.contains("@")) {

            optionalUser =
                    userRepository.findByEmailIgnoreCase(identifier);

        } else {

            optionalUser =
                    userRepository.findByMobileNumber(identifier);
        }

        User user = optionalUser.orElseThrow(() ->
                new ApiException(
                        "Invalid email/mobile or password",
                        HttpStatus.UNAUTHORIZED
                )
        );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new ApiException(
                    "Invalid email/mobile or password",
                    HttpStatus.UNAUTHORIZED
            );
        }

        if (!user.isEnabled()) {

            throw new ApiException(
                    "Account is disabled",
                    HttpStatus.FORBIDDEN
            );
        }

        user.setLastLoginAt(LocalDateTime.now());

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        boolean hasProfilePhoto =
                userProfilePhotoRepository.existsByUser_Id(
                        user.getId()
                );

        return new AuthResponse(
                "Login successful",
                token,
                jwtService.getExpirationMs(),
                UserResponse.from(user, hasProfilePhoto)
        );
    }

    // =========================================
    // GET CURRENT USER PROFILE
    // =========================================

    @Transactional(readOnly = true)
    public UserResponse getProfile(String email) {

        User user = findUserByEmail(email);

        boolean hasProfilePhoto =
                userProfilePhotoRepository.existsByUser_Id(
                        user.getId()
                );

        return UserResponse.from(user, hasProfilePhoto);
    }

    // =========================================
    // REQUEST PASSWORD CHANGE OTP
    // =========================================

    @Transactional
    public void requestPasswordChangeOtp(
            String email,
            String currentPassword) {

        if (email == null || email.isBlank()) {
            throw new ApiException(
                    "Authenticated user email is required",
                    HttpStatus.UNAUTHORIZED
            );
        }

        try {

            /*
             * This verifies the current password,
             * generates the OTP, stores its BCrypt hash,
             * and schedules the email event after commit.
             */
            passwordChangeOtpService.requestOtp(
                    email,
                    currentPassword
            );

        } catch (IllegalArgumentException exception) {

            throw new ApiException(
                    "Current password is incorrect",
                    HttpStatus.BAD_REQUEST
            );

        } catch (IllegalStateException exception) {

            throw new ApiException(
                    exception.getMessage(),
                    HttpStatus.TOO_MANY_REQUESTS
            );
        }
    }

    // =========================================
    // COMPLETE PASSWORD CHANGE USING OTP
    // =========================================

    @Transactional
    public void completePasswordChange(
            String email,
            String submittedOtp,
            ChangePasswordRequest request) {

        if (request == null
                || request.getCurrentPassword() == null
                || request.getNewPassword() == null
                || request.getNewPassword().isBlank()) {

            throw new ApiException(
                    "Current password and new password are required",
                    HttpStatus.BAD_REQUEST
            );
        }

        User user = findUserByEmail(email);

        /*
         * Verify the current password again.
         * This prevents completing a password change
         * if the password changed after OTP issuance.
         */
        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {

            throw new ApiException(
                    "Current password is incorrect",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {

            throw new ApiException(
                    "New password must differ from current password",
                    HttpStatus.BAD_REQUEST
            );
        }

        /*
         * Verify and consume the OTP.
         *
         * PasswordChangeOtpService uses REQUIRED transaction
         * propagation, so OTP consumption participates in
         * this AuthService transaction.
         */
        boolean validOtp;

        try {

            validOtp = passwordChangeOtpService.verifyOtp(
                    email,
                    submittedOtp
            );

        } catch (IllegalArgumentException exception) {

            validOtp = false;
        }

        if (!validOtp) {

            throw new ApiException(
                    "Invalid or expired OTP. Please request a new code.",
                    HttpStatus.BAD_REQUEST
            );
        }

        /*
         * Update the existing user's BCrypt password.
         */
        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);

        /*
         * Confirmation email is scheduled after this
         * transaction commits successfully.
         */
        PasswordChangedEmailEvent confirmationEvent =
                new PasswordChangedEmailEvent(
                        user.getEmail(),
                        LocalDateTime.now().toString()
                );

        passwordEmailEventPublisher
                .publishPasswordChangedAfterCommit(
                        confirmationEvent
                );
    }

    // =========================================
    // DISABLE OLD DIRECT PASSWORD CHANGE
    // =========================================

    @Deprecated
    public void changePassword(
            String email,
            ChangePasswordRequest request) {

        throw new ApiException(
                "Password changes require email OTP verification. "
                        + "Please request an OTP first.",
                HttpStatus.BAD_REQUEST
        );
    }

    // =========================================
    // UPLOAD / REPLACE PROFILE PHOTO
    // =========================================

    @Transactional
    public void uploadProfilePhoto(
            String email,
            MultipartFile file) {

        User user = findUserByEmail(email);

        ValidatedPhoto validatedPhoto =
                validateProfilePhoto(file);

        UserProfilePhoto photo = userProfilePhotoRepository
                .findByUser_Id(user.getId())
                .orElseGet(UserProfilePhoto::new);

        photo.setUser(user);

        photo.setFileName(
                validatedPhoto.fileName()
        );

        photo.setContentType(
                validatedPhoto.contentType()
        );

        photo.setFileExtension(
                validatedPhoto.fileExtension()
        );

        photo.setFileSize(
                (long) validatedPhoto.data().length
        );

        photo.setPhotoData(
                validatedPhoto.data()
        );

        userProfilePhotoRepository.save(photo);
    }

    // =========================================
    // GET CURRENT PROFILE PHOTO
    // =========================================

    @Transactional(readOnly = true)
    public ProfilePhotoResponse getProfilePhoto(String email) {

        User user = findUserByEmail(email);

        UserProfilePhoto photo = userProfilePhotoRepository
                .findByUser_Id(user.getId())
                .orElseThrow(() ->
                        new ApiException(
                                "Profile photo not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        return new ProfilePhotoResponse(
                photo.getFileName(),
                photo.getContentType(),
                photo.getFileExtension(),
                photo.getFileSize(),
                photo.getUploadedAt(),
                photo.getPhotoData()
        );
    }

    // =========================================
    // DELETE PROFILE PHOTO
    // =========================================

    @Transactional
    public void deleteProfilePhoto(String email) {

        User user = findUserByEmail(email);

        UserProfilePhoto photo = userProfilePhotoRepository
                .findByUser_Id(user.getId())
                .orElseThrow(() ->
                        new ApiException(
                                "Profile photo not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        userProfilePhotoRepository.delete(photo);
    }

    // =========================================
    // PROFILE PHOTO VALIDATION
    // =========================================

    private ValidatedPhoto validateProfilePhoto(
            MultipartFile file) {

        if (file == null || file.isEmpty()) {

            throw new ApiException(
                    "Please select a profile photo",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (file.getSize() > MAX_PHOTO_SIZE) {

            throw new ApiException(
                    "Profile photo must not exceed 5 MB",
                    HttpStatus.PAYLOAD_TOO_LARGE
            );
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null
                || originalFilename.isBlank()) {

            throw new ApiException(
                    "Invalid profile photo filename",
                    HttpStatus.BAD_REQUEST
            );
        }

        String cleanPath = StringUtils.cleanPath(
                originalFilename
        ).replace('\\', '/');

        String fileName = cleanPath.substring(
                cleanPath.lastIndexOf('/') + 1
        );

        if (fileName.isBlank()
                || fileName.equals(".")
                || fileName.equals("..")
                || fileName.length() > 255) {

            throw new ApiException(
                    "Invalid profile photo filename",
                    HttpStatus.BAD_REQUEST
            );
        }

        int extensionPosition = fileName.lastIndexOf('.');

        if (extensionPosition <= 0
                || extensionPosition == fileName.length() - 1) {

            throw new ApiException(
                    "Only JPG, JPEG and PNG images are supported",
                    HttpStatus.BAD_REQUEST
            );
        }

        String extension = fileName
                .substring(extensionPosition + 1)
                .toLowerCase(Locale.ROOT);

        if (!extension.equals("jpg")
                && !extension.equals("jpeg")
                && !extension.equals("png")) {

            throw new ApiException(
                    "Only JPG, JPEG and PNG images are supported",
                    HttpStatus.BAD_REQUEST
            );
        }

        byte[] imageBytes;

        try {

            imageBytes = file.getBytes();

        } catch (IOException exception) {

            throw new ApiException(
                    "Unable to read the uploaded profile photo",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (imageBytes.length == 0
                || imageBytes.length > MAX_PHOTO_SIZE) {

            throw new ApiException(
                    "Invalid profile photo size",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Check actual image signature

        boolean isJpeg = imageBytes.length >= 3
                && (imageBytes[0] & 0xFF) == 0xFF
                && (imageBytes[1] & 0xFF) == 0xD8
                && (imageBytes[2] & 0xFF) == 0xFF;

        boolean isPng = imageBytes.length >= 8
                && (imageBytes[0] & 0xFF) == 0x89
                && imageBytes[1] == 0x50
                && imageBytes[2] == 0x4E
                && imageBytes[3] == 0x47
                && imageBytes[4] == 0x0D
                && imageBytes[5] == 0x0A
                && imageBytes[6] == 0x1A
                && imageBytes[7] == 0x0A;

        if (!isJpeg && !isPng) {

            throw new ApiException(
                    "Uploaded file is not a valid JPG, JPEG or PNG image",
                    HttpStatus.BAD_REQUEST
            );
        }

        String contentType;
        String expectedFormat;

        if (isJpeg) {

            if (!extension.equals("jpg")
                    && !extension.equals("jpeg")) {

                throw new ApiException(
                        "Image extension does not match its actual format",
                        HttpStatus.BAD_REQUEST
                );
            }

            contentType = "image/jpeg";
            expectedFormat = "JPEG";

        } else {

            if (!extension.equals("png")) {

                throw new ApiException(
                        "Image extension does not match its actual format",
                        HttpStatus.BAD_REQUEST
                );
            }

            contentType = "image/png";
            expectedFormat = "PNG";
        }

        // Check declared content type

        String suppliedContentType = file.getContentType();

        if (suppliedContentType != null
                && !suppliedContentType.isBlank()
                && !suppliedContentType.equalsIgnoreCase(contentType)) {

            throw new ApiException(
                    "Image content type does not match the uploaded file",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Decode and validate image dimensions

        try (ImageInputStream imageInputStream =
                     ImageIO.createImageInputStream(
                             new ByteArrayInputStream(imageBytes))) {

            if (imageInputStream == null) {

                throw new ApiException(
                        "Invalid image file",
                        HttpStatus.BAD_REQUEST
                );
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(imageInputStream);

            if (!readers.hasNext()) {

                throw new ApiException(
                        "Uploaded file is not a readable image",
                        HttpStatus.BAD_REQUEST
                );
            }

            ImageReader reader = readers.next();

            try {

                reader.setInput(imageInputStream, true, true);

                String actualFormat = reader
                        .getFormatName()
                        .toUpperCase(Locale.ROOT);

                if (!actualFormat.equals(expectedFormat)) {

                    throw new ApiException(
                            "Image format validation failed",
                            HttpStatus.BAD_REQUEST
                    );
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width <= 0
                        || height <= 0
                        || width > MAX_IMAGE_DIMENSION
                        || height > MAX_IMAGE_DIMENSION
                        || (long) width * height > MAX_IMAGE_PIXELS) {

                    throw new ApiException(
                            "Image dimensions are too large. "
                                    + "Maximum allowed dimensions are "
                                    + "4096 x 4096 pixels",
                            HttpStatus.BAD_REQUEST
                    );
                }

                if (reader.read(0) == null) {

                    throw new ApiException(
                            "Uploaded image is corrupted or invalid",
                            HttpStatus.BAD_REQUEST
                    );
                }

            } finally {

                reader.dispose();
            }

        } catch (IOException exception) {

            throw new ApiException(
                    "Unable to validate the uploaded image",
                    HttpStatus.BAD_REQUEST
            );
        }

        return new ValidatedPhoto(
                fileName,
                contentType,
                extension,
                imageBytes
        );
    }

    // =========================================
    // FIND USER BY EMAIL
    // =========================================

    private User findUserByEmail(String email) {

        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ApiException(
                                "User not found",
                                HttpStatus.NOT_FOUND
                        )
                );
    }

    // =========================================
    // VALIDATED PHOTO DATA
    // =========================================

    private record ValidatedPhoto(
            String fileName,
            String contentType,
            String fileExtension,
            byte[] data
    ) {
    }

    // =========================================
    // LOGOUT / TOKEN REVOCATION
    // =========================================

    @Transactional
    public void logout(String token) {

        if (token == null || token.isBlank()) {

            throw new ApiException(
                    "Authentication token is required",
                    HttpStatus.BAD_REQUEST
            );
        }

        String tokenId = jwtService.extractTokenId(token);

        if (revokedTokenRepository.existsByTokenId(tokenId)) {
            return;
        }

        Instant expiry = jwtService.extractExpiration(token);

        LocalDateTime expiryDate = LocalDateTime.ofInstant(
                expiry,
                ZoneOffset.UTC
        );

        LocalDateTime revokedAt =
                LocalDateTime.now(ZoneOffset.UTC);

        RevokedToken revokedToken = new RevokedToken();

        revokedToken.setTokenId(tokenId);
        revokedToken.setExpiresAt(expiryDate);
        revokedToken.setRevokedAt(revokedAt);

        revokedTokenRepository.save(revokedToken);

        TokenRevokedEvent event = new TokenRevokedEvent(
                UUID.randomUUID().toString(),
                tokenId,
                expiryDate,
                revokedAt
        );

        eventPublisher.publishTokenRevoked(event);
    }

    // =========================================
    // UPDATE CUSTOMER PROFILE
    // =========================================

    @Transactional
    public AuthResponse updateProfile(
            String currentEmail,
            ProfileUpdateRequest request) {

        User user = findUserByEmail(currentEmail);

        if (request.getEmail() == null
                && request.getMobileNumber() == null) {

            throw new ApiException(
                    "Please provide at least one profile field to update",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Update email only when supplied.
        if (request.getEmail() != null) {

            String newEmail = request.getEmail()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            if (newEmail.isBlank()) {
                throw new ApiException(
                        "Email address cannot be empty",
                        HttpStatus.BAD_REQUEST
                );
            }

            if (userRepository.existsByEmailIgnoreCaseAndIdNot(
                    newEmail,
                    user.getId())) {

                throw new ApiException(
                        "Email address is already registered",
                        HttpStatus.CONFLICT
                );
            }

            user.setEmail(newEmail);
        }

        // Update mobile number only when supplied.
        if (request.getMobileNumber() != null) {

            String newMobile = request.getMobileNumber().trim();

            if (newMobile.isBlank()) {
                newMobile = null;
            }

            if (newMobile != null
                    && userRepository.existsByMobileNumberAndIdNot(
                    newMobile,
                    user.getId())) {

                throw new ApiException(
                        "Mobile number is already registered",
                        HttpStatus.CONFLICT
                );
            }

            user.setMobileNumber(newMobile);
        }

        User updatedUser = userRepository.save(user);

        boolean hasProfilePhoto =
                userProfilePhotoRepository.existsByUser_Id(
                        updatedUser.getId()
                );

        String newToken = jwtService.generateToken(updatedUser);

        return new AuthResponse(
                "Profile updated successfully",
                newToken,
                jwtService.getExpirationMs(),
                UserResponse.from(updatedUser, hasProfilePhoto)
        );
    }
}
