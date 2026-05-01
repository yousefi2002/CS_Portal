package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import lombok.Data;

@Data
public class UpdateApplicationRequest {

    private String resumeFileId;

    private String portfolioLink;

    private LocalizedText coverLetter;
}
