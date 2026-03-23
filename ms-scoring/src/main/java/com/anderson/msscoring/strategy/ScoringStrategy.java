package com.anderson.msscoring.strategy;

import com.anderson.msscoring.model.CreditHistory;

public interface ScoringStrategy {

    int calculateScore(CreditHistory history);
}
