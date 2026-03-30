package com.spartangoldengym.social.dto;

import java.util.List;

public class RankingResponse {

    private String category;
    private List<RankingEntry> entries;

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public List<RankingEntry> getEntries() { return entries; }
    public void setEntries(List<RankingEntry> entries) { this.entries = entries; }
}
