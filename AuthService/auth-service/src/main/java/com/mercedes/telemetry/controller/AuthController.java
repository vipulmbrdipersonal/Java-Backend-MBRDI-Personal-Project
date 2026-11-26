
package com.mercedes.telemetry.controller;

import com.mercedes.telemetry.dto.*;
import com.mercedes.telemetry.service.AuthService;
import com.mercedes.telemetry.service.PasswordResetService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(
            AuthService authService,
            PasswordResetService passwordResetService
    ) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    // =====================================
    // SIGNUP
    // =====================================

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(
            @Valid @RequestBody SignupRequest request) {

        return ResponseEntity.ok(authService.signup(request));
    }

    // =====================================
    // LOGIN
    // =====================================

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    // =====================================
    // LOGOUT
    // =====================================

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        String token = header != null && header.startsWith("Bearer ")
                ? header.substring(7)
                : null;

        authService.logout(token);

        return ResponseEntity.ok(
                Map.of("message", "Logout successful")
        );
    }

    // =====================================
    // CURRENT USER PROFILE
    // =====================================

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(
            Authentication authentication) {

        return ResponseEntity.ok(
                authService.getProfile(authentication.getName())
        );
    }

    // =====================================
    // REQUEST PASSWORD CHANGE OTP
    // Existing authenticated Settings flow
    // =====================================

    @PostMapping("/change-password/request-otp")
    public ResponseEntity<Map<String, String>> requestPasswordChangeOtp(
            Authentication authentication,
            @RequestBody Map<String, String> request) {

        String currentPassword = request.get("currentPassword");

        authService.requestPasswordChangeOtp(
                authentication.getName(),
                currentPassword
        );

        // Never return the OTP to the frontend.
        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Password change OTP has been sent to your registered email address."
                )
        );
    }

    // =====================================
    // VERIFY OTP AND CHANGE PASSWORD
    // Existing authenticated Settings flow
    // =====================================

    @PutMapping("/change-password/verify-otp")
    public ResponseEntity<Map<String, String>> verifyPasswordChangeOtp(
            Authentication authentication,
            @RequestParam("otp") String otp,
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.completePasswordChange(
                authentication.getName(),
                otp,
                request
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Password updated successfully. A confirmation email will be sent."
                )
        );
    }

    // =====================================
    // FORGOT PASSWORD - REQUEST OTP
    // Public endpoint
    // =====================================

    @PostMapping("/forgot-password/request-otp")
    public ResponseEntity<Map<String, String>> requestPasswordResetOtp(
            @Valid @RequestBody ForgotPasswordRequest request) {

        /*
         * Always return the same response whether the email
         * belongs to an account or not.
         *
         * Cooldown/rate-limit responses are also kept generic
         * to avoid revealing account existence.
         */
        try {
            passwordResetService.requestOtp(request.getEmail());
        } catch (IllegalStateException exception) {
            // Cooldown or rate limit reached.
            // Do not reveal account-specific information.
        }

        return ResponseEntity.accepted().body(
                Map.of(
                        "message",
                        "If an account exists for this email address, a password reset OTP will be sent."
                )
        );
    }

    // =====================================
    // FORGOT PASSWORD - VERIFY OTP
    // Public endpoint
    // =====================================

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<Map<String, String>> verifyPasswordResetOtp(
            @Valid @RequestBody VerifyPasswordResetOtpRequest request) {

        String resetToken = passwordResetService.verifyOtp(
                request.getEmail(),
                request.getOtp()
        );

        if (resetToken == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired OTP. Please request a new OTP."
            );
        }

        /*
         * Return the temporary reset token only after successful OTP
         * verification. The frontend should keep it in navigation
         * state and must not put it in the URL.
         */
        return ResponseEntity.ok(
                Map.of(
                        "resetToken",
                        resetToken,
                        "message",
                        "OTP verified successfully. You can now reset your password."
                )
        );
    }

    // =====================================
    // FORGOT PASSWORD - RESET PASSWORD
    // Public endpoint, protected by reset token
    // =====================================

    @PutMapping("/forgot-password/reset")
    public ResponseEntity<Map<String, String>> resetForgottenPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password and confirm password do not match."
            );
        }

        boolean updated = passwordResetService.resetPassword(
                request.getResetToken(),
                request.getNewPassword()
        );

        if (!updated) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password reset session has expired. Please start again."
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Password reset successfully. A confirmation email will be sent."
                )
        );
    }

    // =====================================
    // UPLOAD / REPLACE PROFILE PHOTO
    // =====================================

    @PutMapping(
            value = "/profile/photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, String>> uploadProfilePhoto(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {

        authService.uploadProfilePhoto(
                authentication.getName(),
                file
        );

        return ResponseEntity.ok(
                Map.of("message", "Profile photo uploaded successfully")
        );
    }

    // =====================================
    // DISPLAY PROFILE PHOTO
    // =====================================

    @GetMapping("/profile/photo")
    public ResponseEntity<ByteArrayResource> getProfilePhoto(
            Authentication authentication) {

        ProfilePhotoResponse photo =
                authService.getProfilePhoto(authentication.getName());

        ContentDisposition disposition =
                ContentDisposition.inline()
                        .filename(
                                photo.getFileName(),
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        photo.getContentType()
                ))
                .contentLength(photo.getData().length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .cacheControl(CacheControl.noStore())
                .body(new ByteArrayResource(photo.getData()));
    }

    // =====================================
    // DOWNLOAD ORIGINAL PROFILE PHOTO
    // =====================================

    @GetMapping("/profile/photo/download")
    public ResponseEntity<ByteArrayResource> downloadProfilePhoto(
            Authentication authentication) {

        ProfilePhotoResponse photo =
                authService.getProfilePhoto(authentication.getName());

        ContentDisposition disposition =
                ContentDisposition.attachment()
                        .filename(
                                photo.getFileName(),
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        photo.getContentType()
                ))
                .contentLength(photo.getData().length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .cacheControl(CacheControl.noStore())
                .body(new ByteArrayResource(photo.getData()));
    }

    // =====================================
    // DELETE PROFILE PHOTO
    // =====================================

    @DeleteMapping("/profile/photo")
    public ResponseEntity<Map<String, String>> deleteProfilePhoto(
            Authentication authentication) {

        authService.deleteProfilePhoto(
                authentication.getName()
        );

        return ResponseEntity.ok(
                Map.of("message", "Profile photo deleted successfully")
        );
    }

    // =====================================
    // UPDATE CUSTOMER PROFILE
    // =====================================

    @PutMapping("/profile")
    public ResponseEntity<AuthResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request) {

        return ResponseEntity.ok(
                authService.updateProfile(
                        authentication.getName(),
                        request
                )
        );
    }
}
