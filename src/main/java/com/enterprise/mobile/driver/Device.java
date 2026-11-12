package com.enterprise.mobile.driver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Describes a physical device, emulator or simulator used for execution.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Device {

    private String name;
    private String platform;
    private String platformVersion;
    private String udid;
    private String automationName;
    private Map<String, Object> capabilities = new LinkedHashMap<>();

    public Device() {
    }

    public Device(String name, String platform, String platformVersion, String udid, String automationName) {
        this.name = name;
        this.platform = platform;
        this.platformVersion = platformVersion;
        this.udid = udid;
        this.automationName = automationName;
    }

    public Platform platformEnum() {
        return Platform.from(platform);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public String getUdid() {
        return udid;
    }

    public void setUdid(String udid) {
        this.udid = udid;
    }

    public String getAutomationName() {
        return automationName;
    }

    public void setAutomationName(String automationName) {
        this.automationName = automationName;
    }

    public Map<String, Object> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Map<String, Object> capabilities) {
        this.capabilities = capabilities == null ? new LinkedHashMap<>() : capabilities;
    }

    public String displayName() {
        return "%s [%s %s, udid=%s]".formatted(
                name == null ? "unknown-device" : name,
                platform,
                platformVersion == null ? "?" : platformVersion,
                udid == null || udid.isBlank() ? "auto" : udid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Device device)) {
            return false;
        }
        return Objects.equals(name, device.name)
                && Objects.equals(platform, device.platform)
                && Objects.equals(udid, device.udid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, platform, udid);
    }

    @Override
    public String toString() {
        return displayName();
    }
}
