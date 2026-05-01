package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import lombok.Data;

import java.util.List;

@Data
public class UpdateStudentRequest {

    private String fullName;

    private String password;

    private String phone;

    private LocalizedText bio;

    private List<String> skills;
}
