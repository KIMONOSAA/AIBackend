package com.kimo.model.dto;

import lombok.Data;

/**
 * @author Mr.kimo
 * @title CourseAuditRequest
 * @date 2024/11/25 11:05
 * @description TODO
 */
@Data
public class CourseAuditRequest {
    private Long courseId;
    private Long managerId;
    private String suggestion;
}
