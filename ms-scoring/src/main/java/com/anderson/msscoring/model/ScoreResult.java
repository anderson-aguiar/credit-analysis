package com.anderson.msscoring.model;

import java.time.LocalDateTime;

public record ScoreResult(

        String customerId,
        Integer score,
        ScoreCategory category,
        LocalDateTime calculatedAt
) {}
