package com.zenmanage.sdk.flags;

import java.util.List;

/**
 * Rules document root payload.
 */
public final class RulesResponse {
    private String version;
    private List<FlagData> flags;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<FlagData> getFlags() {
        return flags;
    }

    public void setFlags(List<FlagData> flags) {
        this.flags = flags;
    }
}
