package com.uet.common.network;

import java.io.Serializable;

public class FileUploadData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String originalFileName;
    private final String contentType;
    private final byte[] data;

    public FileUploadData(String originalFileName, String contentType, byte[] data) {
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.data = data;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isEmpty() {
        return data == null || data.length == 0;
    }
}