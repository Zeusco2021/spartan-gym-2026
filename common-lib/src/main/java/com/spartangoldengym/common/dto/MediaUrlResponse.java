package com.spartangoldengym.common.dto;

import java.util.Date;

public class MediaUrlResponse {

    private String mediaId;
    private String url;
    private Date expiresAt;

    public MediaUrlResponse() {}

    public MediaUrlResponse(String mediaId, String url, Date expiresAt) {
        this.mediaId = mediaId;
        this.url = url;
        this.expiresAt = expiresAt;
    }

    public String getMediaId() { return mediaId; }
    public void setMediaId(String mediaId) { this.mediaId = mediaId; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Date getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }
}
