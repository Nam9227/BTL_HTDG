package com.uet.common.network;

import java.io.Serializable;

public class ImageData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String originalFileName;
    private String contentType;
    private byte[] data;
    private String imageType;      
    private String imageId;        

    
    public ImageData(String originalFileName, String contentType, byte[] data) {
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.data = data;
    }

    
    public ImageData(String originalFileName, String contentType, byte[] data,
                     String imageType, String imageId) {
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.data = data;
        this.imageType = imageType;
        this.imageId = imageId;
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

    public String getImageType() {
        return imageType;
    }

    public String getImageId() {
        return imageId;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public boolean isEmpty() {
        return data == null || data.length == 0;
    }
}