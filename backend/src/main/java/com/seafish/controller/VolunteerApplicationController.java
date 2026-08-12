package com.seafish.controller;
import com.seafish.common.ApiResponse;
import com.seafish.controller.request.CreateVolunteerApplicationRequest;
import com.seafish.controller.response.VolunteerApplicationResponse;
import com.seafish.service.VolunteerApplicationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RestController
@RequestMapping("/api/volunteer-applications")
public class VolunteerApplicationController {

    private final VolunteerApplicationService
            volunteerApplicationService;

    public VolunteerApplicationController(
            VolunteerApplicationService
                    volunteerApplicationService
    ) {
        this.volunteerApplicationService =
                volunteerApplicationService;
    }

    @PostMapping
    public ApiResponse<VolunteerApplicationResponse>
    createApplication(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            CreateVolunteerApplicationRequest request
    ) {
        Long applicantId =
                Long.valueOf(jwt.getSubject());

        VolunteerApplicationResponse response =
                volunteerApplicationService
                        .createApplication(
                                applicantId,
                                request
                        );

        return ApiResponse.success(response);
    }

    @GetMapping("/me")
    public ApiResponse<List<VolunteerApplicationResponse>>
    getMyApplications(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long applicantId =
                Long.valueOf(jwt.getSubject());

        List<VolunteerApplicationResponse> response =
                volunteerApplicationService
                        .getMyApplications(applicantId);

        return ApiResponse.success(response);
    }
}