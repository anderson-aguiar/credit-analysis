package com.anderson.msscoring.model;

public enum ScoreCategory {

    EXCELLENT(800, 1000),
    GOOD(700, 799),
    FAIR(600, 699),
    POOR(0, 599);

    private final int minScore;
    private final int maxScore;

    ScoreCategory(int minScore, int maxScore) {
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    // Método útil: dado um score, retorna a categoria correta
    public static ScoreCategory fromScore(int score) {
        if (score >= 800) return EXCELLENT;
        if (score >= 700) return GOOD;
        if (score >= 600) return FAIR;
        return POOR;
    }

    @Override
    public String toString() {
        return name() + " (" + minScore + "-" + maxScore + ")";
    }
}