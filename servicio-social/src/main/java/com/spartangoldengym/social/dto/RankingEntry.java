package com.spartangoldengym.social.dto;

import java.util.UUID;

public class RankingEntry {

    private int rank;
    private UUID userId;
    private double score;

    public RankingEntry() {}

    public RankingEntry(int rank, UUID userId, double score) {
        this.rank = rank;
        this.userId = userId;
        this.score = score;
    }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
}
