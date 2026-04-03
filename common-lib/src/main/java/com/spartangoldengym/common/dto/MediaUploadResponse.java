package com.spartangoldengym.common.dto;

public class MediaUploadResponse {

    private String mediaId;
    private String mediaType;
    private String bucket;
    private String key;
    private long sizeBytes;

    public MediaUploadResponse() {}

    public MediaUploadResponse(String mediaId, String mediaType, String bucket, String key, long sizeBytes) {
        this.mediaId = mediaId;
        this.mediaType = mediaType;
        this.bucket = bucket;
        this.key = key;
        this.sizeBytes = sizeBytes;
    }

    public String getMediaId() { return mediaId; }
    public void setMediaId(String mediaId) { this.mediaId = mediaId; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
}
