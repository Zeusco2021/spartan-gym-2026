package com.spartangoldengym.entrenamiento.controller;

import com.spartangoldengym.common.dto.MediaUploadResponse;
import com.spartangoldengym.common.dto.MediaUrlResponse;
import com.spartangoldengym.common.service.MultimediaStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MultimediaStorageService storageService;

    public MediaController(MultimediaStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/videos/upload")
    public ResponseEntity<MediaUploadResponse> uploadVideo(
            @RequestParam("file") MultipartFile file) throws IOException {
        MediaUploadResponse response = storageService.uploadVideo(
                file.getInputStream(),
                file.getSize(),
                file.getOriginalFilename(),
                file.getContentType());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/videos/{id}/stream")
    public ResponseEntity<MediaUrlResponse> getVideoStreamUrl(
            @PathVariable("id") String id) {
        MediaUrlResponse response = storageService.getVideoStreamUrl(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/photos/upload")
    public ResponseEntity<MediaUploadResponse> uploadPhoto(
            @RequestParam("file") MultipartFile file) throws IOException {
        MediaUploadResponse response = storageService.uploadPhoto(
                file.getInputStream(),
                file.getSize(),
                file.getOriginalFilename(),
                file.getContentType());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/photos/{id}")
    public ResponseEntity<MediaUrlResponse> getPhotoUrl(
            @PathVariable("id") String id) {
        MediaUrlResponse response = storageService.getPhotoUrl(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{type}/{id}")
    public ResponseEntity<Void> deleteMedia(
            @PathVariable("type") String type,
            @PathVariable("id") String id) {
        storageService.deleteMedia(type, id);
        return ResponseEntity.noContent().build();
    }
}
