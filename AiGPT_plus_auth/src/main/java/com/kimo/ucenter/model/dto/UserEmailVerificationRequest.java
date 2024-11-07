package com.kimo.ucenter.model.dto;

public record UserEmailVerificationRequest(
        String email,
        String userId,
        String code
) {
}
