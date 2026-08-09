package com.zenmanage.sdk.flags;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Flag target payload from API.
 */
public final class FlagTarget {
    private String version;

    @JsonProperty("expired_at")
    private String expiredAt;

    @JsonProperty("published_at")
    private String publishedAt;

    @JsonProperty("scheduled_at")
    private String scheduledAt;

    private TargetValue value;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(String expiredAt) {
        this.expiredAt = expiredAt;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(String scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public TargetValue getValue() {
        return value;
    }

    public void setValue(TargetValue value) {
        this.value = value;
    }
}
