
package com.mercedes.telemetry.dto;

import java.time.LocalDateTime;

public class ProfilePhotoResponse {

    private String fileName;
    private String contentType;
    private String fileExtension;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private byte[] data;

    public ProfilePhotoResponse() {
    }

    // Constructor used by ProfilePhotoService
    // for photo metadata.
    public ProfilePhotoResponse(
            String fileName,
            String contentType,
            String fileExtension,
            Long fileSize,
            LocalDateTime uploadedAt) {

        this.fileName = fileName;
        this.contentType = contentType;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }

    // Constructor used when returning actual image data.
    public ProfilePhotoResponse(
            String fileName,
            String contentType,
            String fileExtension,
            Long fileSize,
            LocalDateTime uploadedAt,
            byte[] data) {

        this.fileName = fileName;
        this.contentType = contentType;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
