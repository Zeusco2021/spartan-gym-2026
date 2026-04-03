package com.spartangoldengym.calendario.dto;

public class SyncResponse {

    private String provider;
    private String status;
    private int eventsImported;
    private int eventsExported;

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getEventsImported() { return eventsImported; }
    public void setEventsImported(int eventsImported) { this.eventsImported = eventsImported; }
    public int getEventsExported() { return eventsExported; }
    public void setEventsExported(int eventsExported) { this.eventsExported = eventsExported; }
}
