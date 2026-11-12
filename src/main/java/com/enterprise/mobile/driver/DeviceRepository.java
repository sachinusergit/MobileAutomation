package com.enterprise.mobile.driver;

import com.enterprise.mobile.config.ConfigManager;
import com.enterprise.mobile.config.FrameworkConstants;
import com.enterprise.mobile.exceptions.FrameworkException;
import com.enterprise.mobile.utils.AdbUtils;
import com.enterprise.mobile.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Loads the device registry (JSON) and resolves the set of devices that match
 * the current platform and any CLI overrides.
 */
public final class DeviceRepository {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceRepository.class);

    private DeviceRepository() {
    }

    /**
     * Returns all devices configured for the requested platform. An explicit
     * {@code device.udid} or {@code device.name} override in the configuration
     * narrows the result to a single device.
     */
    public static List<Device> load(Platform platform) {
        List<Device> registry = readRegistry();
        List<Device> matching = registry.stream()
                .filter(device -> platform.value().equalsIgnoreCase(device.getPlatform()))
                .collect(Collectors.toList());

        String udid = blankToNull(ConfigManager.get(FrameworkConstants.DEVICE_UDID));
        if (udid != null) {
            // An explicit udid always wins: reuse registry metadata when present,
            // otherwise build the device from configuration.
            Device explicit = matching.stream()
                    .filter(device -> udid.equalsIgnoreCase(device.getUdid()))
                    .findFirst()
                    .orElseGet(() -> fromConfig(platform));
            // A wireless ADB serial changes every time the phone reconnects, so
            // a udid that is not connected right now is replaced by the device
            // that actually is connected.
            String online = resolveOnlineUdid(udid);
            if (online != null && !online.equalsIgnoreCase(udid)) {
                LOG.info("Configured udid '{}' is not connected; using the connected device '{}'",
                        udid, online);
                explicit.setUdid(online);
            }
            LOG.info("Resolved explicit device for udid '{}': {}", udid, explicit.displayName());
            return List.of(explicit);
        }

        if (matching.isEmpty()) {
            LOG.warn("No devices registered for platform '{}'; falling back to configuration", platform.value());
            matching = List.of(fromConfig(platform));
        } else {
            matching = applyNameOverride(matching);
        }

        LOG.info("Resolved {} device(s) for platform {}: {}", matching.size(), platform.value(),
                matching.stream().map(Device::displayName).collect(Collectors.joining(", ")));
        return matching;
    }

    public static Device fromConfig(Platform platform) {
        Device device = new Device();
        device.setPlatform(platform.value());
        device.setName(ConfigManager.get(FrameworkConstants.DEVICE_NAME, platform.value() + "-device"));
        device.setUdid(blankToNull(ConfigManager.get(FrameworkConstants.DEVICE_UDID)));
        device.setPlatformVersion(blankToNull(ConfigManager.get(FrameworkConstants.DEVICE_PLATFORM_VERSION)));
        String automationName = blankToNull(ConfigManager.get(FrameworkConstants.DEVICE_AUTOMATION_NAME));
        device.setAutomationName(automationName != null
                ? automationName
                : platform == Platform.IOS ? "XCUITest" : "UiAutomator2");
        return device;
    }

    private static List<Device> applyNameOverride(List<Device> devices) {
        String name = blankToNull(ConfigManager.get(FrameworkConstants.DEVICE_NAME));
        if (name == null) {
            return devices;
        }
        List<Device> filtered = devices.stream()
                .filter(device -> name.equalsIgnoreCase(device.getName()))
                .collect(Collectors.toList());
        if (filtered.isEmpty()) {
            throw new FrameworkException("No registered device matches the configured name '%s'".formatted(name));
        }
        return filtered;
    }

    private static List<Device> readRegistry() {
        String configuredFile = blankToNull(ConfigManager.get(FrameworkConstants.DEVICE_REGISTRY_FILE));
        if (configuredFile != null) {
            Path path = Paths.get(configuredFile);
            if (!Files.exists(path)) {
                throw new FrameworkException("Device registry file does not exist: " + path.toAbsolutePath());
            }
            return JsonUtils.listFromFile(path, Device.class);
        }

        String resource = ConfigManager.get(FrameworkConstants.DEVICE_REGISTRY,
                FrameworkConstants.DEVICE_REGISTRY_RESOURCE);
        if (DeviceRepository.class.getClassLoader().getResource(resource) == null) {
            LOG.warn("Device registry resource '{}' not found; using configuration fallback", resource);
            return List.of();
        }
        return JsonUtils.listFromResource(resource, Device.class);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /**
     * @return the udid to use: the configured one when it is connected, the
     * single connected device otherwise, or {@code null} when nothing is
     * connected.
     */
    private static String resolveOnlineUdid(String configuredUdid) {
        List<String> connected;
        try {
            connected = AdbUtils.connectedDevices();
        } catch (RuntimeException e) {
            LOG.warn("Could not read the connected devices: {}", e.getMessage());
            return configuredUdid;
        }
        if (connected.isEmpty()) {
            // The wireless connection dropped: try to bring the phone back.
            LOG.info("No Android device is connected; reconnecting {}", configuredUdid);
            try {
                AdbUtils.connect(configuredUdid);
                connected = AdbUtils.connectedDevices();
            } catch (RuntimeException e) {
                LOG.warn("Could not reconnect the device: {}", e.getMessage());
            }
        }
        if (connected.isEmpty()) {
            LOG.warn("No Android device is connected; keeping the configured udid '{}'",
                    configuredUdid);
            return configuredUdid;
        }
        for (String device : connected) {
            if (device.equalsIgnoreCase(configuredUdid)) {
                return configuredUdid;
            }
        }
        // Prefer the "ip:port" form of a wireless device: the mDNS name is not
        // understood by every adb build Appium may use.
        for (String device : connected) {
            if (device.matches(".*:\\d+")) {
                return device;
            }
        }
        if (connected.size() == 1) {
            return connected.get(0);
        }
        LOG.warn("{} devices are connected but '{}' is not one of them; keeping the configured udid",
                connected.size(), configuredUdid);
        return configuredUdid;
    }
}
