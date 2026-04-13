package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.response.FileUploadResponse;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.service.FileStorageService;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class GridFsFileStorageServiceImpl implements FileStorageService {

    private final GridFsTemplate gridFsTemplate;

    public GridFsFileStorageServiceImpl(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    @Override
    public FileUploadResponse store(MultipartFile file, String purpose) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("error.file.empty");
        }

        Document metadata = new Document();
        if (purpose != null && !purpose.isBlank()) {
            metadata.put("purpose", purpose);
        }

        ObjectId id;
        try {
            id = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), metadata);
        } catch (IOException e) {
            throw new BadRequestException("error.file.upload_failed");
        }

        return FileUploadResponse.builder()
                .id(id.toHexString())
                .filename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .build();
    }

    @Override
    public GridFsResource getById(String id) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (Exception e) {
            throw new BadRequestException("error.file.id.invalid", id);
        }

        var gridFsFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(objectId)));
        if (gridFsFile == null) {
            throw new ResourceNotFoundException("error.file.not_found", id);
        }

        return gridFsTemplate.getResource(gridFsFile);
    }
}

