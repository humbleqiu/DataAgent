package com.alibaba.cloud.ai.controller;

import com.alibaba.cloud.ai.dto.image.ImageCandidateDTO;
import com.alibaba.cloud.ai.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/image")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/candidates")
    public ResponseEntity<List<ImageCandidateDTO>> getCandidates(@RequestParam("image") MultipartFile image) {
        try {
            List<ImageCandidateDTO> candidates = imageService.getCandidatesFromImage(image);
            return ResponseEntity.ok(candidates);
        } catch (IOException e) {
            log.error("Failed to process image for candidates: ", e);
            return ResponseEntity.status(500).build();
        }
    }
}
