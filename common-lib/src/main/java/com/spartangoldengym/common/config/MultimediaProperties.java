package com.spartangoldengym.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spartan.multimedia")
public class MultimediaProperties {

    private String videoBucket = "spartan-gym-media-videos";
    private String photoBucket = "spartan-gym-media-photos";
    private String cloudfrontDomain = "";
    private String cloudfrontKeyPairId = "";
    private String cloudfrontPrivateKeyPath = "";
    private long presignedUrlExpirationMinutes = 60;

    public String getVideoBucket() { return videoBucket; }
    public void setVideoBucket(String videoBucket) { this.videoBucket = videoBucket; }

    public String getPhotoBucket() { return photoBucket; }
    public void setPhotoBucket(String photoBucket) { this.photoBucket = photoBucket; }

    public String getCloudfrontDomain() { return cloudfrontDomain; }
    public void setCloudfrontDomain(String cloudfrontDomain) { this.cloudfrontDomain = cloudfrontDomain; }

    public String getCloudfrontKeyPairId() { return cloudfrontKeyPairId; }
    public void setCloudfrontKeyPairId(String cloudfrontKeyPairId) { this.cloudfrontKeyPairId = cloudfrontKeyPairId; }

    public String getCloudfrontPrivateKeyPath() { return cloudfrontPrivateKeyPath; }
    public void setCloudfrontPrivateKeyPath(String cloudfrontPrivateKeyPath) { this.cloudfrontPrivateKeyPath = cloudfrontPrivateKeyPath; }

    public long getPresignedUrlExpirationMinutes() { return presignedUrlExpirationMinutes; }
    public void setPresignedUrlExpirationMinutes(long presignedUrlExpirationMinutes) { this.presignedUrlExpirationMinutes = presignedUrlExpirationMinutes; }
}
