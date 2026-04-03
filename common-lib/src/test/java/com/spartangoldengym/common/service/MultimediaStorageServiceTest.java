package com.spartangoldengym.common.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.spartangoldengym.common.config.MultimediaProperties;
import com.spartangoldengym.common.dto.MediaUploadResponse;
import com.spartangoldengym.common.dto.MediaUrlResponse;
import com.spartangoldengym.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MultimediaStorageServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    private MultimediaProperties properties;
    private MultimediaStorageService service;

    @BeforeEach
    void setUp() {
        properties = new MultimediaProperties();
        properties.setVideoBucket("test-videos");
        properties.setPhotoBucket("test-photos");
        properties.setCloudfrontDomain("cdn.example.com");
        properties.setPresignedUrlExpirationMinutes(60);
        service = new MultimediaStorageService(amazonS3, properties);
    }

    @Test
    void uploadVideo_storesInVideoBucketWithCorrectMetadata() {
        InputStream input = new ByteArrayInputStream("video-data".getBytes());
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);

        MediaUploadResponse response = service.uploadVideo(input, 10L, "workout.mp4", "video/mp4");

        verify(amazonS3).putObject(captor.capture());
        PutObjectRequest request = captor.getValue();

        assertEquals("test-videos", request.getBucketName());
        assertTrue(request.getKey().startsWith("videos/"));
        assertTrue(request.getKey().endsWith("/workout.mp4"));
        assertEquals("video/mp4", request.getMetadata().getContentType());
        assertEquals(10L, request.getMetadata().getContentLength());
        assertEquals("video", response.getMediaType());
        assertNotNull(response.getMediaId());
    }

    @Test
    void uploadPhoto_storesWithAes256Encryption() {
        InputStream input = new ByteArrayInputStream("photo-data".getBytes());
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);

        MediaUploadResponse response = service.uploadPhoto(input, 8L, "progress.jpg", "image/jpeg");

        verify(amazonS3).putObject(captor.capture());
        PutObjectRequest request = captor.getValue();

        assertEquals("test-photos", request.getBucketName());
        assertTrue(request.getKey().startsWith("photos/"));
        assertEquals("image/jpeg", request.getMetadata().getContentType());
        assertEquals("AES256", request.getMetadata().getSSEAlgorithm());
        assertEquals("photo", response.getMediaType());
    }

    @Test
    void getVideoStreamUrl_withCloudfrontDomain_returnsCloudfrontUrl() {
        S3ObjectSummary summary = new S3ObjectSummary();
        summary.setKey("videos/abc-123/workout.mp4");
        ListObjectsV2Result listResult = new ListObjectsV2Result();
        listResult.getObjectSummaries().add(summary);
        when(amazonS3.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listResult);

        MediaUrlResponse response = service.getVideoStreamUrl("abc-123");

        assertEquals("abc-123", response.getMediaId());
        assertTrue(response.getUrl().startsWith("https://cdn.example.com/videos/abc-123/"));
        assertNotNull(response.getExpiresAt());
    }

    @Test
    void getVideoStreamUrl_withoutCloudfrontDomain_fallsBackToPresigned() throws MalformedURLException {
        properties.setCloudfrontDomain("");
        service = new MultimediaStorageService(amazonS3, properties);

        S3ObjectSummary summary = new S3ObjectSummary();
        summary.setKey("videos/abc-123/workout.mp4");
        ListObjectsV2Result listResult = new ListObjectsV2Result();
        listResult.getObjectSummaries().add(summary);
        when(amazonS3.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listResult);
        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(new URL("https://s3.amazonaws.com/test-videos/videos/abc-123/workout.mp4"));

        MediaUrlResponse response = service.getVideoStreamUrl("abc-123");

        assertTrue(response.getUrl().contains("s3.amazonaws.com"));
    }

    @Test
    void getVideoStreamUrl_notFound_throwsException() {
        ListObjectsV2Result emptyResult = new ListObjectsV2Result();
        when(amazonS3.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(emptyResult);

        assertThrows(ResourceNotFoundException.class, () -> service.getVideoStreamUrl("nonexistent"));
    }

    @Test
    void getPhotoUrl_existingPhoto_returnsPresignedUrl() throws MalformedURLException {
        S3ObjectSummary summary = new S3ObjectSummary();
        summary.setKey("photos/photo-123/progress.jpg");
        ListObjectsV2Result listResult = new ListObjectsV2Result();
        listResult.getObjectSummaries().add(summary);
        when(amazonS3.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listResult);
        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(new URL("https://s3.amazonaws.com/test-photos/photos/photo-123/progress.jpg"));

        MediaUrlResponse response = service.getPhotoUrl("photo-123");

        assertEquals("photo-123", response.getMediaId());
        assertNotNull(response.getUrl());
        assertNotNull(response.getExpiresAt());
    }

    @Test
    void deleteMedia_video_deletesAllObjectsWithPrefix() {
        S3ObjectSummary summary = new S3ObjectSummary();
        summary.setKey("videos/vid-123/workout.mp4");
        ListObjectsV2Result listResult = new ListObjectsV2Result();
        listResult.getObjectSummaries().add(summary);
        when(amazonS3.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listResult);

        service.deleteMedia("video", "vid-123");

        verify(amazonS3).deleteObject("test-videos", "videos/vid-123/workout.mp4");
    }

    @Test
    void deleteMedia_photo_deletesFromPhotoBucket() {
        S3ObjectSummary summary = new S3ObjectSummary();
        summary.setKey("photos/photo-123/progress.jpg");
        ListObjectsV2Result listResult = new ListObjectsV2Result();
        listResult.getObjectSummaries().add(summary);
        when(amazonS3.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listResult);

        service.deleteMedia("photos", "photo-123");

        verify(amazonS3).deleteObject("test-photos", "photos/photo-123/progress.jpg");
    }

    @Test
    void deleteMedia_notFound_throwsException() {
        ListObjectsV2Result emptyResult = new ListObjectsV2Result();
        when(amazonS3.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(emptyResult);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteMedia("video", "nonexistent"));
    }

    @Test
    void deleteMedia_unsupportedType_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteMedia("audio", "some-id"));
    }

    @Test
    void sanitizeFilename_removesSpecialCharacters() {
        assertEquals("my_file.mp4", MultimediaStorageService.sanitizeFilename("my file.mp4"));
        assertEquals("file", MultimediaStorageService.sanitizeFilename(""));
        assertEquals("file", MultimediaStorageService.sanitizeFilename(null));
        assertEquals("test_video__1_.mp4", MultimediaStorageService.sanitizeFilename("test video (1).mp4"));
    }

    @Test
    void uploadVideo_defaultsContentTypeToMp4WhenNull() {
        InputStream input = new ByteArrayInputStream("data".getBytes());
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);

        service.uploadVideo(input, 4L, "video.unknown", null);

        verify(amazonS3).putObject(captor.capture());
        assertEquals("video/mp4", captor.getValue().getMetadata().getContentType());
    }

    @Test
    void uploadPhoto_defaultsContentTypeToJpegWhenNull() {
        InputStream input = new ByteArrayInputStream("data".getBytes());
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);

        service.uploadPhoto(input, 4L, "photo.unknown", null);

        verify(amazonS3).putObject(captor.capture());
        assertEquals("image/jpeg", captor.getValue().getMetadata().getContentType());
    }
}
