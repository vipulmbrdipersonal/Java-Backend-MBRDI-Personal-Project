
package com.mercedes.telemetry.dto;

public class ProfilePhotoFile {

    private final byte[] data;
    private final String fileName;
    private final String contentType;

    public ProfilePhotoFile(
            byte[] data,
            String fileName,
            String contentType) {

        this.data = data;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }
}
