package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.response.FileUploadResponse;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    FileUploadResponse store(MultipartFile file, String purpose);

    GridFsResource getById(String id);
}

