package com.brentvatne.exoplayer.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WidevineDrmLicense {
    @JsonProperty("status") private String status;
    @JsonProperty("license") private String license;
    @JsonProperty("security_level") private String securityLevel;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(String securityLevel) {
        this.securityLevel = securityLevel;
    }

    @Override
    public String toString() {
        return "WidevineDrmLicense{" +
                "status='" + status + '\'' +
                ", license='" + license + '\'' +
                ", securityLevel='" + securityLevel + '\'' +
                '}';
    }
}