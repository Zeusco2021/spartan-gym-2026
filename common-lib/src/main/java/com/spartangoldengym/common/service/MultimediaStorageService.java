package com.spartangoldengym.common.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.spartangoldengym.common.config.MultimediaProperties;
import com.spartangoldengym.common.dto.MediaUploadResponse;
import com.spartangoldengym.common.dto.MediaUrlResponse;
import com.spartangoldengym.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

/**
 * Service for multimedia storage in S3 with CloudFront distribution.
 * - Exercise videos: stored in video bucket, distributed via CloudFront
 * - Progress photos: stored in photo bucket with AES-256 server-side encryption
 */
@Service
public class MultimediaStorageService {

    private final AmazonS3 amazonS3;
    private final MultimediaProperties properties;

    public MultimediaStorageService(AmazonS3 amazonS3, MultimediaProperties properties) {
        this.amazonS3 = amazonS3;
        this.properties = properties;
    }

    /**
     * Upload an exercise video to S3 with proper content type and metadata.
     */
    public MediaUploadResponse uploadVideo(InputStream inputStream, long contentLength,
                                           String originalFilename, String contentType) {
        String mediaId = UUID.randomUUID().toString();
        String key = "videos/" + mediaId + "/" + sanitizeFilename(originalFilename);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType != null ? contentType : "video/mp4");
        metadata.setContentLength(contentLength);
        metadata.addUserMetadata("original-filename", originalFilename);
        metadata.addUserMetadata("media-id", mediaId);

        PutObjectRequest request = new PutObjectRequest(
                properties.getVideoBucket(), key, inputStream, metadata);

        amazonS3.putObject(request);

        return new MediaUploadResponse(mediaId, "video",
                properties.getVideoBucket(), key, contentLength);
    }

    /**
     * Upload a progress photo to S3 with AES-256 server-side encryption.
     */
    public MediaUploadResponse uploadPhoto(InputStream inputStream, long contentLength,
                                           String originalFilename, String contentType) {
        String mediaId = UUID.randomUUID().toString();
        String key = "photos/" + mediaId + "/" + sanitizeFilename(originalFilename);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType != null ? contentType : "image/jpeg");
        metadata.setContentLength(contentLength);
        metadata.addUserMetadata("original-filename", originalFilename);
        metadata.addUserMetadata("media-id", mediaId);
        // AES-256 server-side encryption for progress photos
        metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);

        PutObjectRequest request = new PutObjectRequest(
                properties.getPhotoBucket(), key, inputStream, metadata);

        amazonS3.putObject(request);

        return new MediaUploadResponse(mediaId, "photo",
                properties.getPhotoBucket(), key, contentLength);
    }

    /**
     * Generate a CloudFront URL for video streaming.
     * Uses the CloudFront domain for low-latency delivery (< 2s start).
     */
    public MediaUrlResponse getVideoStreamUrl(String mediaId) {
        String prefix = "videos/" + mediaId + "/";
        String key = findObjectKeyByPrefix(properties.getVideoBucket(), prefix);
        if (key == null) {
            throw new ResourceNotFoundException("Video", mediaId);
        }

        String cloudfrontDomain = properties.getCloudfrontDomain();
        if (cloudfrontDomain != null && !cloudfrontDomain.isEmpty()) {
            String url = "https://" + cloudfrontDomain + "/" + key;
            Date expiresAt = computeExpiration();
            return new MediaUrlResponse(mediaId, url, expiresAt);
        }

        // Fallback to S3 presigned URL if CloudFront is not configured
        return generatePresignedUrl(properties.getVideoBucket(), key, mediaId);
    }

    /**
     * Generate a presigned URL for photo download.
     */
    public MediaUrlResponse getPhotoUrl(String mediaId) {
        String prefix = "photos/" + mediaId + "/";
        String key = findObjectKeyByPrefix(properties.getPhotoBucket(), prefix);
        if (key == null) {
            throw new ResourceNotFoundException("Photo", mediaId);
        }
        return generatePresignedUrl(properties.getPhotoBucket(), key, mediaId);
    }

    /**
     * Delete a media file from S3.
     */
    public void deleteMedia(String type, String mediaId) {
        String bucket;
        String prefix;
        if ("video".equalsIgnoreCase(type) || "videos".equalsIgnoreCase(type)) {
            bucket = properties.getVideoBucket();
            prefix = "videos/" + mediaId + "/";
        } else if ("photo".equalsIgnoreCase(type) || "photos".equalsIgnoreCase(type)) {
            bucket = properties.getPhotoBucket();
            prefix = "photos/" + mediaId + "/";
        } else {
            throw new IllegalArgumentException("Unsupported media type: " + type);
        }

        ListObjectsV2Request listRequest = new ListObjectsV2Request()
                .withBucketName(bucket)
                .withPrefix(prefix);
        ListObjectsV2Result result = amazonS3.listObjectsV2(listRequest);

        if (result.getObjectSummaries().isEmpty()) {
            throw new ResourceNotFoundException("Media", mediaId);
        }

        for (S3ObjectSummary summary : result.getObjectSummaries()) {
            amazonS3.deleteObject(bucket, summary.getKey());
        }
    }

    private MediaUrlResponse generatePresignedUrl(String bucket, String key, String mediaId) {
        Date expiration = computeExpiration();
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);
        URL url = amazonS3.generatePresignedUrl(request);
        return new MediaUrlResponse(mediaId, url.toString(), expiration);
    }

    private String findObjectKeyByPrefix(String bucket, String prefix) {
        ListObjectsV2Request listRequest = new ListObjectsV2Request()
                .withBucketName(bucket)
                .withPrefix(prefix)
                .withMaxKeys(1);
        ListObjectsV2Result result = amazonS3.listObjectsV2(listRequest);
        if (result.getObjectSummaries().isEmpty()) {
            return null;
        }
        return result.getObjectSummaries().get(0).getKey();
    }

    private Date computeExpiration() {
        long millis = properties.getPresignedUrlExpirationMinutes() * 60 * 1000;
        return new Date(System.currentTimeMillis() + millis);
    }

    static String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "file";
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
