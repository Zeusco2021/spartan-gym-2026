package com.spartangoldengym.calendario.dto;

import java.util.List;

public class ConflictResponse {

    private int totalConflicts;
    private List<ConflictPair> conflicts;

    public int getTotalConflicts() { return totalConflicts; }
    public void setTotalConflicts(int totalConflicts) { this.totalConflicts = totalConflicts; }
    public List<ConflictPair> getConflicts() { return conflicts; }
    public void setConflicts(List<ConflictPair> conflicts) { this.conflicts = conflicts; }

    public static class ConflictPair {
        private EventResponse eventA;
        private EventResponse eventB;

        public EventResponse getEventA() { return eventA; }
        public void setEventA(EventResponse eventA) { this.eventA = eventA; }
        public EventResponse getEventB() { return eventB; }
        public void setEventB(EventResponse eventB) { this.eventB = eventB; }
    }
}
