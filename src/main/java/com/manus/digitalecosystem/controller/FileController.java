package com.manus.digitalecosystem.controller;

import com.manus.digitalecosystem.dto.response.FileUploadResponse;
import com.manus.digitalecosystem.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@Tag(name = "Files", description = "File upload/download (used for profile images)")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file (multipart/form-data)")
    public ResponseEntity<FileUploadResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String purpose
    ) {
        return ResponseEntity.ok(fileStorageService.store(file, purpose));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Download a file by id")
    public ResponseEntity<Resource> download(@PathVariable String id) throws IOException {
        var resource = fileStorageService.getById(id);

        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        if (resource.getContentType() != null && !resource.getContentType().isBlank()) {
            contentType = MediaType.parseMediaType(resource.getContentType());
        }

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(new InputStreamResource(resource.getInputStream()));
    }
}
